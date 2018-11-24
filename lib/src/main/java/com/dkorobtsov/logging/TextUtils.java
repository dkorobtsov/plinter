package com.dkorobtsov.logging;

import java.util.List;

public class TextUtils {

    private TextUtils() {
    }

    public static String slashSegments(List<String> segments) {
        StringBuilder segmentString = new StringBuilder();
        for (String segment : segments) {
            segmentString.append("/").append(segment);
        }
        return segmentString.toString();
    }

    static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static boolean hasPrintableBody(final String subtype) {
        return (subtype != null && (subtype.contains("json")
            || subtype.contains("xml")
            || subtype.contains("plain")
            || subtype.contains("html")));
    }

}
