package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.BodyFormatter.formattedBody;
import static com.dkorobtsov.logging.TextUtils.slashSegments;

import java.io.IOException;
import java.util.Objects;
import okhttp3.Request;
import okio.Buffer;

class Printer {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String REGEX_LINE_SEPARATOR = "\r?\n";
    private static final String DOUBLE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR;

    private static final String[] OMITTED_RESPONSE = {"", "Omitted response body"};
    private static final String[] OMITTED_REQUEST = {"", "Omitted request body"};

    private static final String N = "\n";
    private static final String T = "\t";
    private static final String REQUEST_STARTING_LINE = "┌────── Request ────────────────────────────────────────────────────────────────────────";
    private static final String RESPONSE_STARTING_LINE = "┌────── Response ───────────────────────────────────────────────────────────────────────";
    private static final String ENDING_LINE = "└───────────────────────────────────────────────────────────────────────────────────────";
    private static final String BODY_TAG = "Body:";
    private static final String URL_TAG = "URL: ";
    private static final String METHOD_TAG = "Method: @";
    private static final String HEADERS_TAG = "Headers:";
    private static final String STATUS_CODE_TAG = "Status Code: ";
    private static final String RECEIVED_TAG = "Received in: ";
    private static final String CORNER_UP = "┌ ";
    private static final String CORNER_BOTTOM = "└ ";
    private static final String CENTER_LINE = "├ ";
    private static final String DEFAULT_LINE = "  ";

    private static LoggerConfig loggerConfig;

    private Printer() {
    }

    static void printRequest(LoggerConfig loggerConfig, Request request, boolean hasPrintableBody) {
        Printer.loggerConfig = loggerConfig;

        printRequestStartingLine();
        printUrl(request.url().toString());
        printRequestDetails(request);
        printRequestBody(request, hasPrintableBody);
        printEndingLine();
    }

    static void printResponse(LoggerConfig loggerConfig, InterceptedResponse interceptedResponse) {
        Printer.loggerConfig = loggerConfig;

        printResponseStartingLine();
        printUrl(interceptedResponse.url);
        printResponseDetails(interceptedResponse);
        printResponseBody(interceptedResponse);
        printEndingLine();
    }

    private static void printRequestStartingLine() {
        loggerConfig.logger.log(REQUEST_STARTING_LINE);
    }

    private static void printResponseStartingLine() {
        loggerConfig.logger.log(RESPONSE_STARTING_LINE);
    }

    private static void printEndingLine() {
        loggerConfig.logger.log(ENDING_LINE);
    }

    private static void printUrl(String url) {
        logLines(new String[]{URL_TAG + url}, false);
    }

    private static void printRequestDetails(Request request) {
        logLines(requestDetails(request), true);
    }

    private static void printResponseDetails(InterceptedResponse interceptedResponse) {
        logLines(responseDetails(interceptedResponse), true);
    }

    private static void printRequestBody(Request request, boolean hasPrintableBody) {
        if (bodyShouldBePrinted()) {
            if (hasPrintableBody) {
                String requestBody = LINE_SEPARATOR
                    + BODY_TAG
                    + LINE_SEPARATOR
                    + bodyToString(request);
                logLines(requestBody.split(REGEX_LINE_SEPARATOR), true);
            } else {
                logLines(OMITTED_REQUEST, true);
            }
        }
    }

    private static void printResponseBody(InterceptedResponse interceptedResponse) {
        if (bodyShouldBePrinted()) {
            if (interceptedResponse.hasPrintableBody) {
                final String responseBody = LINE_SEPARATOR
                    + BODY_TAG
                    + LINE_SEPARATOR
                    + formattedBody(interceptedResponse.originalBody);

                logLines(responseBody.split(REGEX_LINE_SEPARATOR), true);
            } else {
                logLines(OMITTED_RESPONSE, true);
            }
        }
    }

    private static boolean bodyShouldBePrinted() {
        return loggerConfig.level == Level.BASIC || loggerConfig.level == Level.BODY;
    }

    private static String[] requestDetails(Request request) {
        boolean isLoggable = loggerConfig.level == Level.HEADERS
            || loggerConfig.level == Level.BASIC;

        String requestDetails = METHOD_TAG + request.method()
            + DOUBLE_SEPARATOR
            + printHeaderIfLoggable(request.headers().toString(), isLoggable);

        return requestDetails.split(REGEX_LINE_SEPARATOR);
    }

    private static String[] responseDetails(InterceptedResponse responseDetails) {
        final boolean isLoggable = loggerConfig.level == Level.HEADERS
            || loggerConfig.level == Level.BASIC;

        final String segmentString = slashSegments(responseDetails.segmentList);
        final String receivedTags = responseDetails.chainMs == 0
            ? ""
            : " - " + RECEIVED_TAG + responseDetails.chainMs + "ms";

        final String log = (!TextUtils.isEmpty(segmentString)
            ? segmentString + " - "
            : "") + "is success : "
            + responseDetails.isSuccessful + receivedTags
            + DOUBLE_SEPARATOR
            + STATUS_CODE_TAG + responseDetails.code + " / " + responseDetails.message
            + DOUBLE_SEPARATOR
            + printHeaderIfLoggable(responseDetails.header, isLoggable);
        return log.split(REGEX_LINE_SEPARATOR);
    }

    private static String printHeaderIfLoggable(String header, boolean loggable) {
        return !isEmpty(header) && loggable
            ? HEADERS_TAG + LINE_SEPARATOR + dotHeaders(header)
            : "";
    }

    private static String bodyToString(final Request request) {
        final Request copy = request.newBuilder().build();
        try (final Buffer buffer = new Buffer()) {
            if (Objects.isNull(copy.body())) {
                return "";
            }
            copy.body().writeTo(buffer);
            return formattedBody(buffer.readUtf8());
        } catch (final IOException e) {
            return "{\"err\": \"" + e.getMessage() + "\"}";
        }
    }

    private static void logLines(String[] lines, boolean withLineSize) {
        for (String line : lines) {
            if (isEmpty(line)) {
                loggerConfig.logger.log(line);
            } else {
                int lineLength = line.length();
                int maxLongSize = withLineSize ? loggerConfig.maxLineLength : lineLength;
                for (int i = 0; i <= lineLength / maxLongSize; i++) {
                    int start = i * maxLongSize;
                    int end = (i + 1) * maxLongSize;
                    end = end > line.length() ? line.length() : end;
                    loggerConfig.logger.log(DEFAULT_LINE + line.substring(start, end));
                }
            }
        }
    }

    private static boolean isEmpty(String line) {
        return TextUtils.isEmpty(line)
            || N.equals(line)
            || T.equals(line)
            || TextUtils.isEmpty(line.trim());
    }

    private static String dotHeaders(String header) {
        String[] headers = header.split(REGEX_LINE_SEPARATOR);
        StringBuilder builder = new StringBuilder();
        String tag = "─ ";
        if (headers.length > 1) {
            for (int i = 0; i < headers.length; i++) {
                if (i == 0) {
                    tag = CORNER_UP;
                } else if (i == headers.length - 1) {
                    tag = CORNER_BOTTOM;
                } else {
                    tag = CENTER_LINE;
                }
                builder.append(tag).append(headers[i]).append(N);
            }
        } else {
            for (String item : headers) {
                builder.append(tag).append(item).append(N);
            }
        }
        return builder.toString();
    }

}
