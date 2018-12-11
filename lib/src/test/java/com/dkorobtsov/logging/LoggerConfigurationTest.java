package com.dkorobtsov.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.dkorobtsov.logging.utils.TestLogger;
import com.dkorobtsov.logging.utils.TestUtil;
import com.squareup.okhttp.mockwebserver.MockResponse;
import java.util.concurrent.Executors;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class LoggerConfigurationTest extends BaseTest {

    @Test
    @Parameters(method = "interceptors")
    public void loggerShouldWorkWithoutAnyAdditionalConfiguration(String interceptor) {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

        interceptWithConfig(interceptor, LoggerConfig.builder()
            .logger(testLogger)
            .build());

        assertTrue("Logger should publish events using only default configuration",
            testLogger.firstFormattedEvent(true)
                .contains("Request"));
    }

    @Test
    @Parameters(method = "interceptors")
    public void loggerWithDefaultFormatterShouldPrintMessageOnly(String interceptor) {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

        interceptWithConfig(interceptor, LoggerConfig.builder()
            .logger(testLogger)
            .build());

        //Comparing message by length since on Gradle runner characters may be different
        //unless GradleVM executes with -Dfile.encoding=utf-8 option
        assertEquals("Logger with default format should publish message only",
            testLogger.firstRawEvent().length(),
            testLogger.firstFormattedEvent(false).length());
    }

    @Test
    @Parameters(method = "interceptors")
    public void loggerShouldBeDisabledWhenDebugModeSetToFalse(String interceptor) {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

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
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

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
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_DATE_LEVEL_MESSAGE);

        interceptWithConfig(interceptor, LoggerConfig.builder()
            .logger(testLogger)
            .build());

        String logEntry = testLogger.lastFormattedEvent(true);

        TestUtil.assertLogEntryElementsCount(logEntry, 3);
        TestUtil.assertEntryStartsWithParsableDate(logEntry);
    }

    @Test
    @Parameters(method = "interceptors")
    public void loggerShouldBeDisabledWhenLevelSetToNone(String interceptor) {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

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
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

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
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

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
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

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
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

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
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

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
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

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
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

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
            e.printStackTrace();
        }
    }

}
