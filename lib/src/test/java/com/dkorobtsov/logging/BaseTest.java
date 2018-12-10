package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.utils.InterceptorVersion.parse;
import static org.junit.Assert.fail;

import com.dkorobtsov.logging.LoggerConfig.LoggerConfigBuilder;
import com.dkorobtsov.logging.interceptors.apache.ApacheHttpRequestInterceptor;
import com.dkorobtsov.logging.interceptors.apache.ApacheHttpResponseInterceptor;
import com.dkorobtsov.logging.interceptors.okhttp.OkHttpLoggingInterceptor;
import com.dkorobtsov.logging.interceptors.okhttp3.OkHttp3LoggingInterceptor;
import com.dkorobtsov.logging.utils.InterceptorVersion;
import com.dkorobtsov.logging.utils.TestLogger;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public abstract class BaseTest {

    private static final ConnectionPool connectionPool = new ConnectionPool();
    private static final Dispatcher dispatcher = new Dispatcher();
    private static final int MAX_IDLE_CONNECTIONS = 10;
    private static final int KEEP_ALIVE_DURATION_MS = 60 * 1000;
    private final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
        .getLogger(Log4j2LoggerTest.class);

    @Rule
    public MockWebServer server = new MockWebServer();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void cleanAnyExistingJavaUtilityLoggingConfigurations() {
        LogManager.getLogManager().reset();
        Logger globalLogger = Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
        globalLogger.setLevel(Level.OFF);
    }

    /**
     * Returns default OkHttp request for use in tests.
     */
    Request defaultOkHttp3Request() {
        return new Request.Builder()
            .url(String.valueOf(server.url("/")))
            .build();
    }

    HttpUriRequest defaultApacheHttpRequest() {
        return new HttpGet(server.url("/").uri());
    }

    com.squareup.okhttp.Request defaultOkHttpRequest() {
        return new com.squareup.okhttp.Request.Builder()
            .url(String.valueOf(server.url("/")))
            .build();
    }

    /**
     * Returns OkHttpClient for all interceptor tests to use as a starting point.
     *
     * <p>The shared instance allows all tests to share a single connection pool, which prevents
     * idle connections from consuming unnecessary resources while connections wait to be evicted.
     */
    OkHttpClient defaultOkHttp3ClientWithInterceptor(Interceptor interceptor) {
        return new OkHttpClient.Builder()
            .connectionPool(connectionPool)
            .dispatcher(dispatcher)
            .addNetworkInterceptor(interceptor)
            .build();
    }

    HttpClient defaultApacheClientWithInterceptors(ApacheHttpRequestInterceptor requestInterceptor,
        ApacheHttpResponseInterceptor responseInterceptor) {
        return HttpClientBuilder
            .create()
            .addInterceptorFirst(requestInterceptor)
            .addInterceptorFirst(responseInterceptor)
            .setMaxConnTotal(MAX_IDLE_CONNECTIONS)
            .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
            .build();
    }

    com.squareup.okhttp.OkHttpClient defaultOkHttpClientWithInterceptor(
        com.squareup.okhttp.Interceptor interceptor) {
        final com.squareup.okhttp.OkHttpClient okHttpClient = new com.squareup.okhttp.OkHttpClient()
            .setConnectionPool(
                new com.squareup.okhttp.ConnectionPool(MAX_IDLE_CONNECTIONS,
                    KEEP_ALIVE_DURATION_MS))
            .setDispatcher(new com.squareup.okhttp.Dispatcher());
        okHttpClient.interceptors().add(interceptor);
        return okHttpClient;
    }

    void attachLoggerToInterceptorWithDefaultRequest(String version, LogWriter log4j2Writer)
        throws IOException {

        InterceptorVersion interceptorVersion = parse(version);
        switch (interceptorVersion) {
            case OKHTTP3:
                attachLoggerToOkHttp3InterceptorWithDefaultRequest(log4j2Writer);
                break;
            case OKHTTP:
                attachLoggerToOkHttpInterceptorWithDefaultRequest(log4j2Writer);
                break;
            case APACHE_HTTPCLIENT_REQUEST:
                attachLoggerToApacheRequestInterceptorWithDefaultRequest(log4j2Writer);
                break;
            default:
                fail("Unknown interceptor version: " + interceptorVersion);
                break;
        }
    }

    void attachLoggerToInterceptor(String version, LogWriter logWriter,
        Request okHttp3Request, com.squareup.okhttp.Request okHttpRequest,
        HttpUriRequest apacheHttpRequest) throws IOException {

        InterceptorVersion interceptorVersion = parse(version);
        switch (interceptorVersion) {
            case OKHTTP3:
                attachLoggerToOkHttp3Interceptor(logWriter, okHttp3Request);
                break;
            case OKHTTP:
                attachLoggerToOkHttpInterceptor(logWriter, okHttpRequest);
                break;
            case APACHE_HTTPCLIENT_REQUEST:
                attachLoggerToApacheRequestInterceptor(logWriter, apacheHttpRequest);
                break;
            default:
                fail("Unknown interceptor version: " + interceptorVersion);
                break;
        }
    }

    private void attachLoggerToOkHttpInterceptorWithDefaultRequest(LogWriter logWriter)
        throws IOException {
        attachLoggerToOkHttpInterceptor(logWriter, defaultOkHttpRequest());
    }

    private void attachLoggerToOkHttpInterceptor(LogWriter logWriter,
        com.squareup.okhttp.Request request) throws IOException {
        OkHttpLoggingInterceptor interceptor = new OkHttpLoggingInterceptor(
            LoggerConfig.builder()
                .logger(logWriter)
                .build());

        defaultOkHttpClientWithInterceptor(interceptor)
            .newCall(request)
            .execute();
    }

    private void attachLoggerToOkHttp3InterceptorWithDefaultRequest(LogWriter logWriter)
        throws IOException {
        attachLoggerToOkHttp3Interceptor(logWriter, defaultOkHttp3Request());
    }

    private void attachLoggerToOkHttp3Interceptor(LogWriter logWriter, Request request)
        throws IOException {
        OkHttp3LoggingInterceptor interceptor = new OkHttp3LoggingInterceptor(
            LoggerConfig.builder()
                .logger(logWriter)
                .build());

        log.debug("Sending request.");
        defaultOkHttp3ClientWithInterceptor(interceptor)
            .newCall(request)
            .execute();
    }

    private void attachLoggerToApacheRequestInterceptorWithDefaultRequest(LogWriter logWriter)
        throws IOException {
        attachLoggerToApacheRequestInterceptor(logWriter, defaultApacheHttpRequest());
    }

    private void attachLoggerToApacheRequestInterceptor(LogWriter logWriter, HttpUriRequest request)
        throws IOException {
        ApacheHttpRequestInterceptor requestInterceptor = new ApacheHttpRequestInterceptor(
            LoggerConfig.builder()
                .logger(logWriter)
                .build());

        ApacheHttpResponseInterceptor responseInterceptor = new ApacheHttpResponseInterceptor(
            LoggerConfig.builder()
                .logger(logWriter)
                .build());

        log.debug("Sending request.");
        defaultApacheClientWithInterceptors(requestInterceptor, responseInterceptor)
            .execute(request);
    }

    List<String> interceptedRequest(String loggerVersion, boolean provideExecutor,
        String content, String contentType, boolean preserveTrailingSpaces) throws IOException {

        return interceptedRequest(loggerVersion, provideExecutor, content, contentType,
            preserveTrailingSpaces, null
        );
    }

    List<String> interceptedRequest(String loggerVersion, boolean provideExecutor,
        String content, String mediaType, boolean preserveTrailingSpaces, Integer maxLineLength)
        throws IOException {

        server.enqueue(new MockResponse().setResponseCode(200));
        final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

        LoggerConfigBuilder builder = LoggerConfig.builder()
            .withThreadInfo(true)
            .logger(testLogger);

        if (Objects.nonNull(maxLineLength)) {
            builder.maxLineLength(maxLineLength);
        }

        if (provideExecutor) {
            builder.executor(new ThreadPoolExecutor(1, 1,
                50L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()));
        }

        LoggerConfig loggerConfig = builder.build();

        InterceptorVersion interceptorVersion = parse(loggerVersion);
        switch (interceptorVersion) {
            case OKHTTP:
                com.squareup.okhttp.Request okHttpRequest = new com.squareup.okhttp.Request.Builder()
                    .url(String.valueOf(server.url("/")))
                    .put(com.squareup.okhttp.RequestBody.create(
                        com.squareup.okhttp.MediaType.parse(mediaType), content))
                    .build();

                OkHttpLoggingInterceptor okhttpLoggingInterceptor
                    = new OkHttpLoggingInterceptor(loggerConfig);

                defaultOkHttpClientWithInterceptor(okhttpLoggingInterceptor)
                    .newCall(okHttpRequest)
                    .execute();

                return testLogger.loggerOutput(preserveTrailingSpaces);

            case OKHTTP3:
                Request okHttp3Request2 = new Request.Builder()
                    .url(String.valueOf(server.url("/")))
                    .put(RequestBody.create(MediaType.parse(mediaType), content))
                    .build();

                OkHttp3LoggingInterceptor okhttp3LoggingInterceptor
                    = new OkHttp3LoggingInterceptor(loggerConfig);

                defaultOkHttp3ClientWithInterceptor(okhttp3LoggingInterceptor)
                    .newCall(okHttp3Request2)
                    .execute();

                return testLogger.loggerOutput(preserveTrailingSpaces);

            case APACHE_HTTPCLIENT_REQUEST:
                final ApacheHttpRequestInterceptor requestInterceptor
                    = new ApacheHttpRequestInterceptor(loggerConfig);

                final ApacheHttpResponseInterceptor responseInterceptor
                    = new ApacheHttpResponseInterceptor(loggerConfig);

                final HttpPut httpPut = new HttpPut(server.url("/").uri());

                ContentType contentType2 = ContentType.create(mediaType);

                final HttpEntity entity = new StringEntity(content, contentType2);

                httpPut.setEntity(entity);
                httpPut.setHeader(new BasicHeader("Content-Type", mediaType));
                defaultApacheClientWithInterceptors(requestInterceptor, responseInterceptor)
                    .execute(httpPut);

                return testLogger.loggerOutput(preserveTrailingSpaces);

            default:
                fail("Unknown interceptor version: " + interceptorVersion);
                return Arrays.asList(new String[1]);
        }
    }

    List<String> interceptedResponse(String loggerVersion, boolean provideExecutors,
        String content, String contentType, boolean preserveTrailingSpaces) throws IOException {
        return interceptedResponse(loggerVersion, provideExecutors, content, contentType, null,
            preserveTrailingSpaces);
    }

    List<String> interceptedResponse(String loggerVersion, boolean provideExecutors,
        String body, String contentType, Integer maxLineLength, boolean preserveTrailingSpaces)
        throws IOException {

        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", contentType)
            .setBody(body));

        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
        LoggerConfigBuilder builder = LoggerConfig.builder()
            .withThreadInfo(true)
            .logger(testLogger);

        if (provideExecutors) {
            builder.executor(new ThreadPoolExecutor(1, 1,
                50L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()));
        }

        if (Objects.nonNull(maxLineLength)) {
            builder.maxLineLength(maxLineLength);
        }

        LoggerConfig loggerConfig = builder.build();

        InterceptorVersion interceptorVersion = parse(loggerVersion);
        switch (interceptorVersion) {
            case OKHTTP:
                OkHttpLoggingInterceptor okhttpLoggingInterceptor
                    = new OkHttpLoggingInterceptor(loggerConfig);

                defaultOkHttpClientWithInterceptor(okhttpLoggingInterceptor)
                    .newCall(defaultOkHttpRequest())
                    .execute();
                break;

            case OKHTTP3:
                OkHttp3LoggingInterceptor okhttp3LoggingInterceptor
                    = new OkHttp3LoggingInterceptor(loggerConfig);

                defaultOkHttp3ClientWithInterceptor(okhttp3LoggingInterceptor)
                    .newCall(defaultOkHttp3Request())
                    .execute();
                break;

            case APACHE_HTTPCLIENT_REQUEST:
                final ApacheHttpRequestInterceptor requestInterceptor
                    = new ApacheHttpRequestInterceptor(loggerConfig);

                final ApacheHttpResponseInterceptor responseInterceptor
                    = new ApacheHttpResponseInterceptor(loggerConfig);

                defaultApacheClientWithInterceptors(requestInterceptor, responseInterceptor)
                    .execute(defaultApacheHttpRequest());
                break;

            default:
                fail("Unknown interceptor version: " + interceptorVersion);
                return Arrays.asList(new String[1]);

        }

        return testLogger.loggerOutput(preserveTrailingSpaces);
    }

}
