package io.github.dkorobtsov.plinter;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static io.github.dkorobtsov.plinter.core.internal.Util.APPLICATION_XML;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests validating that event body containing XML document is handled properly.
 */
@RunWith(JUnitParamsRunner.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class BodyWithXmlPrintingTest extends BaseTest {

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
      + "<animal id=\"2\" species=\"Equus zebra\">Zebra</animal> "
      + "<mammals>";

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_xmlRequest(String interceptor,
                                      boolean withExecutor,
                                      boolean logByLine) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
      XML_BODY, APPLICATION_XML, false, logByLine);

    assertThat(loggerOutput)
      .contains("<animal id=\"0\" species=\"Capra hircus\">Goat</animal>")
      .contains("<animal id=\"1\" species=\"Panthera pardus\">Leopard</animal>");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_xmlResponse(String interceptor,
                                       boolean withExecutor,
                                       boolean logByLine) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
      XML_BODY, APPLICATION_XML, false, logByLine);

    assertThat(loggerOutput)
      .contains("<animal id=\"0\" species=\"Capra hircus\">Goat</animal>")
      .contains("<animal id=\"1\" species=\"Panthera pardus\">Leopard</animal>");
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_malformedXmlRequest(String interceptor,
                                               boolean withExecutor,
                                               boolean logByLine) {
    final List<String> loggerOutput = interceptedRequest(interceptor, withExecutor,
      MALFORMED_XML_BODY, APPLICATION_XML, true, logByLine);

    loggerOutput
      .stream()
      .filter(it ->
        it.startsWith(
          "  <?xml version=\"1.0\" encoding=\"UTF-16\"?><mammals>"))
      .findFirst()
      .orElseThrow(() ->
        new AssertionError("Interceptor should be able to handle xml request body."));
  }

  @Test
  @Parameters(method = "interceptorsWithExecutors")
  public void bodyHandling_malformedXmlResponse(String interceptor,
                                                boolean withExecutor,
                                                boolean logByLine) {
    final List<String> loggerOutput = interceptedResponse(interceptor, withExecutor,
      MALFORMED_XML_BODY, APPLICATION_XML, true, logByLine);

    loggerOutput
      .stream()
      .filter(it ->
        it.startsWith(
          "  <?xml version=\"1.0\" encoding=\"UTF-16\"?><mammals>"))
      .findFirst()
      .orElseThrow(() ->
        new AssertionError("Interceptor should be able to handle xml request body."));
  }


}
