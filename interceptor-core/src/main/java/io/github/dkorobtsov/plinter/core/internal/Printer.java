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


import static io.github.dkorobtsov.plinter.core.internal.Util.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import io.github.dkorobtsov.plinter.core.Level;
import io.github.dkorobtsov.plinter.core.LogWriter;
import io.github.dkorobtsov.plinter.core.LoggerConfig;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;


/**
 * Class responsible for formatting intercepted events and logging them using provided {@link
 * LogWriter} implementation.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.GodClass"})
final class Printer {

  private static final Logger logger = Logger.getLogger(Printer.class.getName());
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String DOUBLE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR;
  private static final String REGEX_LINE_SEPARATOR = "\r?\n";

  private static final String N = "\n";
  private static final String T = "\t";

  private static final String REQUEST_STARTING_LINE = "┌────── Request ";
  private static final String RESPONSE_STARTING_LINE = "┌────── Response ";
  private static final String ENDING_LINE = "└";
  private static final String CORNER_UP = "┌ ";
  private static final String CORNER_BOTTOM = "└ ";
  private static final String CENTER_LINE = "├ ";
  private static final String SECTION_DEFAULT_LINE = "│ ";
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

  private static final String EMPTY_STRING = "";
  private static final String[] OMITTED_RESPONSE = {EMPTY_STRING, "Omitted response body"};
  private static final String[] OMITTED_REQUEST = {EMPTY_STRING, "Omitted request body"};
  private static final String[] EMPTY_REQUEST_BODY = {EMPTY_STRING, "Empty request body"};
  private static final String[] EMPTY_RESPONSE_BODY = {EMPTY_STRING, "Empty response body"};
  private static final String[] PRINTING_FAILED = {EMPTY_STRING,
      "[LoggingInterceptorError] : failed to print body"};


  private static LoggerConfig loggerConfig;

  private Printer() {
  }

  static void printRequest(LoggerConfig loggerConfig, InterceptedRequest request) {
    Printer.loggerConfig = loggerConfig;

    String event = formatStartingLine(true)
        + formatDebugDetails(true)
        + formatUrl(request.url().toString())
        + formatRequestDetails(request)
        + formatRequestBody(request)
        + formatEndingLine();

    logEvent(loggerConfig, event);
  }

  static void printResponse(LoggerConfig loggerConfig, InterceptedResponse interceptedResponse) {
    Printer.loggerConfig = loggerConfig;

    String event = formatStartingLine(false)
        + formatDebugDetails(false)
        + formatUrl(interceptedResponse.url)
        + formatResponseDetails(interceptedResponse)
        + formatResponseBody(interceptedResponse)
        + formatEndingLine();

    logEvent(loggerConfig, event);
  }

  private static void logEvent(LoggerConfig loggerConfig, String event) {
    if (loggerConfig.logByLine) {
      Arrays.stream(event.split(REGEX_LINE_SEPARATOR))
          .map(String::stripTrailing)
          .collect(Collectors.toList())
          .forEach(loggerConfig.logger::log);
    } else {
      loggerConfig.logger.log(event);
    }
  }

  private static String formatStartingLine(boolean isRequest) {
    String title = isRequest ? REQUEST_STARTING_LINE : RESPONSE_STARTING_LINE;
    final int length = loggerConfig.maxLineLength - title.length();
    return LINE_SEPARATOR + title + drawHorizontalLine(length);
  }

  private static String formatEndingLine() {
    final int length = loggerConfig.maxLineLength - ENDING_LINE.length();
    return LINE_SEPARATOR + ENDING_LINE + drawHorizontalLine(length);
  }

  private static String formatSectionHorizontalLine() {
    final int length = loggerConfig.maxLineLength - SECTION_LINE.length();
    return LINE_SEPARATOR + SECTION_LINE + drawHorizontalLine(length);
  }

  private static String formatDebugDetails(boolean isRequest) {
    if (!loggerConfig.withThreadInfo) {
      return EMPTY_STRING;
    }

    final String format = threadInfoStringFormat(isRequest);
    final String tag = isRequest ? SENT_TAG : RECEIVED_TAG;
    final String thread = Thread.currentThread().getName();
    final String date = new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault())
        .format(Calendar.getInstance().getTime());

    final String debugDetails = N + String.format(format, THREAD_TAG, thread, tag, date);

    StringBuilder sb = logLines(debugDetails
        .split(REGEX_LINE_SEPARATOR), SECTION_DEFAULT_LINE, true)
        .append(formatSectionHorizontalLine());

    return sb.toString();
  }

  private static String threadInfoStringFormat(boolean isRequest) {
    final int requestIndent = loggerConfig.maxLineLength - THREAD_INDENT - SENT_TAG.length();
    final int responseIndent = loggerConfig.maxLineLength - THREAD_INDENT - RECEIVED_TAG.length();
    final int indent = isRequest ? requestIndent : responseIndent;
    return THREAD_STRING_FORMAT.replace("{indent}", String.valueOf(indent));
  }

  private static String formatUrl(String url) {
    if (!isEmpty(url)) {
      return logLines(new String[]{URL_TAG + url, EMPTY_STRING}, false);
    }
    return EMPTY_STRING;
  }

  private static String formatRequestDetails(InterceptedRequest request) {
    return logLines(requestDetails(request), true);
  }

  private static String formatResponseDetails(InterceptedResponse interceptedResponse) {
    return logLines(responseDetails(interceptedResponse), true);
  }

  private static String formatRequestBody(InterceptedRequest request) {
    if (!bodyShouldBePrinted()) {
      return EMPTY_STRING;
    }

    final InterceptedRequest copy = request.newBuilder().build();
    if (isNull(copy.body)) {
      return logLines(EMPTY_REQUEST_BODY, true);
    }

    try (Buffer buffer = new Buffer()) {
      copy.body.writeTo(buffer);
      if (Util.isUtf8(buffer)) {
        final String printableBody = BodyFormatter
            .formattedBody(new String(buffer.readByteArray(), UTF_8));

        // To handle situations, when we expect printable body based on
        // media type but nothing is returned.
        if (printableBody.isEmpty()) {
          return logLines(EMPTY_REQUEST_BODY, true);
        } else {
          final String requestBody = LINE_SEPARATOR
              + BODY_TAG
              + LINE_SEPARATOR
              + printableBody;

          return logLines(requestBody.split(REGEX_LINE_SEPARATOR), true);
        }
      } else {
        return logLines(OMITTED_REQUEST, true);
      }
    } catch (IOException e) {
      logger.log(java.util.logging.Level.SEVERE, e.getMessage(), e);
      return logLines(PRINTING_FAILED, true);
    }
  }

  private static String formatResponseBody(InterceptedResponse interceptedResponse) {
    if (!bodyShouldBePrinted()) {
      return EMPTY_STRING;
    }

    if (interceptedResponse.originalBody == null) {
      return logLines(EMPTY_RESPONSE_BODY, true);
    }

    final String encoding = interceptedResponse.headers != null
        ? interceptedResponse.headers.get("Content-encoding")
        : null;

    Buffer buffer = null;
    try (BufferedSource source = interceptedResponse.originalBody.source()) {
      source.request(Long.MAX_VALUE); // Buffer the entire body.

      buffer = source.getBuffer();
      if ("gzip".equals(encoding)) {
        final Buffer gzippedBuffer = buffer.clone();
        buffer.clear();
        buffer.writeAll(new GzipSource(gzippedBuffer));
      }
    } catch (IOException e) {
      logger.log(java.util.logging.Level.SEVERE, e.getMessage(), e);
    }

    if (buffer == null || buffer.size() == 0L) {
      return logLines(EMPTY_RESPONSE_BODY, true);
    }

    final boolean isPrintable = Util.isUtf8(buffer);
    if (isPrintable && buffer.size() > 0L) {
      final String printableBody = BodyFormatter
          .formattedBody(buffer.clone().readString(Charset.defaultCharset()));

      final String responseBody = LINE_SEPARATOR
          + BODY_TAG
          + LINE_SEPARATOR
          + printableBody;

      return logLines(responseBody.split(REGEX_LINE_SEPARATOR), true);
    } else {
      return logLines(OMITTED_RESPONSE, true);
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
        ? EMPTY_STRING
        : " - " + EXECUTION_TIME_TAG + interceptedResponse.chainMs + "ms";

    final String statusMessage = nonNull(interceptedResponse.message)
        ? interceptedResponse.message : EMPTY_STRING;

    final String log = (!isEmpty(segmentString)
        ? segmentString + " - "
        : EMPTY_STRING) + "is success : "
        + interceptedResponse.isSuccessful + receivedTags
        + DOUBLE_SEPARATOR
        + STATUS_CODE_TAG + interceptedResponse.code + " / " + statusMessage
        + DOUBLE_SEPARATOR
        + printHeaderIfLoggable(interceptedResponse.headers != null
        ? interceptedResponse.headers.toString() : EMPTY_STRING, isLoggable);
    return log.split(REGEX_LINE_SEPARATOR);
  }

  private static String slashSegments(List<String> segments) {
    if (isNull(segments) || segments.isEmpty()) {
      return EMPTY_STRING;
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
        : EMPTY_STRING;
  }

  private static String logLines(String[] lines, boolean withLineSize) {
    return logLines(lines, DEFAULT_LINE, withLineSize).toString();
  }

  private static StringBuilder logLines(String[] lines, String startingWith, boolean withLineSize) {
    StringBuilder sb = new StringBuilder();
    for (String line : lines) {
      if (isEmpty(line)) {
        sb.append(LINE_SEPARATOR).append(startingWith);
      } else {
        sb.append(logLine(startingWith, withLineSize, line));
      }
    }
    return sb;
  }

  private static StringBuilder logLine(String startingWith, boolean withLineSize, String line) {
    final int lineLength = line.length();
    final int maxLongSize = withLineSize
        ? loggerConfig.maxLineLength - startingWith.length()
        : lineLength;

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i <= lineLength / maxLongSize; i++) {
      final int start = i * maxLongSize;
      int end = (i + 1) * maxLongSize;
      end = Math.min(end, line.length());

      if (start != end) {
        // This condition check handles very rare occasion when multiline string exactly matches
        // max line length, in that case unnecessary empty line will be printed
        sb.append(LINE_SEPARATOR).append(startingWith).append(line, start, end);
      }
    }
    return sb;
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
    String tag = EMPTY_STRING;
    if (headers.length > 1) {
      for (int i = 0; i < headers.length; i++) {
        if (i == 0) {
          tag = CORNER_UP;
        } else if (i == headers.length - 1) {
          tag = CORNER_BOTTOM;
        } else {
          tag = CENTER_LINE;
        }
        builder.append(tag).append(headers[i]).append(LINE_SEPARATOR);
      }
    } else {
      for (String item : headers) {
        builder.append(tag).append(item).append(LINE_SEPARATOR);
      }
    }
    return builder.toString();
  }

  private static String drawHorizontalLine(final int length) {
    final char[] chars = new char[Math.max(length, 0)];
    Arrays.fill(chars, Printer.HORIZONTAL_LINE);
    return new String(chars);
  }

}
