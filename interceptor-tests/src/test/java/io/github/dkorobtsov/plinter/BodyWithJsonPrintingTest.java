package io.github.dkorobtsov.plinter;

import io.github.dkorobtsov.plinter.core.internal.Util;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.dkorobtsov.plinter.core.internal.Util.APPLICATION_JSON;
import static io.github.dkorobtsov.plinter.core.internal.Util.APPLICATION_ZIP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests validating that event body containing JSON document is handled properly.
 */
@RunWith(JUnitParamsRunner.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class BodyWithJsonPrintingTest extends BaseTest {

  private static final String SIMPLE_JSON = "{name: \"John\", age: 31, city: \"New York\"}";
  private static final String JSON_ARRAY = "[{\"test1\": \"test1\"}, {\"test2\": \"test2\"}]";
  private static final String PREFORMATTED_JSON_BODY = ""
      + "  {\n"
      + "    \"id\": 431169,\n"
      + "    \"category\": {\n"
      + "      \"id\": 0,\n"
      + "      \"name\": \"string\"\n"
      + "    },\n"
      + "    \"name\": \"doggie\",\n"
      + "    \"photoUrls\": [\n"
      + "      \"string\"\n"
      + "    ],\n"
      + "    \"tags\": [\n"
      + "      {\n"
      + "        \"id\": 0,\n"
      + "        \"name\": \"string\"\n"
      + "      }\n"
      + "    ],\n"
      + "    \"status\": \"available\"\n"
      + "  }";

  private static final String MALFORMED_JSON_BODY = ""
      + "  {\n"
      + "    \"id\": 431169,\n"
      + "    \"category\": {\n"
      + "      \"id\": 0,\n"
      + "      \"name\": \"string\"\n"
      + "    \"name\": \"doggie\",\n"
      + "    \"photoUrls\": [\n"
      + "      \"string\"\n"
      + "    \"tags\": [\n"
      + "      {\n"
      + "        \"id\": 0,\n"
      + "        \"name\": \"string\"\n"
      + "      }\n"
      + "    ],\n"
      + "    \"status\": \"available\"\n"
      + "  }";

  private static final String MALFORMED_JSON_STARTING_WITH_INVALID_CHAR = "? \"test\" : \"test1\"}";


  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_simpleJsonRequest(String interceptor, boolean withExecutor,
                                             boolean logByLine) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        SIMPLE_JSON, APPLICATION_JSON, true, logByLine)
        .stream()
        .map(String::stripTrailing)
        .collect(Collectors.toList());

    assertThat(loggerOutput)
        .contains("     \"city\": \"New York\",");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_simpleJsonResponse(String interceptor, boolean withExecutor,
                                              boolean logByLine) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        SIMPLE_JSON, APPLICATION_JSON, true, logByLine)
        .stream()
        .map(String::stripTrailing)
        .collect(Collectors.toList());

    assertThat(loggerOutput)
        .contains("     \"city\": \"New York\",");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_jsonArrayResponse(String interceptor, boolean withExecutor,
                                             boolean logByLine) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        JSON_ARRAY, APPLICATION_JSON, false, logByLine);

    assertThat(loggerOutput)
        .contains("{\"test1\": \"test1\"},")
        .contains("{\"test2\": \"test2\"}");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_preformattedJsonRequest(String interceptor, boolean withExecutor,
                                                   boolean logByLine) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        PREFORMATTED_JSON_BODY, APPLICATION_JSON, true, logByLine)
        .stream()
        .map(String::stripTrailing)
        .collect(Collectors.toList());

    assertThat(loggerOutput)
        .containsSequence("     \"name\": \"doggie\",");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_preformattedJsonResponse(String interceptor, boolean withExecutor,
                                                    boolean logByLine) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        PREFORMATTED_JSON_BODY, APPLICATION_JSON, true, logByLine)
        .stream()
        .map(String::stripTrailing)
        .collect(Collectors.toList());

    assertThat(loggerOutput)
        .contains("     \"name\": \"doggie\",");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_malformedJsonRequest(String interceptor, boolean withExecutor,
                                                boolean logByLine) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        MALFORMED_JSON_BODY, APPLICATION_JSON, false, logByLine)
        .stream()
        .map(String::stripTrailing)
        .collect(Collectors.toList());

    final List<String> filteredOutput = loggerOutput
        .stream()
        .filter(it -> it.startsWith("\"status\": \"available\""))
        .collect(Collectors.toList());

    assertThat(filteredOutput)
        .withFailMessage("Interceptor should be able to handle malformed json request body.")
        .isNotEmpty();
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_jsonArrayRequest(String interceptor, boolean withExecutor,
                                            boolean logByLine) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        JSON_ARRAY, APPLICATION_JSON, false, logByLine);

    assertThat(loggerOutput)
        .contains("{\"test1\": \"test1\"},")
        .contains("{\"test2\": \"test2\"}");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_JsonRequestWithInvalidChar(String interceptor, boolean withExecutor,
                                                      boolean logByLine) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        MALFORMED_JSON_STARTING_WITH_INVALID_CHAR, APPLICATION_JSON, false, logByLine);

    assertTrue("Interceptor should be able to log malformed json request body.",
        loggerOutput.contains("? \"test\" : \"test1\"}"));
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_malformedJsonResponse(String interceptor, boolean withExecutor,
                                                 boolean logByLine) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        MALFORMED_JSON_BODY, APPLICATION_JSON, false, logByLine);

    final List<String> filteredOutput = loggerOutput
        .stream()
        .filter(it -> it.startsWith("\"status\": \"available\""))
        .collect(Collectors.toList());

    assertThat(filteredOutput)
        .withFailMessage("Interceptor should be able to handle malformed json request body.")
        .isNotEmpty();
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_JsonResponseWithInvalidChar(String interceptor, boolean withExecutor,
                                                       boolean logByLine) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        MALFORMED_JSON_STARTING_WITH_INVALID_CHAR, APPLICATION_JSON, false, logByLine);

    assertTrue("Interceptor should be able to log malformed json request body.",
        loggerOutput.contains("? \"test\" : \"test1\"}"));
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_fileRequest(String interceptor, boolean withExecutor,
                                       boolean logByLine) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        Util.gzip(PREFORMATTED_JSON_BODY).readString(Charset.defaultCharset()),
        APPLICATION_ZIP, true, logByLine)
        .stream()
        .map(String::stripTrailing)
        .collect(Collectors.toList());

    assertThat(loggerOutput)
        .contains("  Omitted request body");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_fileResponse(String interceptor, boolean withExecutor,
                                        boolean logByLine) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        Util.gzip(PREFORMATTED_JSON_BODY).readString(Charset.defaultCharset()),
        APPLICATION_ZIP, true, logByLine)
        .stream()
        .map(String::stripTrailing)
        .collect(Collectors.toList());

    assertThat(loggerOutput)
        .contains("  Omitted response body");
  }

}

