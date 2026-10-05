package com.gcu.chinook.generator;

/** Turns SQL names into Java names. */
final class Names {

    private Names() {
    }

    /** album_id becomes AlbumId. */
    static String pascal(String sqlName) {
        StringBuilder result = new StringBuilder();
        for (String part : sqlName.split("_")) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return result.toString();
    }

    /** album_id becomes albumId. */
    static String camel(String sqlName) {
        String pascal = pascal(sqlName);
        return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
    }
}
