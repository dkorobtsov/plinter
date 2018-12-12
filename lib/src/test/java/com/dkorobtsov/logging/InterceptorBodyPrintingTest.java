package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.internal.Util.APPLICATION_JSON;
import static com.dkorobtsov.logging.internal.Util.APPLICATION_XML;
import static com.dkorobtsov.logging.internal.Util.APPLICATION_ZIP;
import static com.dkorobtsov.logging.internal.Util.TEXT_HTML;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class InterceptorBodyPrintingTest extends BaseTest {

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


  private static final String XML_BODY =
      "<?xml version=\"1.0\" encoding=\"UTF-16\"?>"
          + "<mammals>"
          + "<animal id=\"0\" species=\"Capra hircus\">Goat</animal>"
          + "<animal id=\"1\" species=\"Panthera pardus\">Leopard</animal>"
          + "<animal id=\"2\" species=\"Equus zebra\">Zebra</animal> "
          + "</mammals>";

  private static final String MALFORMED_XML_BODY =
      "<?xml version=\"1.0\" encoding=\"UTF-16\"?>"
          + "<mammals>"
          + "<animal id=\"0\" species=\"Capra hircus\">Goat</animal>"
          + "animal id=\"1\" species=\"Panthera pardus\">Leopard</animal>"
          + "<animal id=\"2\" species=\"Equus zebra\">Zebra</animal> "
          + "</mammals>";

  private static final String HTML_BODY = ""
      + "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>"
      + "<title>Error 404 Not Found</title></head><body>"
      + "<div style=\"font-family:Arial,Helvetica,sans-serif;\"><h2>HTTP ERROR 404</h2>"
      + "<pre>Not Found</pre></div><tr><th></th><th></th><th></th><th></th></tr></body></html>";

  private static final String MALFORMED_HTML_BODY = ""
      + "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>"
      + "<title>Error 404 Not Found</title></head><body>"
      + "<div style=\"font-family:Arial,Helvetica,sans-serif;\"><h2>HTTP ERROR 404</h2>"
      + "<pre>Not Found</pre></div><tr><th></th><th><th></th><th></th></tr></body></html>";

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_simpleJsonRequest(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        SIMPLE_JSON, APPLICATION_JSON, true);

    assertThat(loggerOutput).contains("     \"city\": \"New York\", ");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_simpleJsonResponse(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        SIMPLE_JSON, APPLICATION_JSON, true);

    assertThat(loggerOutput).contains("     \"city\": \"New York\", ");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_jsonArrayRequest(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        JSON_ARRAY, APPLICATION_JSON, false);

    assertThat(loggerOutput).contains("{\"test1\": \"test1\"},");
    assertThat(loggerOutput).contains("{\"test2\": \"test2\"}");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_jsonArrayResponse(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        JSON_ARRAY, APPLICATION_JSON, false);

    assertThat(loggerOutput).contains("{\"test1\": \"test1\"},");
    assertThat(loggerOutput).contains("{\"test2\": \"test2\"}");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_preformattedJsonRequest(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        PREFORMATTED_JSON_BODY, APPLICATION_JSON, true);

    assertThat(loggerOutput).containsSequence("     \"name\": \"doggie\", ");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_preformattedJsonResponse(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        PREFORMATTED_JSON_BODY, APPLICATION_JSON, true);

    assertThat(loggerOutput).containsSequence("     \"name\": \"doggie\", ");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_malformedJsonRequest(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        MALFORMED_JSON_BODY, APPLICATION_JSON, false);

    loggerOutput
        .stream()
        .filter(it ->
            it.startsWith("\"status\": \"available\""))
        .findFirst()
        .orElseThrow(() ->
            new AssertionError(
                "Interceptor should be able to handle malformed json request body."));
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_JsonRequestWithInvalidChar(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        MALFORMED_JSON_STARTING_WITH_INVALID_CHAR, APPLICATION_JSON, false);

    assertTrue("Interceptor should be able to log malformed json request body.",
        loggerOutput.contains("? \"test\" : \"test1\"}"));
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_malformedJsonResponse(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        MALFORMED_JSON_BODY, APPLICATION_JSON, false);

    loggerOutput
        .stream()
        .filter(it ->
            it.startsWith("\"status\": \"available\""))
        .findFirst()
        .orElseThrow(() ->
            new AssertionError(
                "Interceptor should be able to handle malformed json response body."));
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_JsonResponseWithInvalidChar(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        MALFORMED_JSON_STARTING_WITH_INVALID_CHAR, APPLICATION_JSON, false);

    assertTrue("Interceptor should be able to log malformed json request body.",
        loggerOutput.contains("? \"test\" : \"test1\"}"));
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_htmlRequest(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        HTML_BODY, TEXT_HTML, false);

    assertThat(loggerOutput).contains("<title>Error 404 Not Found</title>");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_htmlResponse(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        HTML_BODY, "text/html", false);

    assertThat(loggerOutput).contains("<title>Error 404 Not Found</title>");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_malformedHtmlRequest(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        MALFORMED_HTML_BODY, TEXT_HTML, false);

    loggerOutput
        .stream()
        .filter(it ->
            it.startsWith(
                "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>"))
        .findFirst()
        .orElseThrow(() ->
            new AssertionError("Interceptor should be able to handle html request body."));
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_malformedHtmlResponse(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        MALFORMED_HTML_BODY, "text/html", false);

    loggerOutput
        .stream()
        .filter(it ->
            it.startsWith(
                "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>"))
        .findFirst()
        .orElseThrow(() ->
            new AssertionError("Interceptor should be able to handle html response body."));
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_xmlRequest(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        XML_BODY, APPLICATION_XML, false);

    assertThat(loggerOutput).contains("<?xml version=\"1.0\" encoding=\"UTF-16\"?>");
    assertThat(loggerOutput).contains("</mammals>");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_xmlResponse(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        XML_BODY, APPLICATION_XML, false);

    assertThat(loggerOutput).contains("<?xml version=\"1.0\" encoding=\"UTF-16\"?>");
    assertThat(loggerOutput).contains("</mammals>");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_malformedXmlRequest(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        MALFORMED_XML_BODY, APPLICATION_XML, false);

    loggerOutput
        .stream()
        .filter(it ->
            it.startsWith(
                "<?xml version=\"1.0\" encoding=\"UTF-16\"?><mammals>"))
        .findFirst()
        .orElseThrow(() ->
            new AssertionError("Interceptor should be able to handle xml request body."));
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_malformedXmlResponse(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        MALFORMED_XML_BODY, APPLICATION_XML, false);

    loggerOutput
        .stream()
        .filter(it ->
            it.startsWith(
                "<?xml version=\"1.0\" encoding=\"UTF-16\"?><mammals>"))
        .findFirst()
        .orElseThrow(() ->
            new AssertionError("Interceptor should be able to handle xml response body."));
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_fileRequest(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
        PREFORMATTED_JSON_BODY, APPLICATION_ZIP, true);

    assertThat(loggerOutput).contains("  Omitted response body ");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_fileResponse(String interceptor, boolean withExecutor) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
        PREFORMATTED_JSON_BODY, APPLICATION_ZIP,
        true);

    assertThat(loggerOutput).contains("  Omitted response body ");
  }

}
