package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.internal.Util.TEXT_HTML;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class BodyWithHtmlPrintingTest extends BaseTest {

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

}

