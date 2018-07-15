package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.TestUtil.defaultClientWithInterceptor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import okhttp3.Request;
import org.junit.Rule;
import org.junit.Test;

public class LoggingInterceptorTests {

  @Rule public MockWebServer server = new MockWebServer();

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

  private Request defaultRequest() {
    return new Request.Builder()
        .url(String.valueOf(server.url("/")))
        .build();
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

    assertEquals("Logger with default formatter should publish message only",
        testLogger.firstFormattedEvent(), testLogger.firstRawEvent());
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
    String[] entryElements = extractTextFromLogEntrySeparatedByBrackets(logEntry);

    assertEquals("Log event should contain 3 elements: Date, Level, Message.",
        3, entryElements.length);

    assertTextIsParsableDate(entryElements[0]);

    assertTrue("Log entry should contain severity tag.",
        entryElements[1].contains("INFO"));

    assertEquals("Log entry should end with message text.",
        entryElements[2], testLogger.lastRawEvent());

  }

  private void assertTextIsParsableDate(String text) {
    try {
      new SimpleDateFormat("yyyy-MM-ddd kk:mm:ss").parse(text);
    } catch (ParseException e) {
      fail("Log entry should start with parsable date stamp");
    }
  }

  @SuppressWarnings({"RegExpRedundantEscape", "RegExpSingleCharAlternation"})
  private String[] extractTextFromLogEntrySeparatedByBrackets(String logEntry) {
    return Arrays
        .stream(logEntry.split("\\[|\\]"))
        .filter(s -> s.trim().length() > 0)
        .map(String::trim)
        .toArray(String[]::new);
  }

  @Test
  public void loggerShouldBeDisabledWhenLevelSetToNone() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
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
    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
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
    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
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
    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
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

}
