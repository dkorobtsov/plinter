package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.internal.Util.ACCEPT;
import static com.dkorobtsov.logging.internal.Util.APPLICATION_JSON;
import static com.dkorobtsov.logging.internal.Util.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

import com.dkorobtsov.logging.internal.ClientPrintingExecutor;
import com.dkorobtsov.logging.internal.HttpStatus;
import com.dkorobtsov.logging.internal.InterceptedMediaType;
import com.dkorobtsov.logging.internal.InterceptedRequest;
import com.dkorobtsov.logging.internal.InterceptedRequestBody;
import com.dkorobtsov.logging.internal.InterceptedResponse;
import com.dkorobtsov.logging.internal.Util;
import com.dkorobtsov.logging.utils.TestLogger;
import com.dkorobtsov.logging.utils.TestUtil;
import java.net.MalformedURLException;
import java.net.URL;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class PrinterTest extends BaseTest {

  private static final String RESIZABLE_BODY = "{name: \"" + TestUtil.randomText(500) + "\"}";
  private static final String SIMPLE_JSON = "{name: \"John\", age: 31, city: \"New York\"}";
  private static final String TEST_URL = "http://google.com/api/test/";
  private static final int MAX_LINE_LENGTH = 120;
  private static final int TRAILING_SPACES = 3;

  @Test
  public void printRequest_hasNoPrintableBody() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.loggerOutput(false))
        .contains("Omitted request body");
  }

  @Test
  public void printRequest_hasPrintableBody() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .method("PUT", InterceptedRequestBody
            .create(InterceptedMediaType.parse(APPLICATION_JSON), SIMPLE_JSON))
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.formattedOutput())
        .contains(""
            + "  Body: \n"
            + "  { \n"
            + "     \"city\": \"New York\", \n"
            + "     \"name\": \"John\", \n"
            + "     \"age\": 31 \n"
            + "  } "
        );
  }

  @Test
  @Parameters({
      "80", "110", "140", "180"
  })
  public void printRequest_outputResizing(String maxLineLength) {
    int maxLength = Integer.parseInt(maxLineLength);
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .method("PUT", InterceptedRequestBody
            .create(InterceptedMediaType.parse(APPLICATION_JSON), RESIZABLE_BODY))
        .addHeader("LongHeader", TestUtil.randomText(500))
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printRequest(
            defaultLoggerConfig(testLogger, false, maxLength),
            request);

    testLogger
        .loggerOutput(true)
        .stream()
        .filter(
            it -> it.startsWith("| Thread:")
                || it.startsWith("  LongHeader:")
                || it.startsWith("  {\"name\":")
                || it.startsWith("┌")
                || it.startsWith("├")
                || it.startsWith("└"))
        .forEach(
            it -> assertThat(it.length())
                .isEqualTo(maxLength + TRAILING_SPACES));
  }

  @Test
  public void printRequest_putMethod() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .method("PUT", InterceptedRequestBody
            .create(InterceptedMediaType.parse(APPLICATION_JSON), SIMPLE_JSON))
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.loggerOutput(false)).contains("Method: @PUT");
  }

  @Test
  public void printRequest_postMethod() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .method("POST", InterceptedRequestBody
            .create(InterceptedMediaType.parse(APPLICATION_JSON), SIMPLE_JSON))
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.loggerOutput(false)).contains("Method: @POST");
  }

  @Test
  public void printRequest_deleteMethod() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .method("DELETE", InterceptedRequestBody
            .create(InterceptedMediaType.parse(APPLICATION_JSON), SIMPLE_JSON))
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.loggerOutput(false)).contains("Method: @DELETE");
  }

  @Test
  public void printRequest_patchMethod() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .method("PATCH", InterceptedRequestBody
            .create(InterceptedMediaType.parse(APPLICATION_JSON), SIMPLE_JSON))
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.loggerOutput(false)).contains("Method: @PATCH");
  }

  @Test
  public void printRequest_traceMethod() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .method("TRACE", null)
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.loggerOutput(false)).contains("Method: @TRACE");
  }

  @Test
  public void printRequest_optionsMethod() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .method("OPTIONS", null)
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.loggerOutput(false)).contains("Method: @OPTIONS");
  }

  @Test
  public void printRequest_headMethod() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .method("HEAD", null)
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.loggerOutput(false)).contains("Method: @HEAD");
  }

  @Test
  public void printRequest_connectMethod() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .method("CONNECT", null)
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.loggerOutput(false)).contains("Method: @CONNECT");
  }

  @Test
  public void printRequest_urlShouldBePrintedInSingleLine() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final String randomSeed = TestUtil.randomText(100);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .url(TEST_URL + randomSeed)
        .build();

    ClientPrintingExecutor
        .printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.loggerOutput(false)).contains("URL: " + TEST_URL + randomSeed);
  }

  @Test
  public void printRequest_oneHeader() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .get()
        .addHeader(CONTENT_TYPE, APPLICATION_JSON)
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.loggerOutput(false)).contains("Headers:");
    assertThat(testLogger.loggerOutput(false)).contains("Content-Type: application/json");
  }

  @Test
  public void printRequest_twoHeaders() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .get()
        .addHeader(CONTENT_TYPE, APPLICATION_JSON)
        .addHeader(ACCEPT, APPLICATION_JSON)
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.loggerOutput(false)).contains("Headers:");
    assertThat(testLogger.loggerOutput(false)).contains("┌ Content-Type: application/json");
    assertThat(testLogger.loggerOutput(false)).contains("└ Accept: application/json");
  }

  @Test
  public void printRequest_threeHeadersWithMultiline() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .get()
        .addHeader(CONTENT_TYPE, APPLICATION_JSON)
        .addHeader("LongHeader", "unogetabukuloqidijibiqodenofigubabuxunetar"
            + "obukisikeyeguburehuquyogoquxosevutonasedigutiwavepihawiquhidanirotiguwuwac"
            + "omenubafacufufuhujajajehanacirepexigewuwiwucifayumokawikoyipofazejixekalun"
            + "uguxumucaraputoceqaxeyasegipulicikev")
        .addHeader(ACCEPT, APPLICATION_JSON)
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.loggerOutput(false)).contains("Headers:");
    assertThat(testLogger.loggerOutput(false)).contains("┌ Content-Type: application/json");
    assertThat(testLogger.loggerOutput(false)).contains(
        "├ LongHeader: unogetabukuloqidijibiqodenofigubabuxunetarobukisikeyeguburehuquyogoquxosevutonasedigutiwavepihawiquhidanir");
    assertThat(testLogger.loggerOutput(false)).contains(
        "otiguwuwacomenubafacufufuhujajajehanacirepexigewuwiwucifayumokawikoyipofazejixekalunuguxumucaraputoceqaxeyasegipulicikev");
    assertThat(testLogger.loggerOutput(false)).contains("└ Accept: application/json");
  }

  @Test
  public void printRequest_generalFormatting() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .get()
        .addHeader(CONTENT_TYPE, APPLICATION_JSON)
        .addHeader(ACCEPT, APPLICATION_JSON)
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printRequest(LoggerConfig.builder()
            .logger(testLogger)
            .maxLineLength(80)
            .build(), request);

    assertThat(testLogger.formattedOutput())
        .isEqualTo(""
            + "┌────── Request ────────────────────────────────────────────────────────────────── \n"
            + "  URL: http://google.com/api/test/\n"
            + " \n"
            + "  Method: @GET \n"
            + "   \n"
            + "  Headers: \n"
            + "  ┌ Content-Type: application/json \n"
            + "  └ Accept: application/json \n"
            + "   \n"
            + "  Omitted request body \n"
            + "└───────────────────────────────────────────────────────────────────────────────── \n");
  }

  @Test
  public void printResponse_elapsedTime() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedResponse response = InterceptedResponse.builder()
        .chainMs(10)
        .code(200)
        .isSuccessful(true)
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printResponse(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), response);

    assertThat(testLogger.formattedOutput()).contains("Execution time: 10ms");
  }

  @Test
  public void printResponse_isSuccess() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedResponse response = InterceptedResponse.builder()
        .chainMs(10)
        .code(200)
        .message(HttpStatus.OK.getMessage())
        .isSuccessful(true)
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printResponse(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), response);

    assertThat(testLogger.formattedOutput()).contains("is success : true");
    assertThat(testLogger.formattedOutput()).contains("Status Code: 200 / OK");
  }

  @Test
  public void printResponse_isFail() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedResponse response = InterceptedResponse.builder()
        .chainMs(10)
        .code(504)
        .message(HttpStatus.GATEWAY_TIMEOUT.getMessage())
        .isSuccessful(false)
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printResponse(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), response);

    assertThat(testLogger.formattedOutput()).contains("is success : false");
    assertThat(testLogger.formattedOutput()).contains("Status Code: 504 / GATEWAY_TIMEOUT");
  }

  @Test
  public void printResponse_hasNoPrintableBody() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedResponse response = InterceptedResponse.builder()
        .code(200)
        .message(HttpStatus.OK.getMessage())
        .isSuccessful(true)
        .url(TEST_URL)
        .build();

    ClientPrintingExecutor
        .printResponse(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), response);

    assertThat(testLogger.loggerOutput(false))
        .contains("Omitted response body");
  }

  @Test
  public void printResponse_hasPrintableBody() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedResponse response = InterceptedResponse.builder()
        .chainMs(10)
        .code(200)
        .isSuccessful(true)
        .contentType(InterceptedMediaType.parse(APPLICATION_JSON))
        .message(HttpStatus.OK.getMessage())
        .url(TEST_URL)
        .originalBody(SIMPLE_JSON.getBytes())
        .hasPrintableBody(true)
        .build();
    ClientPrintingExecutor
        .printResponse(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), response);

    assertThat(testLogger.formattedOutput())
        .contains(""
            + "  Body: \n"
            + "  { \n"
            + "     \"city\": \"New York\", \n"
            + "     \"name\": \"John\", \n"
            + "     \"age\": 31 \n"
            + "  } "
        );
  }

  @Test
  public void printResponse_segmentsPrinting() throws MalformedURLException {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedResponse response = InterceptedResponse.builder()
        .code(200)
        .message(HttpStatus.OK.getMessage())
        .isSuccessful(true)
        .url(TEST_URL)
        .segmentList(Util.encodedPathSegments(new URL(TEST_URL)))
        .build();

    ClientPrintingExecutor
        .printResponse(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), response);

    assertThat(testLogger.loggerOutput(true))
        .contains("  /api/test/ - is success : true ");
  }

  @Test
  public void printResponse_urlShouldBePrintedInSingleLine() throws MalformedURLException {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final String randomSeed = TestUtil.randomText(110);

    final InterceptedResponse response = InterceptedResponse.builder()
        .code(200)
        .message(HttpStatus.OK.getMessage())
        .isSuccessful(true)
        .url(TEST_URL + randomSeed)
        .segmentList(Util.encodedPathSegments(new URL(TEST_URL + randomSeed)))
        .build();

    ClientPrintingExecutor
        .printResponse(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), response);

    assertThat(testLogger.loggerOutput(false)).contains("URL: " + TEST_URL + randomSeed);
    assertThat(testLogger.loggerOutput(true)).contains("   - is success : true ");
  }

  @Test
  @Parameters({
      "80", "110", "140", "180"
  })
  public void printResponse_outputResizing(String maxLineLength) throws MalformedURLException {
    final int maxLength = Integer.parseInt(maxLineLength);
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedResponse response = InterceptedResponse.builder()
        .chainMs(10)
        .code(200)
        .isSuccessful(true)
        .contentType(InterceptedMediaType.parse(APPLICATION_JSON))
        .header("LongHeader: " + TestUtil.randomText(500))
        .message(HttpStatus.OK.getMessage())
        .url(TEST_URL)
        .originalBody(RESIZABLE_BODY.getBytes())
        .hasPrintableBody(true)
        .segmentList(Util.encodedPathSegments(new URL(TEST_URL)))
        .build();

    ClientPrintingExecutor
        .printResponse(
            defaultLoggerConfig(testLogger, false, maxLength),
            response);

    testLogger
        .loggerOutput(true)
        .stream()
        .filter(
            it -> it.startsWith("| Thread:")
                || it.startsWith("  LongHeader:")
                || it.startsWith("  {\"name\":")
                || it.startsWith("┌")
                || it.startsWith("├")
                || it.startsWith("└"))
        .forEach(
            it -> assertThat(it.length())
                .isEqualTo(maxLength + TRAILING_SPACES));
  }

  @Test
  public void printResponse_generalFormatting() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedResponse response = InterceptedResponse.builder()
        .chainMs(10)
        .code(200)
        .isSuccessful(true)
        .contentType(InterceptedMediaType.parse(APPLICATION_JSON))
        .message(HttpStatus.OK.getMessage())
        .header("Content-Type: application/json\nAccept: application/json")
        .url(TEST_URL)
        .originalBody(SIMPLE_JSON.getBytes())
        .hasPrintableBody(true)
        .build();

    ClientPrintingExecutor
        .printResponse(LoggerConfig.builder()
            .logger(testLogger)
            .maxLineLength(80)
            .build(), response);

    assertThat(testLogger.formattedOutput())
        .isEqualTo(""
            + "┌────── Response ───────────────────────────────────────────────────────────────── \n"
            + "  URL: http://google.com/api/test/\n"
            + " \n"
            + "  is success : true - Execution time: 10ms \n"
            + "   \n"
            + "  Status Code: 200 / OK \n"
            + "   \n"
            + "  Headers: \n"
            + "  ┌ Content-Type: application/json \n"
            + "  └ Accept: application/json \n"
            + "   \n"
            + "  Body: \n"
            + "  { \n"
            + "     \"city\": \"New York\", \n"
            + "     \"name\": \"John\", \n"
            + "     \"age\": 31 \n"
            + "  } \n"
            + "└───────────────────────────────────────────────────────────────────────────────── \n");
  }

}
