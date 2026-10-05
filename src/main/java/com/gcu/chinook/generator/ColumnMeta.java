package com.gcu.chinook.generator;

record ColumnMeta(
        String name,
        int jdbcType,
        String typeName,
        int size,
        int decimalDigits,
        boolean nullable,
        boolean primaryKey,
        boolean autoIncrement,
        ForeignKeyMeta foreignKey) {
}
