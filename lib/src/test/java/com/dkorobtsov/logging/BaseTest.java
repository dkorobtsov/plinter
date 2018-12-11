package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.utils.InterceptorVersion.parse;
import static java.util.Objects.nonNull;
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

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
        .getLogger(BaseTest.class);

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
                break;

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
                break;

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
                break;

            default:
                fail("Unknown interceptor version: " + interceptorVersion);
                return Arrays.asList(new String[1]);
        }
        return testLogger.loggerOutput(preserveTrailingSpaces);
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

        if (nonNull(maxLineLength)) {
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

    void interceptWithConfig(String interceptor, LoggerConfig loggerConfig)
        throws IOException {

        switch (InterceptorVersion.parse(interceptor)) {
            case OKHTTP3:
                OkHttp3LoggingInterceptor okHttp3LoggingInterceptor
                    = new OkHttp3LoggingInterceptor(loggerConfig);

                logger.info("OkHttp3 Interceptor: {}",
                    okHttp3LoggingInterceptor.loggerConfig().toString());

                defaultOkHttp3ClientWithInterceptor(okHttp3LoggingInterceptor)
                    .newCall(defaultOkHttp3Request())
                    .execute();
                break;

            case OKHTTP:
                final OkHttpLoggingInterceptor okHttpLoggingInterceptor
                    = new OkHttpLoggingInterceptor(loggerConfig);

                logger.info("OkHttp Interceptor: {}",
                    okHttpLoggingInterceptor.loggerConfig().toString());

                defaultOkHttpClientWithInterceptor(okHttpLoggingInterceptor)
                    .newCall(defaultOkHttpRequest())
                    .execute();
                break;

            case APACHE_HTTPCLIENT_REQUEST:
                final ApacheHttpRequestInterceptor requestInterceptor
                    = new ApacheHttpRequestInterceptor(loggerConfig);

                final ApacheHttpResponseInterceptor responseInterceptor
                    = new ApacheHttpResponseInterceptor(loggerConfig);

                logger.info("Apache Request Interceptor: {}",
                    requestInterceptor.loggerConfig().toString());
                logger.info("Apache Response Interceptor: {}",
                    responseInterceptor.loggerConfig().toString());

                defaultApacheClientWithInterceptors(requestInterceptor, responseInterceptor)
                    .execute(defaultApacheHttpRequest());
                break;

            default:
                fail("Unknown interceptor version: " + interceptor);
                break;
        }
    }

}
