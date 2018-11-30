package com.dkorobtsov.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.dkorobtsov.logging.enums.InterceptorVersion;
import com.dkorobtsov.logging.enums.Level;
import com.dkorobtsov.logging.enums.LoggingFormat;
import com.dkorobtsov.logging.interceptors.ApacheHttpRequestInterceptor;
import com.dkorobtsov.logging.interceptors.ApacheHttpResponseInterceptor;
import com.dkorobtsov.logging.interceptors.OkHttp3LoggingInterceptor;
import com.dkorobtsov.logging.interceptors.OkHttpLoggingInterceptor;
import com.squareup.okhttp.mockwebserver.MockResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class LoggingInterceptorsTests extends BaseTest {

    private final Logger logger = LogManager.getLogger(Log4j2LoggerTest.class);

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void loggerShouldWorkWithoutAnyAdditionalConfiguration(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
        interceptWithValues(interceptorVersion, testLogger);

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
        interceptWithValues(interceptorVersion, testLogger);

        //Comparing message by length since on Gradle runner characters may be different
        //unless GradleVM executes with -Dfile.encoding=utf-8 option
        assertEquals("Logger with default formatter should publish message only",
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
        interceptWithValues(interceptorVersion, testLogger, false);

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
        interceptWithValues(interceptorVersion, testLogger, true);

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
        interceptWithValues(interceptorVersion, testLogger);

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
        interceptWithValues(interceptorVersion, testLogger, Level.NONE);

        assertTrue("Logger output should be empty if debug mode is off.",
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
        interceptWithValues(interceptorVersion, testLogger, Level.BODY);

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
        interceptWithValues(interceptorVersion, testLogger, Level.HEADERS);

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
        interceptWithValues(interceptorVersion, testLogger, Level.BASIC);

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
    public void userShouldBeAbleToSupplyExecutor(String version) throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_THREAD_MESSAGE);

        interceptWithValues(version, testLogger, Executors.newSingleThreadExecutor());

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
        interceptWithValues(interceptorVersion, testLogger, Level.BASIC, false);

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
        interceptWithValues(interceptorVersion, testLogger, Level.BASIC, true);

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
        interceptWithValues(interceptorVersion, testLogger, Level.BASIC, null);

        assertFalse("Thread info should not be logged if disabled.",
            testLogger.formattedOutput().contains("Thread"));
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void userShouldBeAbleToUseDefaultLogger(String version) {
        server.enqueue(new MockResponse().setResponseCode(200));

        try {
            interceptWithValues(version);
        } catch (Exception e) {
            fail("User should be able to use default logger.");
            e.printStackTrace();
        }
    }

    private void interceptWithValues(String version) throws IOException {
        interceptWithValues(version, null, null, null, null, null);
    }

    private void interceptWithValues(String version, TestLogger testLogger) throws IOException {
        interceptWithValues(version, testLogger, null, null, null, null);
    }

    private void interceptWithValues(String version,
        TestLogger testLogger, Boolean loggable) throws IOException {
        interceptWithValues(version, testLogger, loggable, null, null, null);
    }

    @SuppressWarnings("SameParameterValue")
    private void interceptWithValues(String version,
        TestLogger testLogger, Level level, Boolean withThreadInfo) throws IOException {
        interceptWithValues(version, testLogger, null, level, null, withThreadInfo);
    }

    private void interceptWithValues(String version,
        TestLogger testLogger, Level level) throws IOException {
        interceptWithValues(version, testLogger, null, level, null, null);
    }

    private void interceptWithValues(String version, TestLogger testLogger, Executor runnable)
        throws IOException {
        interceptWithValues(version, testLogger, null, null, runnable, null);
    }


    private void interceptWithValues(String version, TestLogger testLogger, Boolean loggable,
        Level level, Executor executor, Boolean withThreadInfo) throws IOException {

        LoggingInterceptor.Builder interceptorBuilder = new LoggingInterceptor.Builder();

        if (Objects.nonNull(testLogger)) {
            interceptorBuilder.logger(testLogger);
        }

        if (Objects.nonNull(loggable)) {
            interceptorBuilder.loggable(loggable);
        }

        if (Objects.nonNull(level)) {
            interceptorBuilder.level(level);
        }

        if (Objects.nonNull(executor)) {
            interceptorBuilder.executor(executor);
        }

        if (Objects.nonNull(withThreadInfo)) {
            interceptorBuilder.withThreadInfo(withThreadInfo);
        }

        switch (InterceptorVersion.parse(version)) {
            case OKHTTP3:
                OkHttp3LoggingInterceptor okHttp3LoggingInterceptor = interceptorBuilder
                    .buildForOkhttp3();

                logger.info("OkHttp3 Interceptor: {}",
                    okHttp3LoggingInterceptor.loggerConfig().toString());

                defaultOkHttp3ClientWithInterceptor(okHttp3LoggingInterceptor)
                    .newCall(defaultOkHttp3Request())
                    .execute();
                break;

            case OKHTTP:
                final OkHttpLoggingInterceptor okHttpLoggingInterceptor = interceptorBuilder
                    .buildForOkhttp();

                logger.info("OkHttp Interceptor: {}",
                    okHttpLoggingInterceptor.loggerConfig().toString());

                defaultOkHttpClientWithInterceptor(okHttpLoggingInterceptor)
                    .newCall(defaultOkHttpRequest())
                    .execute();
                break;

            case APACHE_HTTPCLIENT_REQUEST:
                final ApacheHttpRequestInterceptor requestInterceptor = interceptorBuilder
                    .buildForApacheHttpClientRequest();

                final ApacheHttpResponseInterceptor responseInterceptor = interceptorBuilder
                    .buildFordApacheHttpClientResponse();

                logger.info("Apache Request Interceptor: {}",
                    requestInterceptor.loggerConfig().toString());
                logger.info("Apache Response Interceptor: {}",
                    responseInterceptor.loggerConfig().toString());

                defaultApacheClientWithInterceptors(requestInterceptor, responseInterceptor)
                    .execute(defaultApacheHttpRequest());
                break;

            default:
                fail("Unknown interceptor version: " + version);
                break;
        }
    }

}
