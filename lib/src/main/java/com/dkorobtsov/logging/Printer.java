package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.BodyFormatter.formattedBody;
import static com.dkorobtsov.logging.BodyUtil.hasPrintableBody;
import static java.util.Objects.nonNull;

import com.dkorobtsov.logging.enums.Level;
import com.dkorobtsov.logging.internal.InterceptedRequest;
import com.dkorobtsov.logging.internal.InterceptedRequestBody;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import okio.Buffer;
import org.apache.http.util.TextUtils;

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
    private static final String EXECUTION_TIME_TAG = "Execution time: ";
    private static final String CORNER_UP = "┌ ";
    private static final String CORNER_BOTTOM = "└ ";
    private static final String CENTER_LINE = "├ ";
    private static final String DEFAULT_LINE = "  ";
    private static final String SECTION_LINE = "├───────────────────────────────────────────────────────────────────────────────────────";
    private static final String SECTION_DEFAULT_LINE = "| ";
    private static final String THREAD_TAG = "Thread: ";
    private static final String SENT_TAG = "Sent: ";
    private static final String SENT_STRING_FORMAT = "%-5s %-46s %-5s %s";
    private static final String RECEIVED_TAG = "Received: ";
    private static final String RECEIVED_STRING_FORMAT = "%-5s %-42s %-5s %s";
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";

    private static LoggerConfig loggerConfig;

    private Printer() {
    }

    static void printRequest(LoggerConfig loggerConfig, InterceptedRequest request) {
        Printer.loggerConfig = loggerConfig;

        printRequestStartingLine();
        printDebugDetails(true);
        printUrl(request.url().toString());
        printRequestDetails(request);
        printRequestBody(request);
        printEndingLine();
    }

    static void printResponse(LoggerConfig loggerConfig, InterceptedResponse interceptedResponse) {
        Printer.loggerConfig = loggerConfig;

        printResponseStartingLine();
        printDebugDetails(false);
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

    private static void printDebugDetails(boolean isRequest) {
        if (loggerConfig.withThreadInfo) {
            String debugDetails = LINE_SEPARATOR +
                String.format(isRequest ? SENT_STRING_FORMAT : RECEIVED_STRING_FORMAT,
                    THREAD_TAG,
                    Thread.currentThread().getName(),
                    isRequest ? SENT_TAG : RECEIVED_TAG,
                    new SimpleDateFormat(TIMESTAMP_FORMAT)
                        .format(Calendar.getInstance().getTime()));

            logLines(debugDetails.split(REGEX_LINE_SEPARATOR), SECTION_DEFAULT_LINE, true);
            loggerConfig.logger.log(SECTION_LINE);
        }
    }

    private static void printUrl(String url) {
        // ApacheHTTPClient response does not contain URL
        if (!isEmpty(url)) {
            logLines(new String[]{URL_TAG + url}, false);
        }
    }

    private static void printRequestDetails(InterceptedRequest request) {
        logLines(requestDetails(request), true);
    }

    private static void printResponseDetails(InterceptedResponse interceptedResponse) {
        logLines(responseDetails(interceptedResponse), true);
    }

    private static void printRequestBody(InterceptedRequest request) {
        if (bodyShouldBePrinted()) {
            if (hasPrintableBody(mediaType(request))) {
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

    private static String mediaType(InterceptedRequest request) {
        final InterceptedRequestBody requestBody = request.body();

        String requestSubtype = null;
        if (nonNull(requestBody) && nonNull(requestBody.contentType())) {
            requestSubtype = Objects.requireNonNull(requestBody.contentType()).subtype();
        }
        return requestSubtype;
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

    private static String[] requestDetails(InterceptedRequest request) {
        boolean isLoggable = loggerConfig.level == Level.HEADERS
            || loggerConfig.level == Level.BASIC;

        String requestDetails = METHOD_TAG + request.method()
            + DOUBLE_SEPARATOR
            + printHeaderIfLoggable(request.headers().toString(), isLoggable);

        return requestDetails.split(REGEX_LINE_SEPARATOR);
    }

    private static String[] responseDetails(InterceptedResponse interceptedResponse) {
        final boolean isLoggable = loggerConfig.level == Level.HEADERS
            || loggerConfig.level == Level.BASIC;

        final String segmentString = slashSegments(interceptedResponse.segmentList);
        final String receivedTags = interceptedResponse.chainMs == 0
            ? ""
            : " - " + EXECUTION_TIME_TAG + interceptedResponse.chainMs + "ms";

        final String log = (!TextUtils.isEmpty(segmentString)
            ? segmentString + " - "
            : "") + "is success : "
            + interceptedResponse.isSuccessful + receivedTags
            + DOUBLE_SEPARATOR
            + STATUS_CODE_TAG + interceptedResponse.code + " / " + interceptedResponse.message
            + DOUBLE_SEPARATOR
            + printHeaderIfLoggable(interceptedResponse.header, isLoggable);
        return log.split(REGEX_LINE_SEPARATOR);
    }

    private static String slashSegments(List<String> segments) {
        if (segments.isEmpty()) {
            return "";
        }
        StringBuilder segmentString = new StringBuilder();
        for (String segment : segments) {
            segmentString.append("/").append(segment);
        }
        return segmentString.toString();
    }

    private static String printHeaderIfLoggable(String header, boolean loggable) {
        return !isEmpty(header) && loggable
            ? HEADERS_TAG + LINE_SEPARATOR + dotHeaders(header)
            : "";
    }

    private static String bodyToString(final InterceptedRequest request) {
        final InterceptedRequest copy = request.newBuilder().build();
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
        logLines(lines, DEFAULT_LINE, withLineSize);
    }

    private static void logLines(String[] lines, String startingWith, boolean withLineSize) {
        for (String line : lines) {
            if (isEmpty(line)) {
                loggerConfig.logger.log(startingWith);
            } else {
                int lineLength = line.length();
                int maxLongSize = withLineSize ? loggerConfig.maxLineLength : lineLength;
                for (int i = 0; i <= lineLength / maxLongSize; i++) {
                    int start = i * maxLongSize;
                    int end = (i + 1) * maxLongSize;
                    end = end > line.length() ? line.length() : end;
                    loggerConfig.logger.log(startingWith + line.substring(start, end));
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
