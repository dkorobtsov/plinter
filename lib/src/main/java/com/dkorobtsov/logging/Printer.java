package com.dkorobtsov.logging;


import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import okhttp3.Request;
import okio.Buffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

/**
 * @author ihsan on 09/02/2017.
 */
class Printer {

  private static final int JSON_INDENT = 3;

  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String REGEX_LINE_SEPARATOR = "\r?\n";
  private static final String DOUBLE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR;

  private static final String[] OMITTED_RESPONSE = {"", "Omitted response body"};
  private static final String[] OMITTED_REQUEST = {"", "Omitted request body"};

  private static final String N = "\n";
  private static final String T = "\t";
  private static final String REQUEST_UP_LINE = "┌────── Request ────────────────────────────────────────────────────────────────────────";
  private static final String END_LINE = "└───────────────────────────────────────────────────────────────────────────────────────";
  private static final String RESPONSE_UP_LINE = "┌────── Response ───────────────────────────────────────────────────────────────────────";
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
  private static final String EMPTY_LINE = "";

  Printer() {
    throw new UnsupportedOperationException();
  }

  private static boolean isEmpty(String line) {
    return TextUtils.isEmpty(line) || N.equals(line) || T.equals(line) || TextUtils
        .isEmpty(line.trim());
  }

  static void printJsonRequest(LogWriter logWriter, Level level, Request request) {
    logWriter.log(REQUEST_UP_LINE);

    logLines(new String[]{URL_TAG + request.url()}, logWriter, false);
    logLines(getRequest(request, level), logWriter, true);

    String requestBody = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + bodyToString(request);
    if (level == Level.BASIC || level == Level.BODY) {
      logLines(requestBody.split(REGEX_LINE_SEPARATOR), logWriter, true);
    }

    logWriter.log(END_LINE);
  }

  static void printJsonResponse(LogWriter logWriter, Level level, ResponseDetails responseDetails) {
    logWriter.log(RESPONSE_UP_LINE);

    final String[] urlLine = {URL_TAG + responseDetails.url, EMPTY_LINE};
    logLines(urlLine, logWriter, true);

    final String[] response = getResponse(level, responseDetails);
    logLines(response, logWriter, true);

    final String responseBody =
        LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + formattedBody(responseDetails.bodyString);
    if (level == Level.BASIC || level == Level.BODY) {
      logLines(responseBody.split(REGEX_LINE_SEPARATOR), logWriter, true);
    }

    logWriter.log(END_LINE);
  }

  static void printFileRequest(LogWriter logWriter, Level level, Request request) {
    logWriter.log(REQUEST_UP_LINE);

    logLines(new String[]{URL_TAG + request.url()}, logWriter,
        false);
    logLines(getRequest(request, level), logWriter,
        true);

    if (level == Level.BASIC || level == Level.BODY) {
      logLines(OMITTED_REQUEST, logWriter, true);
    }

    logWriter.log(END_LINE);

  }

  static void printFileResponse(LogWriter logWriter, Level level, ResponseDetails responseDetails) {

    logWriter.log(RESPONSE_UP_LINE);

    final String[] urlLine = {URL_TAG + responseDetails.url, EMPTY_LINE};
    logLines(urlLine, logWriter, true);

    logLines(getResponse(level, responseDetails), logWriter, true);

    if (level == Level.BASIC || level == Level.BODY) {
      logLines(OMITTED_RESPONSE, logWriter, true);
    }

    logWriter.log(END_LINE);
  }

  private static String[] getRequest(Request request, Level level) {
    String header = request.headers().toString();
    boolean isLoggable = level == Level.HEADERS || level == Level.BASIC;
    String log = METHOD_TAG + request.method()
        + DOUBLE_SEPARATOR
        + printHeaderIfLoggable(header, isLoggable);
    return log.split(REGEX_LINE_SEPARATOR);
  }

  private static String[] getResponse(Level level, ResponseDetails responseDetails) {
    final boolean isLoggable = level == Level.HEADERS || level == Level.BASIC;
    final String segmentString = slashSegments(responseDetails.segmentList);
    final String log = (!TextUtils.isEmpty(segmentString) ? segmentString + " - "
        : "") + "is success : "
        + responseDetails.isSuccessful + " - " + RECEIVED_TAG + responseDetails.chainMs + "ms"
        + DOUBLE_SEPARATOR
        + STATUS_CODE_TAG + responseDetails.code + " / " + responseDetails.message
        + DOUBLE_SEPARATOR
        + printHeaderIfLoggable(responseDetails.header, isLoggable);
    return log.split(REGEX_LINE_SEPARATOR);
  }

  private static String printHeaderIfLoggable(String header, boolean loggable) {
    return !isEmpty(header) && loggable ? HEADERS_TAG + LINE_SEPARATOR + dotHeaders(header) : "";
  }

  private static String slashSegments(List<String> segments) {
    StringBuilder segmentString = new StringBuilder();
    for (String segment : segments) {
      segmentString.append("/").append(segment);
    }
    return segmentString.toString();
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
        builder.append(tag).append(headers[i]).append("\n");
      }
    } else {
      for (String item : headers) {
        builder.append(tag).append(item).append("\n");
      }
    }
    return builder.toString();
  }

  private static void logLines(String[] lines, LogWriter logger, boolean withLineSize) {
    for (String line : lines) {
      int lineLength = line.length();
      int maxLongSize = withLineSize ? 110 : lineLength;
      for (int i = 0; i <= lineLength / maxLongSize; i++) {
        int start = i * maxLongSize;
        int end = (i + 1) * maxLongSize;
        end = end > line.length() ? line.length() : end;
        logger.log(DEFAULT_LINE + line.substring(start, end));
      }
    }
  }

  private static String bodyToString(final Request request) {
    final Request copy = request.newBuilder().build();
    try (final Buffer buffer = new Buffer()) {
      if (copy.body() == null) {
        return "";
      }
      copy.body().writeTo(buffer);
      return formattedBody(buffer.readUtf8());
    } catch (final IOException e) {
      return "{\"err\": \"" + e.getMessage() + "\"}";
    }
  }

  static String formattedBody(final String msg) {
    String message;
    try {
      if (msg.startsWith("{")) {
        message = new JSONObject(msg).toString(JSON_INDENT);
      } else if (msg.startsWith("[")) {
        message = new JSONArray(msg).toString(JSON_INDENT);
      } else if (msg.startsWith("<?xml")) {
        message = formatAsXml(msg);
      } else {
        message = msg;
      }
    } catch (JSONException e) {
      message = msg;
    }
    return message;
  }

  private static String formatAsXml(String xml) {
    try {
      final InputSource src = new InputSource(new StringReader(xml));
      final Node document = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder().parse(src).getDocumentElement();
      final boolean keepDeclaration = xml.startsWith("<?xml");

      final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
      final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
      final LSSerializer writer = impl.createLSSerializer();

      writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
      writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);

      return writer.writeToString(document);
    } catch (Exception e) {
      //If failed to parse - just showing as is.
      return xml;
    }
  }

}
