package io.github.dkorobtsov.tests;

import io.github.dkorobtsov.plinter.core.LoggerConfig;
import io.github.dkorobtsov.plinter.core.LoggingFormat;
import io.github.dkorobtsov.plinter.core.internal.ClientPrintingExecutor;
import io.github.dkorobtsov.plinter.core.internal.InterceptedMediaType;
import io.github.dkorobtsov.plinter.core.internal.InterceptedRequest;
import io.github.dkorobtsov.plinter.core.internal.InterceptedRequestBody;
import io.github.dkorobtsov.tests.utils.TestLogger;
import io.github.dkorobtsov.tests.utils.TestUtil;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.github.dkorobtsov.plinter.core.internal.Util.ACCEPT;
import static io.github.dkorobtsov.plinter.core.internal.Util.APPLICATION_JSON;
import static io.github.dkorobtsov.plinter.core.internal.Util.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for requests printing validation.
 */
@RunWith(JUnitParamsRunner.class)
@SuppressWarnings({"Indentation", "PMD"})
public class RequestsPrintingTest extends BaseTest {

  private static final String RESIZABLE_BODY = "{name: \"" + TestUtil.randomText(500) + "\"}";
  private static final String SIMPLE_JSON = "{name: \"John\", age: 31, city: \"New York\"}";
  private static final String TEST_URL = "http://google.com/api/test/";
  private static final int MAX_LINE_LENGTH = 120;

  @Test
  public void printRequest_hasNoPrintableBody() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
      .url(TEST_URL)
      .build();

    ClientPrintingExecutor
      .printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.loggerOutput(false))
      .contains("Empty request body");
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
        + "  Body:\n"
        + "  {\n"
        + "     \"city\": \"New York\",\n"
        + "     \"name\": \"John\",\n"
        + "     \"age\": 31\n"
        + "  }"
      );
  }

  @Test
  @Parameters(method = "validMaxLineSizes")
  @SuppressWarnings("BooleanExpressionComplexity")
  public void printRequest_outputResizing(String maxLineLength) {
    final int maxLength = Integer.parseInt(maxLineLength);
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
      .filter(it -> !it.isEmpty())
      .filter(
        it -> it.startsWith("| Thread:")
          || it.startsWith("  LongHeader:")
          || it.startsWith("  {\"name\":")
          || it.charAt(0) == '┌'
          || it.charAt(0) == '├'
          || it.charAt(0) == '└')
      .map(String::stripTrailing)
      .forEach(
        it -> assertThat(it).hasSize(maxLength));
  }

  String[] httpMethods() {
    return new String[]{
      "PUT, Method: @PUT",
      "POST, Method: @POST",
      "DELETE, Method: @DELETE",
      "PATCH, Method: @PATCH",
      "TRACE, Method: @TRACE",
      "OPTIONS, Method: @OPTIONS",
      "HEAD, Method: @HEAD",
      "CONNECT, Method: @CONNECT"
    };
  }

  @Test
  @Parameters(method = "httpMethods")
  public void printRequest_withHttpMethod_shouldPrintCorrectLogMessage(String method, String expectedLogMessage) {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
      .method(method, getRequestBodyForMethod(method))
      .url(TEST_URL)
      .build();

    ClientPrintingExecutor.printRequest(defaultLoggerConfig(testLogger, false, MAX_LINE_LENGTH), request);

    assertThat(testLogger.loggerOutput(false)).contains(expectedLogMessage);
  }

  private InterceptedRequestBody getRequestBodyForMethod(String httpMethod) {
    switch (httpMethod) {
      case "PUT":
      case "PATCH":
      case "POST":
        return InterceptedRequestBody.create(InterceptedMediaType.parse(APPLICATION_JSON), SIMPLE_JSON);
      default:
        return null;
    }
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

    assertThat(testLogger.loggerOutput(false))
      .contains("URL: " + TEST_URL + randomSeed);
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

    assertThat(testLogger.loggerOutput(false))
      .contains("Headers:")
      .contains("Content-Type: application/json");
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

    assertThat(testLogger.loggerOutput(false))
      .contains("Headers:")
      .contains("┌ Content-Type: application/json")
      .contains("└ Accept: application/json");
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

    assertThat(testLogger.loggerOutput(false))
      .contains("Headers:")
      .contains("┌ Content-Type: application/json")
      .contains("├ LongHeader: unogetabukuloqidijibiqodenofigubabuxunetaro"
        + "bukisikeyeguburehuquyogoquxosevutonasedigutiwavepihawiquhidan")
      .contains("irotiguwuwacomenubafacufufuhujajajehanacirepexigewuwiwuci"
        + "fayumokawikoyipofazejixekalunuguxumucaraputoceqaxeyasegipulic")
      .contains("└ Accept: application/json");
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
        + "\n"
        + "┌────── Request ────────────────────────────────────────────────────────────────\n"
        + "  URL: http://google.com/api/test/\n"
        + "  \n"
        + "  Method: @GET\n"
        + "  \n"
        + "  Headers:\n"
        + "  ┌ Content-Type: application/json\n"
        + "  └ Accept: application/json\n"
        + "  \n"
        + "  Empty request body\n"
        + "└─────────────────────────────────────────────────────────────────────────────── \n");
  }

}
