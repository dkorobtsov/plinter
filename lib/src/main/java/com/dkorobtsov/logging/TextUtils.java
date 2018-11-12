package com.dkorobtsov.logging;

public class TextUtils {

    private TextUtils() {
    }

    static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static boolean isFileRequest(final String subtype) {
        return !(subtype != null && (subtype.contains("json")
            || subtype.contains("xml")
            || subtype.contains("plain")
            || subtype.contains("html")));
    }

}
