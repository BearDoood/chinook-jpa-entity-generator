package com.gcu.chinook.generator;

import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Builds the Java source for each table. */
@Component
class EntityCodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(EntityCodeGenerator.class);

    // text columns without a length report a huge size, so skip @Size for those
    private static final int MAX_SIZE_FOR_ANNOTATION = 100_000;

    private final GeneratorProperties properties;

    EntityCodeGenerator(GeneratorProperties properties) {
        this.properties = properties;
    }

    /** Returns file name to source code, one entry per class. */
    Map<String, String> generate(List<TableMeta> tables) {
        Map<String, TableMeta> byName = new LinkedHashMap<>();
        tables.forEach(t -> byName.put(t.name(), t));

        Map<String, String> files = new LinkedHashMap<>();
        for (TableMeta table : tables) {
            String className = Names.className(table.name());
            files.put(className + ".java", generateEntity(table, className, byName));

            if (table.primaryKeyColumns().size() > 1) {
                String idName = className + "Id";
                files.put(idName + ".java", generateIdClass(table, idName));
            }
        }
        return files;
    }

    private String generateEntity(TableMeta table, String className, Map<String, TableMeta> allTables) {
        Set<String> imports = new TreeSet<>();
        imports.add("jakarta.persistence.Entity");
        imports.add("jakarta.persistence.Table");

        boolean compositeKey = table.primaryKeyColumns().size() > 1;
        if (table.primaryKeyColumns().isEmpty()) {
            log.warn("Table {} has no primary key, the entity will not have an @Id", table.name());
        }

        StringBuilder fields = new StringBuilder();
        StringBuilder accessors = new StringBuilder();
        Set<String> usedNames = new TreeSet<>();
        table.columns().forEach(c -> usedNames.add(Names.camel(c.name())));

        for (ColumnMeta column : table.columns()) {
            String fieldName = Names.camel(column.name());
            String javaType = javaType(column, imports);

            appendColumnField(fields, column, fieldName, javaType, compositeKey, imports);
            appendAccessors(accessors, fieldName, javaType, true);
        }

        for (ColumnMeta column : table.columns()) {
            ForeignKeyMeta fk = column.foreignKey();
            if (fk == null) {
                continue;
            }
            if (fk.composite()) {
                log.warn("Skipping relationship for {}.{} because {} spans several columns",
                        table.name(), column.name(), fk.name());
                continue;
            }
            if (!allTables.containsKey(fk.targetTable())) {
                log.warn("Skipping relationship for {}.{} because table {} was not found",
                        table.name(), column.name(), fk.targetTable());
                continue;
            }

            String targetClass = Names.className(fk.targetTable());
            String relationName = relationName(column, targetClass, usedNames);
            usedNames.add(relationName);

            imports.add("jakarta.persistence.FetchType");
            imports.add("jakarta.persistence.JoinColumn");
            imports.add("jakarta.persistence.ManyToOne");

            // The scalar field above does the writing, this one is read only
            fields.append("    @ManyToOne(fetch = FetchType.LAZY)\n");
            fields.append("    @JoinColumn(name = \"").append(Names.sqlLiteral(column.name()))
                    .append("\", insertable = false, updatable = false)\n");
            fields.append("    private ").append(targetClass).append(' ').append(relationName).append(";\n\n");
            appendAccessors(accessors, relationName, targetClass, false);
        }

        StringBuilder out = new StringBuilder();
        out.append("package ").append(properties.packageName()).append(";\n\n");

        if (compositeKey) {
            imports.add("jakarta.persistence.IdClass");
        }
        imports.forEach(i -> out.append("import ").append(i).append(";\n"));

        out.append("\n/** Generated from table ").append(table.name()).append(". */\n");
        out.append("@Entity\n");
        out.append("@Table(name = \"").append(Names.sqlLiteral(table.name())).append("\")\n");
        if (compositeKey) {
            out.append("@IdClass(").append(className).append("Id.class)\n");
        }
        out.append("class ").append(className).append(" {\n\n");
        out.append(fields);
        out.append(accessors.toString().stripTrailing()).append("\n");
        out.append("}\n");
        return out.toString();
    }

    private void appendColumnField(StringBuilder fields, ColumnMeta column, String fieldName, String javaType,
            boolean compositeKey, Set<String> imports) {
        boolean generatedKey = column.primaryKey() && column.autoIncrement() && !compositeKey;

        if (column.primaryKey()) {
            imports.add("jakarta.persistence.Id");
            fields.append("    @Id\n");
        }
        if (generatedKey) {
            imports.add("jakarta.persistence.GeneratedValue");
            imports.add("jakarta.persistence.GenerationType");
            fields.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
        }

        // A generated key is null until the insert happens, so it cannot be @NotNull
        if (!column.nullable() && !generatedKey) {
            imports.add("jakarta.validation.constraints.NotNull");
            fields.append("    @NotNull\n");
        }
        boolean text = isText(column);
        boolean sized = text && column.size() > 0 && column.size() <= MAX_SIZE_FOR_ANNOTATION;
        if (sized) {
            imports.add("jakarta.validation.constraints.Size");
            fields.append("    @Size(max = ").append(column.size()).append(")\n");
        }

        imports.add("jakarta.persistence.Column");
        List<String> attributes = new ArrayList<>();
        attributes.add("name = \"" + Names.sqlLiteral(column.name()) + "\"");
        if (!column.nullable()) {
            attributes.add("nullable = false");
        }
        if (sized) {
            attributes.add("length = " + column.size());
        }
        if (column.jdbcType() == Types.NUMERIC || column.jdbcType() == Types.DECIMAL) {
            if (column.size() > 0 && column.size() < 1000) {
                attributes.add("precision = " + column.size());
                attributes.add("scale = " + column.decimalDigits());
            }
        }
        fields.append("    @Column(").append(String.join(", ", attributes)).append(")\n");
        fields.append("    private ").append(javaType).append(' ').append(fieldName).append(";\n\n");
    }

    private String generateIdClass(TableMeta table, String idName) {
        Set<String> imports = new TreeSet<>();
        imports.add("java.io.Serializable");
        imports.add("java.util.Objects");

        List<ColumnMeta> keyColumns = new ArrayList<>();
        for (String keyName : table.primaryKeyColumns()) {
            table.columns().stream().filter(c -> c.name().equals(keyName)).findFirst().ifPresent(keyColumns::add);
        }

        StringBuilder fields = new StringBuilder();
        StringBuilder accessors = new StringBuilder();
        List<String> params = new ArrayList<>();
        StringBuilder assignments = new StringBuilder();
        StringBuilder equalsChecks = new StringBuilder();
        List<String> hashFields = new ArrayList<>();

        for (ColumnMeta column : keyColumns) {
            String fieldName = Names.camel(column.name());
            String javaType = javaType(column, imports);
            fields.append("    private ").append(javaType).append(' ').append(fieldName).append(";\n");
            params.add(javaType + " " + fieldName);
            assignments.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
            if (equalsChecks.length() > 0) {
                equalsChecks.append("\n                && ");
            }
            equalsChecks.append("Objects.equals(").append(fieldName).append(", that.").append(fieldName).append(")");
            hashFields.add(fieldName);
            appendAccessors(accessors, fieldName, javaType, false);
        }

        StringBuilder out = new StringBuilder();
        out.append("package ").append(properties.packageName()).append(";\n\n");
        imports.forEach(i -> out.append("import ").append(i).append(";\n"));
        out.append("\n/** Composite key for table ").append(table.name()).append(". */\n");
        out.append("class ").append(idName).append(" implements Serializable {\n\n");
        out.append(fields).append("\n");
        out.append("    public ").append(idName).append("() {\n    }\n\n");
        out.append("    public ").append(idName).append("(").append(String.join(", ", params)).append(") {\n");
        out.append(assignments).append("    }\n\n");
        out.append(accessors);
        out.append("    @Override\n");
        out.append("    public boolean equals(Object o) {\n");
        out.append("        if (this == o) {\n            return true;\n        }\n");
        out.append("        if (!(o instanceof ").append(idName).append(" that)) {\n            return false;\n        }\n");
        out.append("        return ").append(equalsChecks).append(";\n");
        out.append("    }\n\n");
        out.append("    @Override\n");
        out.append("    public int hashCode() {\n");
        out.append("        return Objects.hash(").append(String.join(", ", hashFields)).append(");\n");
        out.append("    }\n");
        out.append("}\n");
        return out.toString();
    }

    /** artist_id becomes artist. If that name is taken, the target class is added. */
    private String relationName(ColumnMeta column, String targetClass, Set<String> usedNames) {
        String base = Names.camel(column.name());
        if (base.length() > 2 && base.endsWith("Id")) {
            base = base.substring(0, base.length() - 2);
        }
        if (!usedNames.contains(base)) {
            return base;
        }
        return base + targetClass;
    }

    private boolean isText(ColumnMeta column) {
        return switch (column.jdbcType()) {
            case Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR, Types.NVARCHAR, Types.NCHAR -> true;
            default -> false;
        };
    }

    private String javaType(ColumnMeta column, Set<String> imports) {
        switch (column.jdbcType()) {
            case Types.INTEGER:
                return "Integer";
            case Types.SMALLINT:
            case Types.TINYINT:
                return "Short";
            case Types.BIGINT:
                return "Long";
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.NVARCHAR:
            case Types.NCHAR:
                return "String";
            case Types.NUMERIC:
            case Types.DECIMAL:
                imports.add("java.math.BigDecimal");
                return "BigDecimal";
            case Types.DOUBLE:
            case Types.FLOAT:
                return "Double";
            case Types.REAL:
                return "Float";
            case Types.BIT:
            case Types.BOOLEAN:
                return "Boolean";
            case Types.DATE:
                imports.add("java.time.LocalDate");
                return "LocalDate";
            case Types.TIMESTAMP:
                imports.add("java.time.LocalDateTime");
                return "LocalDateTime";
            case Types.TIMESTAMP_WITH_TIMEZONE:
                imports.add("java.time.OffsetDateTime");
                return "OffsetDateTime";
            case Types.TIME:
                imports.add("java.time.LocalTime");
                return "LocalTime";
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return "byte[]";
            default:
                log.warn("No mapping for SQL type {} ({}), using Object", column.typeName(), column.jdbcType());
                return "Object";
        }
    }

    private void appendAccessors(StringBuilder out, String fieldName, String javaType, boolean withSetter) {
        String suffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        out.append("    public ").append(javaType).append(" get").append(suffix).append("() {\n");
        out.append("        return ").append(fieldName).append(";\n    }\n\n");
        if (withSetter) {
            out.append("    public void set").append(suffix).append("(").append(javaType).append(' ')
                    .append(fieldName).append(") {\n");
            out.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n    }\n\n");
        }
    }
}
