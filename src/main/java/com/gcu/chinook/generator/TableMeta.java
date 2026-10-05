package com.gcu.chinook.generator;

import java.util.List;

/** primaryKeyColumns are in key order, which matters for composite keys. */
record TableMeta(String name, List<ColumnMeta> columns, List<String> primaryKeyColumns) {
}
