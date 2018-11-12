package com.dkorobtsov.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.dkorobtsov.logging.interceptors.ApacheHttpRequestInterceptor;
import com.dkorobtsov.logging.interceptors.ApacheHttpResponseInterceptor;
import com.dkorobtsov.logging.interceptors.Okhttp3LoggingInterceptor;
import com.dkorobtsov.logging.interceptors.OkhttpLoggingInterceptor;
import com.squareup.okhttp.mockwebserver.MockResponse;
import java.io.IOException;
import java.util.concurrent.Executors;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class LoggingInterceptorsTests extends BaseTest {

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void loggerShouldWorkWithoutAnyAdditionalConfiguration(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
        intercepWithSimpleInterceptor(interceptorVersion, testLogger);

        assertTrue("Logger should publish events using only default configuration",
            testLogger.firstFormattedEvent().contains("Request"));
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void loggerWithDefaultFormatterShouldPrintMessageOnly(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
        intercepWithSimpleInterceptor(interceptorVersion, testLogger);

        //Comparing message by length since on Gradle runner characters may be different
        //unless GradleVM executes with -Dfile.encoding=utf-8 option
        assertEquals("Logger with default formatter should publish message only",
            testLogger.firstRawEvent().length(), testLogger.firstFormattedEvent().length());
    }

    private void intercepWithSimpleInterceptor(String interceptorVersion, TestLogger testLogger)
        throws IOException {
        if (interceptorVersion.equals(InterceptorVersion.OKHTTP3.getName())) {
            Okhttp3LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
                .logger(testLogger)
                .buildForOkhttp3();

            defaultOkhttp3ClientWithInterceptor(interceptor)
                .newCall(defaultOkhttp3Request())
                .execute();
        } else if (interceptorVersion.equals(InterceptorVersion.OKHTTP.getName())) {
            final OkhttpLoggingInterceptor interceptor = new LoggingInterceptor.Builder()
                .logger(testLogger)
                .buildForOkhttp();
            defaultOkhttpClientWithInterceptor(interceptor)
                .newCall(defaultOkhttpRequest())
                .execute();
        } else if (interceptorVersion
            .equals(InterceptorVersion.APACHE_HTTPCLIENT_REQUEST.getName())) {
            final ApacheHttpRequestInterceptor requestInterceptor = new LoggingInterceptor.Builder()
                .logger(testLogger)
                .buildForApacheHttpClientRequest();
            final ApacheHttpResponseInterceptor responseInterceptor = new LoggingInterceptor.Builder()
                .logger(testLogger)
                .builFordApacheHttpClientResponse();
            defaultApacheClientWithInterceptors(requestInterceptor, responseInterceptor)
                .execute(defaultApacheHttpRequest());
        } else {
            fail("Only okhttp and okhttp3 versions are supported");
        }
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void loggerShouldBeDisabledWhenDebugModeSetToFalse(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
        interceptWithSimpleLoggableInterceptor(interceptorVersion, testLogger, false);

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
        TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
        interceptWithSimpleLoggableInterceptor(interceptorVersion, testLogger, true);

        assertTrue("Logger should publish intercepted events if debug mode is on.",
            testLogger.firstFormattedEvent().contains("Request"));
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void defaultLoggerFormatCanBeModified(String interceptorVersion) throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LogFormatter.JUL_DATE_LEVEL_MESSAGE);
        interceptWithSimpleLoggableInterceptor(interceptorVersion, testLogger, true);

        String logEntry = testLogger.lastFormattedEvent();

        TestUtil.assertLogEntryElementsCount(logEntry, 3);
        TestUtil.assertEntryStartsWithParsableDate(logEntry);
    }

    private void interceptWithSimpleLoggableInterceptor(String interceptorVersion,
        TestLogger testLogger, boolean loggable) throws IOException {
        if (interceptorVersion.equals(InterceptorVersion.OKHTTP3.getName())) {
            Okhttp3LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
                .loggable(loggable)
                .logger(testLogger)
                .buildForOkhttp3();

            defaultOkhttp3ClientWithInterceptor(interceptor)
                .newCall(defaultOkhttp3Request())
                .execute();
        } else if (interceptorVersion.equals(InterceptorVersion.OKHTTP.getName())) {
            final OkhttpLoggingInterceptor interceptor = new LoggingInterceptor.Builder()
                .logger(testLogger)
                .loggable(loggable)
                .buildForOkhttp();
            defaultOkhttpClientWithInterceptor(interceptor)
                .newCall(defaultOkhttpRequest())
                .execute();
        } else if (interceptorVersion
            .equals(InterceptorVersion.APACHE_HTTPCLIENT_REQUEST.getName())) {
            final ApacheHttpRequestInterceptor requestInterceptor = new LoggingInterceptor.Builder()
                .logger(testLogger)
                .loggable(loggable)
                .buildForApacheHttpClientRequest();
            final ApacheHttpResponseInterceptor responseInterceptor = new LoggingInterceptor.Builder()
                .logger(testLogger)
                .loggable(loggable)
                .builFordApacheHttpClientResponse();
            defaultApacheClientWithInterceptors(requestInterceptor, responseInterceptor)
                .execute(defaultApacheHttpRequest());
        } else {
            fail("Only okhttp and okhttp3 versions are supported");
        }
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void loggerShouldBeDisabledWhenLevelSetToNone(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LogFormatter.JUL_THREAD_MESSAGE);
        interceptWithLogLevelInterceptor(interceptorVersion, testLogger, Level.NONE);

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
        TestLogger testLogger = new TestLogger(LogFormatter.JUL_THREAD_MESSAGE);
        interceptWithLogLevelInterceptor(interceptorVersion, testLogger, Level.BODY);

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
        TestLogger testLogger = new TestLogger(LogFormatter.JUL_THREAD_MESSAGE);
        interceptWithLogLevelInterceptor(interceptorVersion, testLogger, Level.HEADERS);

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
        TestLogger testLogger = new TestLogger(LogFormatter.JUL_THREAD_MESSAGE);
        interceptWithLogLevelInterceptor(interceptorVersion, testLogger, Level.BASIC);

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

    private void interceptWithLogLevelInterceptor(String interceptorVersion, TestLogger testLogger,
        Level level) throws IOException {
        if (interceptorVersion.equals(InterceptorVersion.OKHTTP3.getName())) {
            Okhttp3LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
                .level(level)
                .logger(testLogger)
                .buildForOkhttp3();

            defaultOkhttp3ClientWithInterceptor(interceptor)
                .newCall(defaultOkhttp3Request())
                .execute();
        } else if (interceptorVersion.equals(InterceptorVersion.OKHTTP.getName())) {
            final OkhttpLoggingInterceptor interceptor = new LoggingInterceptor.Builder()
                .logger(testLogger)
                .level(level)
                .buildForOkhttp();
            defaultOkhttpClientWithInterceptor(interceptor)
                .newCall(defaultOkhttpRequest())
                .execute();
        } else if (interceptorVersion
            .equals(InterceptorVersion.APACHE_HTTPCLIENT_REQUEST.getName())) {
            final ApacheHttpRequestInterceptor requestInterceptor = new LoggingInterceptor.Builder()
                .logger(testLogger)
                .level(level)
                .buildForApacheHttpClientRequest();
            final ApacheHttpResponseInterceptor responseInterceptor = new LoggingInterceptor.Builder()
                .logger(testLogger)
                .level(level)
                .builFordApacheHttpClientResponse();
            defaultApacheClientWithInterceptors(requestInterceptor, responseInterceptor)
                .execute(defaultApacheHttpRequest());
        } else {
            fail("Only okhttp and okhttp3 versions are supported");
        }
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void userShouldBeAbleToSupplyExecutor(String interceptorVersion) throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        TestLogger testLogger = new TestLogger(LogFormatter.JUL_THREAD_MESSAGE);
        if (interceptorVersion.equals(InterceptorVersion.OKHTTP3.getName())) {
            Okhttp3LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
                .level(Level.BASIC)
                .executor(Executors.newSingleThreadExecutor())
                .logger(testLogger)
                .buildForOkhttp3();

            defaultOkhttp3ClientWithInterceptor(interceptor)
                .newCall(defaultOkhttp3Request())
                .execute();
        } else if (interceptorVersion.equals(InterceptorVersion.OKHTTP.getName())) {
            final OkhttpLoggingInterceptor interceptor = new LoggingInterceptor.Builder()
                .logger(testLogger)
                .level(Level.BASIC)
                .executor(Executors.newSingleThreadExecutor())
                .buildForOkhttp();
            defaultOkhttpClientWithInterceptor(interceptor)
                .newCall(defaultOkhttpRequest())
                .execute();
        } else if (interceptorVersion
            .equals(InterceptorVersion.APACHE_HTTPCLIENT_REQUEST.getName())) {
            final ApacheHttpRequestInterceptor requestInterceptor = new LoggingInterceptor.Builder()
                .logger(testLogger)
                .level(Level.BASIC)
                .executor(Executors.newSingleThreadExecutor())
                .buildForApacheHttpClientRequest();
            final ApacheHttpResponseInterceptor responseInterceptor = new LoggingInterceptor.Builder()
                .logger(testLogger)
                .level(Level.BASIC)
                .executor(Executors.newSingleThreadExecutor())
                .builFordApacheHttpClientResponse();
            defaultApacheClientWithInterceptors(requestInterceptor, responseInterceptor)
                .execute(defaultApacheHttpRequest());
        } else {
            fail("Only okhttp and okhttp3 versions are supported");
        }

        assertTrue("User should be able to supply executor.",
            testLogger.formattedOutput().contains("thread"));
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void userShouldBeAbleToUseDefaultLogger(String interceptorVersion) throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        if (interceptorVersion.equals(InterceptorVersion.OKHTTP3.getName())) {
            Okhttp3LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
                .level(Level.BASIC)
                .executor(Executors.newSingleThreadExecutor())
                .buildForOkhttp3();

            defaultOkhttp3ClientWithInterceptor(interceptor)
                .newCall(defaultOkhttp3Request())
                .execute();
        } else if (interceptorVersion.equals(InterceptorVersion.OKHTTP.getName())) {
            final OkhttpLoggingInterceptor interceptor = new LoggingInterceptor.Builder()
                .level(Level.BASIC)
                .executor(Executors.newSingleThreadExecutor())
                .buildForOkhttp();
            defaultOkhttpClientWithInterceptor(interceptor)
                .newCall(defaultOkhttpRequest())
                .execute();
        } else if (interceptorVersion
            .equals(InterceptorVersion.APACHE_HTTPCLIENT_REQUEST.getName())) {
            final ApacheHttpRequestInterceptor requestInterceptor = new LoggingInterceptor.Builder()
                .level(Level.BASIC)
                .executor(Executors.newSingleThreadExecutor())
                .buildForApacheHttpClientRequest();
            final ApacheHttpResponseInterceptor responseInterceptor = new LoggingInterceptor.Builder()
                .level(Level.BASIC)
                .executor(Executors.newSingleThreadExecutor())
                .builFordApacheHttpClientResponse();
            defaultApacheClientWithInterceptors(requestInterceptor, responseInterceptor)
                .execute(defaultApacheHttpRequest());
        } else {
            fail("Only okhttp and okhttp3 versions are supported");
        }
    }


}
