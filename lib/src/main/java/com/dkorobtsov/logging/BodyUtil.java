package com.dkorobtsov.logging;

import static java.util.Objects.nonNull;

public class BodyUtil {

    private BodyUtil() {

    }

    public static boolean hasPrintableBody(final String mediaType) {
        return (nonNull(mediaType) && (mediaType.contains("json")
            || mediaType.contains("xml")
            || mediaType.contains("plain")
            || mediaType.contains("html")));
    }

}
