package com.dkorobtsov.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.dkorobtsov.logging.utils.TestLogger;
import com.dkorobtsov.logging.utils.TestUtil;
import com.squareup.okhttp.mockwebserver.MockResponse;
import java.io.IOException;
import java.util.concurrent.Executors;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class LoggerConfigurationTest extends BaseTest {

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void loggerShouldWorkWithoutAnyAdditionalConfiguration(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

        interceptWithConfig(interceptorVersion,
            LoggerConfig.builder()
                .logger(testLogger)
                .build());

        assertTrue("Logger should publish events using only default configuration",
            testLogger.firstFormattedEvent(true)
                .contains("Request"));
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void loggerWithDefaultFormatterShouldPrintMessageOnly(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

        interceptWithConfig(interceptorVersion,
            LoggerConfig.builder()
                .logger(testLogger)
                .build());

        //Comparing message by length since on Gradle runner characters may be different
        //unless GradleVM executes with -Dfile.encoding=utf-8 option
        assertEquals("Logger with default format should publish message only",
            testLogger.firstRawEvent().length(),
            testLogger.firstFormattedEvent(false).length());
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void loggerShouldBeDisabledWhenDebugModeSetToFalse(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

        interceptWithConfig(interceptorVersion,
            LoggerConfig.builder()
                .logger(testLogger)
                .loggable(false)
                .build());

        assertTrue("Logger output should be empty if debug mode is off.",
            testLogger.formattedOutput().isEmpty());
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void loggerShouldBeEnabledWhenDebugModeSetToTrue(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

        interceptWithConfig(interceptorVersion,
            LoggerConfig.builder()
                .logger(testLogger)
                .build());

        assertTrue("Logger should publish intercepted events if debug mode is on.",
            testLogger.firstFormattedEvent(true)
                .contains("Request"));
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void defaultLoggerFormatCanBeModified(String interceptorVersion) throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_DATE_LEVEL_MESSAGE);

        interceptWithConfig(interceptorVersion,
            LoggerConfig.builder()
                .logger(testLogger)
                .build());

        String logEntry = testLogger.lastFormattedEvent(true);

        TestUtil.assertLogEntryElementsCount(logEntry, 3);
        TestUtil.assertEntryStartsWithParsableDate(logEntry);
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void loggerShouldBeDisabledWhenLevelSetToNone(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

        interceptWithConfig(interceptorVersion,
            LoggerConfig.builder()
                .logger(testLogger)
                .level(Level.NONE)
                .build());

        assertTrue("Logger output should be empty if level set to None.",
            testLogger.formattedOutput().isEmpty());
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void headersShouldNotBeLoggedWhenLevelSetToBody(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

        interceptWithConfig(interceptorVersion,
            LoggerConfig.builder()
                .logger(testLogger)
                .level(Level.BODY)
                .build());

        assertFalse("Headers should not be logged when level set to Body.",
            testLogger.formattedOutput().contains("Headers"));
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void bodyShouldNotBeLoggedWhenLevelSetToHeaders(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

        interceptWithConfig(interceptorVersion,
            LoggerConfig.builder()
                .logger(testLogger)
                .level(Level.HEADERS)
                .build());

        assertFalse("Body should not be logged when level set to Headers.",
            testLogger.formattedOutput().contains("body"));
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void allDetailsShouldBePrintedIfLevelSetToBasic(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

        interceptWithConfig(interceptorVersion,
            LoggerConfig.builder()
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
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void userShouldBeAbleToSupplyExecutor(String interceptorVersion) throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

        interceptWithConfig(interceptorVersion,
            LoggerConfig.builder()
                .logger(testLogger)
                .executor(Executors.newSingleThreadExecutor())
                .build());

        assertTrue("User should be able to supply executor.",
            testLogger.formattedOutput().contains("thread"));
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void threadInfoShouldNotBeLoggedIfDisabled(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

        interceptWithConfig(interceptorVersion,
            LoggerConfig.builder()
                .logger(testLogger)
                .withThreadInfo(false)
                .build());

        assertFalse("Thread info should not be logged if disabled.",
            testLogger.formattedOutput().contains("Thread"));
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void threadInfoShouldBeLoggedIfEnabled(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

        interceptWithConfig(interceptorVersion,
            LoggerConfig.builder()
                .logger(testLogger)
                .withThreadInfo(true)
                .build());

        assertTrue("Thread info should not be logged if disabled.",
            testLogger.formattedOutput().contains("Thread"));
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void threadInfoShouldNotBeLoggedByDefault(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

        interceptWithConfig(interceptorVersion,
            LoggerConfig.builder()
                .logger(testLogger)
                .build());

        assertFalse("Thread info should not be logged if disabled.",
            testLogger.formattedOutput().contains("Thread"));
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void userShouldBeAbleToUseDefaultLogger(String interceptorVersion) {
        server.enqueue(new MockResponse().setResponseCode(200));

        try {
            interceptWithConfig(interceptorVersion,
                LoggerConfig.builder()
                    .build());
        } catch (Exception e) {
            fail("User should be able to use default logger.");
            e.printStackTrace();
        }
    }

}
