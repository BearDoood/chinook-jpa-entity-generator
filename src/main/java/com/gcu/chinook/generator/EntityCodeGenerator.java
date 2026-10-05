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

    private final GeneratorProperties properties;

    EntityCodeGenerator(GeneratorProperties properties) {
        this.properties = properties;
    }

    /** Returns file name to source code, one entry per table. */
    Map<String, String> generate(List<TableMeta> tables) {
        Map<String, String> files = new LinkedHashMap<>();
        for (TableMeta table : tables) {
            String className = Names.pascal(table.name());
            files.put(className + ".java", generateEntity(table, className));
        }
        return files;
    }

    private String generateEntity(TableMeta table, String className) {
        Set<String> imports = new TreeSet<>();
        imports.add("jakarta.persistence.Entity");
        imports.add("jakarta.persistence.Table");
        imports.add("jakarta.persistence.Column");

        StringBuilder fields = new StringBuilder();
        StringBuilder accessors = new StringBuilder();

        for (ColumnMeta column : table.columns()) {
            String fieldName = Names.camel(column.name());
            String javaType = javaType(column, imports);

            if (column.primaryKey()) {
                imports.add("jakarta.persistence.Id");
                fields.append("    @Id\n");
            }
            if (column.primaryKey() && column.autoIncrement()) {
                imports.add("jakarta.persistence.GeneratedValue");
                imports.add("jakarta.persistence.GenerationType");
                fields.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
            }
            if (!column.nullable()) {
                imports.add("jakarta.validation.constraints.NotNull");
                fields.append("    @NotNull\n");
            }
            boolean sized = isText(column) && column.size() > 0;
            if (sized) {
                imports.add("jakarta.validation.constraints.Size");
                fields.append("    @Size(max = ").append(column.size()).append(")\n");
            }

            List<String> attributes = new ArrayList<>();
            attributes.add("name = \"" + column.name() + "\"");
            if (!column.nullable()) {
                attributes.add("nullable = false");
            }
            if (sized) {
                attributes.add("length = " + column.size());
            }
            fields.append("    @Column(").append(String.join(", ", attributes)).append(")\n");
            fields.append("    private ").append(javaType).append(' ').append(fieldName).append(";\n\n");

            appendAccessors(accessors, fieldName, javaType);
        }

        StringBuilder out = new StringBuilder();
        out.append("package ").append(properties.packageName()).append(";\n\n");
        imports.forEach(i -> out.append("import ").append(i).append(";\n"));
        out.append("\n/** Generated from table ").append(table.name()).append(". */\n");
        out.append("@Entity\n");
        out.append("@Table(name = \"").append(table.name()).append("\")\n");
        out.append("class ").append(className).append(" {\n\n");
        out.append(fields);
        out.append(accessors.toString().stripTrailing()).append("\n");
        out.append("}\n");
        return out.toString();
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
            case Types.TIME:
                imports.add("java.time.LocalTime");
                return "LocalTime";
            default:
                log.warn("No mapping for SQL type {} ({}), using Object", column.typeName(), column.jdbcType());
                return "Object";
        }
    }

    private void appendAccessors(StringBuilder out, String fieldName, String javaType) {
        String suffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        out.append("    public ").append(javaType).append(" get").append(suffix).append("() {\n");
        out.append("        return ").append(fieldName).append(";\n    }\n\n");
        out.append("    public void set").append(suffix).append("(").append(javaType).append(' ')
                .append(fieldName).append(") {\n");
        out.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n    }\n\n");
    }
}
