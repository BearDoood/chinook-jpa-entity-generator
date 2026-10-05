package com.gcu.chinook.generator;

import java.util.Set;

/** Turns SQL names into Java names and SQL names into annotation values. */
final class Names {

    private static final Set<String> RESERVED = Set.of("class", "default", "package", "public", "private", "new",
            "long", "int", "short", "double", "float", "boolean", "char", "byte", "void", "static", "final",
            "interface", "enum", "import", "return", "switch", "case", "this", "super", "try", "catch", "throw");

    private Names() {
    }

    /** album_id or AlbumId becomes AlbumId. */
    static String pascal(String sqlName) {
        StringBuilder result = new StringBuilder();
        for (String part : sqlName.split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            // ALLCAPS parts get lowercased so ALBUM does not stay shouting
            String cleaned = part.length() > 1 && part.equals(part.toUpperCase()) ? part.toLowerCase() : part;
            result.append(Character.toUpperCase(cleaned.charAt(0))).append(cleaned.substring(1));
        }
        return result.toString();
    }

    /** album_id or AlbumId becomes albumId. */
    static String camel(String sqlName) {
        String pascal = pascal(sqlName);
        String camel = Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
        return RESERVED.contains(camel) ? camel + "_" : camel;
    }

    /** Class name from a table name. Chinook tables are already singular. */
    static String className(String tableName) {
        return pascal(tableName);
    }

    /**
     * The value to put inside a Java string literal for an annotation name.
     * Names that are not plain lowercase must stay quoted in SQL, so the
     * quotes are escaped here.
     */
    static String sqlLiteral(String sqlName) {
        if (sqlName.matches("[a-z_][a-z0-9_]*")) {
            return sqlName;
        }
        return "\\\"" + sqlName + "\\\"";
    }
}
