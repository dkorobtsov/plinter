/*
 *  MIT License
 *
 * Copyright (c) 2018 Dmitri Korobtsov
 * Copyright (c) 2017 ihsan BAL
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.dkorobtsov.plinter.core.internal;

import static io.github.dkorobtsov.plinter.core.internal.BodyFormatter.formattedBody;
import static io.github.dkorobtsov.plinter.core.internal.Util.hasPrintableBody;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import io.github.dkorobtsov.plinter.core.Level;
import io.github.dkorobtsov.plinter.core.LogWriter;
import io.github.dkorobtsov.plinter.core.LoggerConfig;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import okio.Buffer;


/**
 * Class responsible for formatting intercepted events and logging them using provided {@link
 * LogWriter} implementation.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.GodClass"})
final class Printer {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String REGEX_LINE_SEPARATOR = "\r?\n";
  private static final String DOUBLE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR;

  private static final String N = "\n";
  private static final String T = "\t";

  private static final String REQUEST_STARTING_LINE = "┌────── Request ";
  private static final String RESPONSE_STARTING_LINE = "┌────── Response ";
  private static final String ENDING_LINE = "└";
  private static final String CORNER_UP = "┌ ";
  private static final String CORNER_BOTTOM = "└ ";
  private static final String CENTER_LINE = "├ ";
  private static final String SECTION_DEFAULT_LINE = "| ";
  private static final String DEFAULT_LINE = "  ";
  private static final String SECTION_LINE = "├";
  private static final char HORIZONTAL_LINE = '─';

  private static final String BODY_TAG = "Body:";
  private static final String URL_TAG = "URL: ";
  private static final String METHOD_TAG = "Method: @";
  private static final String HEADERS_TAG = "Headers:";
  private static final String STATUS_CODE_TAG = "Status Code: ";
  private static final String EXECUTION_TIME_TAG = "Execution time: ";
  private static final String THREAD_TAG = "Thread: ";
  private static final String SENT_TAG = "Sent: ";
  private static final String RECEIVED_TAG = "Received: ";
  private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";
  private static final String THREAD_STRING_FORMAT = "%-5s %-{indent}s %-5s %s";
  private static final int THREAD_INDENT = 36;

  private static final String[] OMITTED_RESPONSE = {"", "Omitted response body"};
  private static final String[] OMITTED_REQUEST = {"", "Omitted request body"};
  private static final String[] EMPTY_REQUEST_BODY = {"", "Empty request body"};
  private static final String[] EMPTY_RESPONSE_BODY = {"", "Empty response body"};

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
    final int i = loggerConfig.maxLineLength - REQUEST_STARTING_LINE.length();
    loggerConfig.logger.log(REQUEST_STARTING_LINE + repeatChar(HORIZONTAL_LINE, i));
  }

  private static void printResponseStartingLine() {
    final int i = loggerConfig.maxLineLength - RESPONSE_STARTING_LINE.length();
    loggerConfig.logger.log(RESPONSE_STARTING_LINE + repeatChar(HORIZONTAL_LINE, i));
  }

  private static void printEndingLine() {
    final int i = loggerConfig.maxLineLength - ENDING_LINE.length();
    loggerConfig.logger.log(ENDING_LINE + repeatChar(HORIZONTAL_LINE, i));
  }

  private static void printSectionLine() {
    final int i = loggerConfig.maxLineLength - SECTION_LINE.length();
    loggerConfig.logger.log(SECTION_LINE + repeatChar(HORIZONTAL_LINE, i));
  }

  private static void printDebugDetails(boolean isRequest) {
    if (loggerConfig.withThreadInfo) {
      final String format = threadInfoStringFormat(isRequest);

      final String debugDetails = LINE_SEPARATOR
          + String.format(format,
          THREAD_TAG, Thread.currentThread().getName(),
          isRequest ? SENT_TAG : RECEIVED_TAG,
          new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault())
              .format(Calendar.getInstance().getTime()));

      logLines(debugDetails.split(REGEX_LINE_SEPARATOR), SECTION_DEFAULT_LINE, true);
      printSectionLine();
    }
  }

  private static String threadInfoStringFormat(boolean isRequest) {
    final int requestIndent = loggerConfig.maxLineLength - THREAD_INDENT - SENT_TAG.length();
    final int responseIndent = loggerConfig.maxLineLength - THREAD_INDENT - RECEIVED_TAG.length();
    final int indent = isRequest ? requestIndent : responseIndent;
    return THREAD_STRING_FORMAT.replace("{indent}", String.valueOf(indent));
  }

  private static void printUrl(String url) {
    if (!isEmpty(url)) {
      logLines(new String[]{URL_TAG + url, ""}, false);
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
        String printableBody = bodyToString(request);

        // To handle situations, when we expect printable body based on
        // media type but nothing is returned.
        if (printableBody.isEmpty()) {
          logLines(EMPTY_REQUEST_BODY, true);
        } else {
          final String requestBody = LINE_SEPARATOR
              + BODY_TAG
              + LINE_SEPARATOR
              + printableBody;

          logLines(requestBody.split(REGEX_LINE_SEPARATOR), true);
        }

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
        String printableBody = formattedBody(interceptedResponse.originalBody);

        if (printableBody.isEmpty()) {
          logLines(EMPTY_RESPONSE_BODY, true);
        } else {
          final String responseBody = LINE_SEPARATOR
              + BODY_TAG
              + LINE_SEPARATOR
              + printableBody;

          logLines(responseBody.split(REGEX_LINE_SEPARATOR), true);
        }
      } else {
        logLines(OMITTED_RESPONSE, true);
      }
    }
  }

  private static boolean bodyShouldBePrinted() {
    return loggerConfig.level == Level.BASIC || loggerConfig.level == Level.BODY;
  }

  private static String[] requestDetails(InterceptedRequest request) {
    final boolean isLoggable = loggerConfig.level == Level.HEADERS
        || loggerConfig.level == Level.BASIC;

    final String requestDetails = METHOD_TAG + request.method()
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

    final String statusMessage = nonNull(interceptedResponse.message)
        ? interceptedResponse.message : "";

    final String log = (!isEmpty(segmentString)
        ? segmentString + " - "
        : "") + "is success : "
        + interceptedResponse.isSuccessful + receivedTags
        + DOUBLE_SEPARATOR
        + STATUS_CODE_TAG + interceptedResponse.code + " / " + statusMessage
        + DOUBLE_SEPARATOR
        + printHeaderIfLoggable(interceptedResponse.header, isLoggable);
    return log.split(REGEX_LINE_SEPARATOR);
  }

  private static String slashSegments(List<String> segments) {
    if (isNull(segments) || segments.isEmpty()) {
      return "";
    }
    final StringBuilder segmentString = new StringBuilder();
    for (String segment : segments) {
      segmentString.append('/').append(segment);
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
    try (Buffer buffer = new Buffer()) {
      if (isNull(copy.body())) {
        return "";
      }
      copy.body().writeTo(buffer);
      return formattedBody(buffer.readByteArray());
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
        logLine(startingWith, withLineSize, line);
      }
    }
  }

  private static void logLine(String startingWith, boolean withLineSize, String line) {
    final int lineLength = line.length();
    final int maxLongSize = withLineSize
        ? loggerConfig.maxLineLength - startingWith.length()
        : lineLength;
    for (int i = 0; i <= lineLength / maxLongSize; i++) {
      final int start = i * maxLongSize;
      int end = (i + 1) * maxLongSize;
      end = end > line.length() ? line.length() : end;

      if (start != end) {
        // This condition check handles very rare occasion when multiline string exactly matches
        // max line length, in that case unnecessary empty line will be printed
        loggerConfig.logger.log(startingWith + line.substring(start, end));
      }
    }
  }

  private static boolean isEmpty(String line) {
    return Util.isEmpty(line)
        || N.equals(line)
        || T.equals(line)
        || Util.isEmpty(line.trim());
  }

  private static String dotHeaders(String header) {
    final String[] headers = header.split(REGEX_LINE_SEPARATOR);

    final StringBuilder builder = new StringBuilder();
    String tag = "";
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

  private static String repeatChar(final char val, final int times) {
    final char[] chars = new char[times < 0 ? 0 : times];
    Arrays.fill(chars, val);
    return new String(chars);
  }

}
