package com.dkorobtsov.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.squareup.okhttp.mockwebserver.MockResponse;
import java.io.IOException;
import java.util.concurrent.Executors;
import org.junit.Test;

public class LoggingInterceptorTests extends BaseTest {



  @Test
  public void loggerShouldWorkWithoutAnyAdditionalConfiguration() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

    assertTrue("Logger should publish events using only default configuration",
        testLogger.firstFormattedEvent().contains("Request"));
  }

  @Test
  public void loggerWithDefaultFormatterShouldPrintMessageOnly() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

    //Comparing message by length since on Gradle runner characters may be different
    //unless GradleVM executes with -Dfile.encoding=utf-8 option
    assertEquals("Logger with default formatter should publish message only",
        testLogger.firstRawEvent().length(), testLogger.firstFormattedEvent().length());
  }

  @Test
  public void loggerShouldBeDisabledWhenDebugModeSetToFalse() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .loggable(false)
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

    assertTrue("Logger output should be empty if debug mode is off.",
        testLogger.formattedOutput().isEmpty());
  }

  @Test
  public void loggerShouldBeEnabledWhenDebugModeSetToTrue() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .loggable(true)
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

    assertTrue("Logger should publish intercepted events if debug mode is on.",
        testLogger.firstFormattedEvent().contains("Request"));
  }

  @Test
  public void defaultLoggerFormatCanBeModified() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    TestLogger testLogger = new TestLogger(LogFormatter.JUL_DATE_LEVEL_MESSAGE);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .loggable(true)
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

    String logEntry = testLogger.lastFormattedEvent();

    TestUtil.assertLogEntryElementsCount(logEntry, 3);
    TestUtil.assertEntryStartsWithParsableDate(logEntry);
  }

  @Test
  public void loggerShouldBeDisabledWhenLevelSetToNone() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    TestLogger testLogger = new TestLogger(LogFormatter.JUL_THREAD_MESSAGE);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .level(Level.NONE)
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

    assertTrue("Logger output should be empty if debug mode is off.",
        testLogger.formattedOutput().isEmpty());
  }

  @Test
  public void headersShouldNotBeLoggedWhenLevelSetToBody() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    TestLogger testLogger = new TestLogger(LogFormatter.JUL_THREAD_MESSAGE);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .level(Level.BODY)
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

    assertFalse("Headers should not be logged when level set to Body.",
        testLogger.formattedOutput().contains("Headers"));
  }

  @Test
  public void bodyShouldNotBeLoggedWhenLevelSetToHeaders() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    TestLogger testLogger = new TestLogger(LogFormatter.JUL_THREAD_MESSAGE);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .level(Level.HEADERS)
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

    assertFalse("Body should not be logged when level set to Headers.",
        testLogger.formattedOutput().contains("body"));
  }

  @Test
  public void allDetailsShouldBePrintedIfLevelSetToBasic() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    TestLogger testLogger = new TestLogger(LogFormatter.JUL_THREAD_MESSAGE);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .level(Level.BASIC)
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

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
  public void userShouldBeAbleToSupplyExecutor() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    TestLogger testLogger = new TestLogger(LogFormatter.JUL_THREAD_MESSAGE);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .level(Level.BASIC)
        .executor(Executors.newSingleThreadExecutor())
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

    assertTrue("User should be able to supply executor.",
        testLogger.formattedOutput().contains("thread"));
  }

}
