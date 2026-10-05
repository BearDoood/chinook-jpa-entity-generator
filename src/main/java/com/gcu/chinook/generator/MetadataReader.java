package com.gcu.chinook.generator;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Reads tables, columns, keys and foreign keys from JDBC metadata. */
@Component
class MetadataReader {

    private static final Logger log = LoggerFactory.getLogger(MetadataReader.class);

    private final DataSource dataSource;
    private final GeneratorProperties properties;

    MetadataReader(DataSource dataSource, GeneratorProperties properties) {
        this.dataSource = dataSource;
        this.properties = properties;
    }

    List<TableMeta> read() throws SQLException {
        List<TableMeta> tables = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            String schema = properties.schema();

            log.info("Connected to {} {}", metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());

            for (String tableName : readTableNames(metaData, catalog, schema)) {
                List<String> primaryKeys = readPrimaryKeys(metaData, catalog, schema, tableName);
                Map<String, ForeignKeyMeta> foreignKeys = readForeignKeys(metaData, catalog, schema, tableName);
                List<ColumnMeta> columns = readColumns(metaData, catalog, schema, tableName, primaryKeys, foreignKeys);
                tables.add(new TableMeta(tableName, columns, primaryKeys));
            }
        }

        logTables(tables);
        return tables;
    }

    private List<String> readTableNames(DatabaseMetaData metaData, String catalog, String schema) throws SQLException {
        List<String> names = new ArrayList<>();
        try (ResultSet rs = metaData.getTables(catalog, schema, "%", new String[] {"TABLE"})) {
            while (rs.next()) {
                names.add(rs.getString("TABLE_NAME"));
            }
        }
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    private List<String> readPrimaryKeys(DatabaseMetaData metaData, String catalog, String schema, String table)
            throws SQLException {
        Map<Integer, String> bySequence = new TreeMap<>();
        try (ResultSet rs = metaData.getPrimaryKeys(catalog, schema, table)) {
            while (rs.next()) {
                bySequence.put(rs.getInt("KEY_SEQ"), rs.getString("COLUMN_NAME"));
            }
        }
        return new ArrayList<>(bySequence.values());
    }

    private Map<String, ForeignKeyMeta> readForeignKeys(DatabaseMetaData metaData, String catalog, String schema,
            String table) throws SQLException {
        // Group rows by constraint name so multi-column keys can be spotted
        Map<String, List<String[]>> byConstraint = new LinkedHashMap<>();
        try (ResultSet rs = metaData.getImportedKeys(catalog, schema, table)) {
            while (rs.next()) {
                byConstraint.computeIfAbsent(rs.getString("FK_NAME"), k -> new ArrayList<>()).add(new String[] {
                        rs.getString("FKCOLUMN_NAME"), rs.getString("PKTABLE_NAME"), rs.getString("PKCOLUMN_NAME")});
            }
        }

        Map<String, ForeignKeyMeta> byColumn = new HashMap<>();
        byConstraint.forEach((constraint, rows) -> {
            boolean composite = rows.size() > 1;
            for (String[] row : rows) {
                byColumn.put(row[0], new ForeignKeyMeta(constraint, row[1], row[2], composite));
            }
        });
        return byColumn;
    }

    private List<ColumnMeta> readColumns(DatabaseMetaData metaData, String catalog, String schema, String table,
            List<String> primaryKeys, Map<String, ForeignKeyMeta> foreignKeys) throws SQLException {
        List<ColumnMeta> columns = new ArrayList<>();
        try (ResultSet rs = metaData.getColumns(catalog, schema, table, "%")) {
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                columns.add(new ColumnMeta(
                        name,
                        rs.getInt("DATA_TYPE"),
                        rs.getString("TYPE_NAME"),
                        rs.getInt("COLUMN_SIZE"),
                        rs.getInt("DECIMAL_DIGITS"),
                        rs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls,
                        primaryKeys.contains(name),
                        "YES".equals(rs.getString("IS_AUTOINCREMENT")),
                        foreignKeys.get(name)));
            }
        }
        return columns;
    }

    private void logTables(List<TableMeta> tables) {
        log.info("Discovered {} tables", tables.size());
        for (TableMeta table : tables) {
            log.info("table={} columns={} primaryKey={}", table.name(), table.columns().size(),
                    table.primaryKeyColumns());
            for (ColumnMeta column : table.columns()) {
                log.info("table={} column={} type={} length={} nullable={} primaryKey={} foreignKey={}",
                        table.name(), column.name(), column.typeName(), column.size(), column.nullable(),
                        column.primaryKey(),
                        column.foreignKey() == null ? "none" : column.foreignKey().describe());
            }
        }
    }
}
