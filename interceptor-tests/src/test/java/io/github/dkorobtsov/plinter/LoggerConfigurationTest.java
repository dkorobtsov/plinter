package io.github.dkorobtsov.plinter;

import io.github.dkorobtsov.plinter.core.Level;
import io.github.dkorobtsov.plinter.core.LoggerConfig;
import io.github.dkorobtsov.plinter.core.LoggingFormat;
import io.github.dkorobtsov.plinter.utils.TestLogger;
import io.github.dkorobtsov.plinter.utils.TestUtil;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import okhttp3.mockwebserver.MockResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Executors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test validating logger behavior when different configuration options are used.
 */
@RunWith(JUnitParamsRunner.class)
public class LoggerConfigurationTest extends BaseTest {

  private static final Logger logger = LogManager
      .getLogger(LoggerConfigurationTest.class.getName());

  @Test
  @Parameters(method = "interceptors")
  public void loggerShouldWorkWithoutAnyAdditionalConfiguration(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

    interceptWithConfig(interceptor, LoggerConfig.builder()
        .logger(testLogger)
        .build());

    assertTrue("Logger should publish events using only default configuration",
        testLogger.firstFormattedEvent(false)
            .contains("Request"));
  }

  @Test
  @Parameters(method = "interceptors")
  public void loggerWithDefaultFormatterShouldPrintMessageOnly(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

    interceptWithConfig(interceptor, LoggerConfig.builder()
        .logger(testLogger)
        .build());

    Assert.assertEquals("Logger with default format should publish message only",
        "┌────── Request ───────────────────────────────────────────"
            + "───────────────────────────────────────────────────",
        testLogger.firstFormattedEvent(false));
  }

  @Test
  @Parameters(method = "interceptors")
  public void loggerShouldBeDisabledWhenDebugModeSetToFalse(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

    interceptWithConfig(interceptor, LoggerConfig.builder()
        .logger(testLogger)
        .loggable(false)
        .build());

    assertTrue("Logger output should be empty if debug mode is off.",
        testLogger.formattedOutput().isEmpty());
  }

  @Test
  @Parameters(method = "interceptors")
  public void loggerShouldBeEnabledWhenDebugModeSetToTrue(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

    interceptWithConfig(interceptor, LoggerConfig.builder()
        .logger(testLogger)
        .build());

    assertTrue("Logger should publish intercepted events if debug mode is on.",
        testLogger.firstFormattedEvent(true)
            .contains("Request"));
  }

  @Test
  @Parameters(method = "interceptors")
  public void defaultLoggerFormatCanBeModified(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_DATE_LEVEL_MESSAGE);

    interceptWithConfig(interceptor, LoggerConfig.builder()
        .logger(testLogger)
        .build());

    final String logEntry = testLogger.firstFormattedEvent(true);

    TestUtil.assertLogEntryElementsCount(logEntry, 2);
    TestUtil.assertEntryStartsWithParsableDate(logEntry);
  }

  @Test
  @Parameters(method = "interceptors")
  public void loggerShouldBeDisabledWhenLevelSetToNone(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

    interceptWithConfig(interceptor, LoggerConfig.builder()
        .logger(testLogger)
        .level(Level.NONE)
        .build());

    assertTrue("Logger output should be empty if level set to None.",
        testLogger.formattedOutput().isEmpty());
  }

  @Test
  @Parameters(method = "interceptors")
  public void headersShouldNotBeLoggedWhenLevelSetToBody(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

    interceptWithConfig(interceptor, LoggerConfig.builder()
        .logger(testLogger)
        .level(Level.BODY)
        .build());

    assertFalse("Headers should not be logged when level set to Body.",
        testLogger.formattedOutput().contains("Headers"));
  }

  @Test
  @Parameters(method = "interceptors")
  public void bodyShouldNotBeLoggedWhenLevelSetToHeaders(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

    interceptWithConfig(interceptor, LoggerConfig.builder()
        .logger(testLogger)
        .level(Level.HEADERS)
        .build());

    assertFalse("Body should not be logged when level set to Headers.",
        testLogger.formattedOutput().contains("body"));
  }

  @Test
  @Parameters(method = "interceptors")
  public void allDetailsShouldBePrintedIfLevelSetToBasic(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

    interceptWithConfig(interceptor, LoggerConfig.builder()
        .logger(testLogger)
        .level(Level.BASIC)
        .build());

    assertTrue("Request section should be present in logger output.",
        testLogger.formattedOutput().contains("Request"));

    assertTrue("Response section should be present in logger output.",
        testLogger.formattedOutput().contains("Response"));

    assertTrue("Url should be logged when level set to Basic.",
        testLogger.formattedOutput().contains("URL"));

    assertTrue("Method should be logged when level set to Basic.",
        testLogger.formattedOutput().contains("Method"));

    assertTrue("Headers should be logged when level set to Basic.",
        testLogger.formattedOutput().contains("Headers"));

    assertTrue("Status code should be logged when level set to Basic.",
        testLogger.formattedOutput().contains("Status Code:"));

    assertTrue("Body should be logged when level set to Basic.",
        testLogger.formattedOutput().contains("body"));
  }

  @Test
  @Parameters(method = "interceptors")
  public void userShouldBeAbleToSupplyExecutor(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

    interceptWithConfig(interceptor, LoggerConfig.builder()
        .logger(testLogger)
        .executor(Executors.newSingleThreadExecutor())
        .build());

    assertTrue("User should be able to supply executor.",
        testLogger.formattedOutput().contains("thread"));
  }

  @Test
  @Parameters(method = "interceptors")
  public void threadInfoShouldNotBeLoggedIfDisabled(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

    interceptWithConfig(interceptor, LoggerConfig.builder()
        .logger(testLogger)
        .withThreadInfo(false)
        .build());

    assertFalse("Thread info should not be logged if disabled.",
        testLogger.formattedOutput().contains("Thread"));
  }

  @Test
  @Parameters(method = "interceptors")
  public void threadInfoShouldBeLoggedIfEnabled(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

    interceptWithConfig(interceptor, LoggerConfig.builder()
        .logger(testLogger)
        .withThreadInfo(true)
        .build());

    assertTrue("Thread info should not be logged if disabled.",
        testLogger.formattedOutput().contains("Thread"));
  }

  @Test
  @Parameters(method = "interceptors")
  public void threadInfoShouldNotBeLoggedByDefault(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

    interceptWithConfig(interceptor, LoggerConfig.builder()
        .logger(testLogger)
        .build());

    assertFalse("Thread info should not be logged if disabled.",
        testLogger.formattedOutput().contains("Thread"));
  }

  @Test
  @Parameters(method = "interceptors")
  public void userShouldBeAbleToUseDefaultLogger(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));

    try {
      interceptWithConfig(interceptor, LoggerConfig.builder()
          .build());
    } catch (Exception e) {
      fail("User should be able to use default logger.");
      logger.error(e);
    }
  }

}
