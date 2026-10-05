package com.gcu.chinook.generator;

/**
 * A foreign key on one column. If the key spans several columns it is marked
 * composite and no relationship gets generated for it.
 */
record ForeignKeyMeta(String name, String targetTable, String targetColumn, boolean composite) {

    String describe() {
        return targetTable + "(" + targetColumn + ")";
    }
}
