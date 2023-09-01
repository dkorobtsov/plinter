package io.github.dkorobtsov.plinter;

import io.github.dkorobtsov.plinter.utils.Retry;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.dkorobtsov.plinter.core.internal.Util.TEXT_HTML;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests validating that event body containing HTML document is handled properly.
 */
@RunWith(JUnitParamsRunner.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class BodyWithHtmlPrintingTest extends BaseTest {

  private static final String EMPTY_BODY = "";
  private static final String HTML_BODY =
    "<html>\n"
      + "    <head>\n"
      + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>\n"
      + "        <title>Error 404 Not Found</title>\n"
      + "    </head>\n"
      + "    <body>\n"
      + "        <div style=\"font-family:Arial,Helvetica,sans-serif;\">\n"
      + "            <h2>HTTP ERROR 404</h2>\n"
      + "            <pre>Not Found</pre>\n"
      + "        </div>\n"
      + "        <tr>\n"
      + "            <th></th>\n"
      + "            <th></th>\n"
      + "            <th></th>\n"
      + "            <th></th>\n"
      + "        </tr>\n"
      + "    </body>\n"
      + "</html>";

  private static final String MALFORMED_HTML_BODY =
    "<html>\n"
      + "    <head>\n"
      + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>\n"
      + "        <title>Error 404 Not Found</title>\n"
      + "    </head>\n"
      + "    <body>\n"
      + "        <div style=\"font-family:Arial,Helvetica,sans-serif;\">\n"
      + "            <h2>HTTP ERROR 404</h2>\n"
      + "            <pre>Not Found</pre>\n"
      + "        </div>\n"
      + "        <tr>\n"
      + "            <th></th>\n"
      + "            <th><th></th>\n"
      + "            <th></th>\n"
      + "            <th></th>\n"
      + "        </tr>\n"
      + "    </body>\n"
      + "</html>";

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_emptyHtmlRequest(String interceptor,
                                            boolean withExecutor,
                                            boolean logByLine) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
      EMPTY_BODY, TEXT_HTML, false, logByLine);

    assertThat(loggerOutput).contains("Empty request body");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_emptyHtmlResponse(String interceptor,
                                             boolean withExecutor,
                                             boolean logByLine) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
      EMPTY_BODY, TEXT_HTML, false, logByLine);

    assertThat(loggerOutput).contains("Empty response body");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_htmlRequest(String interceptor,
                                       boolean withExecutor,
                                       boolean logByLine) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
      HTML_BODY, TEXT_HTML, false, logByLine);

    assertThat(loggerOutput).contains("<title>Error 404 Not Found</title>");
  }

  @Test
  @Retry
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_htmlResponse(String interceptor,
                                        boolean withExecutor,
                                        boolean logByLine) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
      HTML_BODY, TEXT_HTML, false, logByLine);

    assertThat(loggerOutput).contains("<title>Error 404 Not Found</title>");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_malformedHtmlRequest(String interceptor,
                                                boolean withExecutor,
                                                boolean logByLine) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
      MALFORMED_HTML_BODY, TEXT_HTML, false, logByLine);

    final List<String> filteredOutput = loggerOutput
      .stream()
      .filter(it ->
        it.contains(
          "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>"
        )
      )
      .collect(Collectors.toList());

    assertThat(filteredOutput)
      .withFailMessage("Interceptor should be able to handle html request body.")
      .isNotEmpty();
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_malformedHtmlResponse(String interceptor,
                                                 boolean withExecutor,
                                                 boolean logByLine) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
      MALFORMED_HTML_BODY, "text/html", false, logByLine);

    final List<String> filteredOutput = loggerOutput
      .stream()
      .filter(it ->
        it.contains(
          "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>"
        )
      )
      .collect(Collectors.toList());

    assertThat(filteredOutput)
      .withFailMessage("Interceptor should be able to handle html request body.")
      .isNotEmpty();
  }

}

