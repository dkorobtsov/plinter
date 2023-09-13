package io.github.dkorobtsov.tests.integration;

import io.github.dkorobtsov.plinter.core.LoggingFormat;
import io.github.dkorobtsov.tests.BaseTest;
import io.github.dkorobtsov.tests.utils.TestLogger;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import spark.Spark;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests to validate that different body types are handled properly.
 */
@RunWith(JUnitParamsRunner.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class InterceptorBodyHandlingTest extends BaseTest {

  @BeforeClass
  public static void setup() {
    startSparkServer();
  }

  @AfterClass
  public static void tearDown() {
    Spark.stop();
  }

  @Test
  @Parameters(method = "interceptors")
  public void printableBodyHandling_html(String interceptor) {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    interceptWithConfig(interceptor, defaultLoggerConfig(testLogger), null, null,
      WEBSERVER_URL + "index.html");

    assertThat(testLogger.formattedOutput())
      .containsIgnoringCase("Content-Type: text/html")
      .containsIgnoringCase("Hello World!");
  }

  @Test
  @Parameters(method = "interceptors")
  public void printableBodyHandling_javaScript(String interceptor) {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    interceptWithConfig(interceptor, defaultLoggerConfig(testLogger), null, null,
      WEBSERVER_URL + "script.js");

    assertThat(testLogger.formattedOutput())
      .containsIgnoringCase("Content-Type: application/javascript")
      .containsIgnoringCase("console.log(\"Hello JavaScript\");");
  }

  @Test
  @Parameters(method = "interceptors")
  public void printableBodyHandling_negativeTest_png(String interceptor) {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    interceptWithConfig(interceptor, defaultLoggerConfig(testLogger), null, null,
      WEBSERVER_URL + "wordcloud.png");

    assertThat(testLogger.formattedOutput())
      .containsIgnoringCase("Content-Type: image/png")
      .containsIgnoringCase("Omitted response body");
  }

  @Test
  @Parameters(method = "interceptors")
  public void printableBodyHandling_css(String interceptor) {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    interceptWithConfig(interceptor, defaultLoggerConfig(testLogger), null, null,
      WEBSERVER_URL + "style.css");

    assertThat(testLogger.formattedOutput())
      .containsIgnoringCase("Content-Type: text/css")
      .containsIgnoringCase("Content of css file");
  }

  @Test
  @Parameters(method = "interceptors")
  public void printableBodyHandling_txt(String interceptor) {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    interceptWithConfig(interceptor, defaultLoggerConfig(testLogger), null, null,
      WEBSERVER_URL + "hello.txt");

    assertThat(testLogger.formattedOutput())
      .containsIgnoringCase("Content-Type: text/plain")
      .containsIgnoringCase("Hello World!");
  }

  @Test
  @Parameters(method = "interceptors")
  public void printableBodyHandling_raml(String interceptor) {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    interceptWithConfig(interceptor, defaultLoggerConfig(testLogger), null, null,
      WEBSERVER_URL + "helloworld.raml");

    assertThat(testLogger.formattedOutput())
      .containsIgnoringCase("Content-Type: application/raml+yaml")
      .containsIgnoringCase("/helloworld: # optional resource");
  }

  @Test
  @Parameters(method = "interceptors")
  public void printableBodyHandling_yaml(String interceptor) {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    interceptWithConfig(interceptor, defaultLoggerConfig(testLogger), null, null,
      WEBSERVER_URL + "petstore-minimal.yaml");

    assertThat(testLogger.formattedOutput())
      .containsIgnoringCase("Content-Type: application/raml+yaml")
      .containsIgnoringCase("title: \"Swagger Petstore\"");
  }

  @Test
  @Parameters(method = "interceptors")
  public void printableBodyHandling_json(String interceptor) {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    interceptWithConfig(interceptor, defaultLoggerConfig(testLogger), null, null,
      WEBSERVER_URL + "petstore_minimal.json");

    assertThat(testLogger.formattedOutput())
      .containsIgnoringCase("Content-Type: application/json")
      .containsIgnoringCase("\"host\": \"petstore.swagger.io\",");
  }

}
