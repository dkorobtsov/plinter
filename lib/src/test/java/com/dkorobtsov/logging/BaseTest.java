package com.dkorobtsov.logging;

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
import java.util.List;
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
import okhttp3.Response;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Rule;

public abstract class BaseTest {

    private static final org.apache.logging.log4j.Logger logger
        = org.apache.logging.log4j.LogManager.getLogger(BaseTest.class.getName());

    private static final ConnectionPool CONNECTION_POOL = new ConnectionPool();
    private static final Dispatcher DISPATCHER = new Dispatcher();
    private static final int MAX_IDLE_CONNECTIONS = 10;
    private static final int KEEP_ALIVE_DURATION_MS = 60 * 1000;

    @Rule
    public MockWebServer server = new MockWebServer();

    @Before
    public void cleanAnyExistingJavaUtilityLoggingConfigurations() {
        LogManager.getLogManager().reset();
        Logger globalLogger = Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
        globalLogger.setLevel(Level.OFF);
    }

    /**
     * Returns list of parameters for data driven tests.
     *
     * Format: "Interceptor name, should use manually provided executor?"
     *
     * For valid interceptor names please check: {@link InterceptorVersion}
     *
     * NB: In IDE current method shown as unused, but it's refereed in @Parameters annotation in
     * child classes.
     */
    String[] interceptorsWithExecutors() {
        return new String[]{
            "okhttp, true", "okhttp, false",
            "okhttp3, true", "okhttp3, false",
            "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
        };
    }

    /**
     * Returns list of parameters for data driven tests.
     *
     * Format: "Interceptor name 1", "Interceptor name 2" etc
     *
     * For valid interceptor names please check: {@link InterceptorVersion}
     *
     * NB: In IDE current method shown as unused, but it's refereed in @Parameters annotation in
     * child classes.
     */
    String[] interceptors() {
        return new String[]{
            "okhttp", "okhttp3", "apacheHttpclientRequest"
        };
    }

    List<String> interceptedRequest(String loggerVersion, boolean withExecutor,
        String content, String mediaType, boolean preserveTrailingSpaces) {

        server.enqueue(new MockResponse().setResponseCode(200));

        final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
        LoggerConfig loggerConfig = defaultLoggerConfig(testLogger, withExecutor);

        interceptWithConfig(loggerVersion, loggerConfig, content, mediaType);

        return testLogger.loggerOutput(preserveTrailingSpaces);
    }

    List<String> interceptedResponse(String loggerVersion, boolean withExecutor,
        String body, String contentType, boolean preserveTrailingSpaces) {

        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", contentType)
            .setBody(body));

        final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
        LoggerConfig loggerConfig = defaultLoggerConfig(testLogger, withExecutor);

        interceptWithConfig(loggerVersion, loggerConfig);

        return testLogger.loggerOutput(preserveTrailingSpaces);
    }

    /**
     * Default Logger configuration for use in tests.
     *
     * @param testLogger Test logger instance.
     * @param withExecutor If specified, intercepted traffic will be printed in separate thread.
     */
    LoggerConfig defaultLoggerConfig(TestLogger testLogger, boolean withExecutor) {
        LoggerConfigBuilder builder = LoggerConfig.builder()
            .withThreadInfo(true)
            .logger(testLogger);

        if (withExecutor) {
            builder.executor(new ThreadPoolExecutor(1, 1,
                50L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()));
        }

        return builder.build();
    }

    void interceptWithConfig(String interceptor, LoggerConfig loggerConfig) {
        interceptWithConfig(interceptor, loggerConfig, null, null);
    }

    void interceptWithConfig(String interceptor, LoggerConfig loggerConfig,
        String content, String mediaType) {

        switch (InterceptorVersion.parse(interceptor)) {
            case OKHTTP:
                logger.info("OkHttp Interceptor: {}",
                    loggerConfig.toString());

                executeOkHttpRequest(
                    defaultOkHttpClient(new OkHttpLoggingInterceptor(loggerConfig)),
                    okHttpRequest(content, mediaType));
                break;

            case OKHTTP3:
                logger.info("OkHttp Interceptor: {}",
                    loggerConfig.toString());

                executeOkHttp3Request(
                    defaultOkHttp3Client(new OkHttp3LoggingInterceptor(loggerConfig)),
                    okHttp3Request(content, mediaType));
                break;

            case APACHE_HTTPCLIENT_REQUEST:
                logger.info("Apache Interceptors: {}",
                    loggerConfig.toString());

                executeApacheRequest(defaultApacheClient(
                    new ApacheHttpRequestInterceptor(loggerConfig),
                    new ApacheHttpResponseInterceptor(loggerConfig)),
                    apacheHttpRequest(content, mediaType));
                break;

            default:
                fail("Unknown interceptor version: " + interceptor);
                break;
        }
    }

    Response executeOkHttp3Request(
        OkHttpClient client, Request request) {
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            logger.error(e);
        }
        return null;
    }

    com.squareup.okhttp.Response executeOkHttpRequest(
        com.squareup.okhttp.OkHttpClient client,
        com.squareup.okhttp.Request request) {
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            logger.error(e);
        }
        return null;
    }

    HttpResponse executeApacheRequest(HttpClient client,
        HttpUriRequest request) {
        try {
            return client.execute(request);
        } catch (IOException e) {
            logger.error(e);
        }
        return null;
    }

    /**
     * Returns OkHttp3 client for use in tests.
     */
    OkHttpClient defaultOkHttp3Client(Interceptor interceptor) {
        return new OkHttpClient.Builder()
            .connectionPool(CONNECTION_POOL)
            .dispatcher(DISPATCHER)
            .addNetworkInterceptor(interceptor)
            .build();
    }

    /**
     * Returns Apache client for use in tests.
     */
    HttpClient defaultApacheClient(ApacheHttpRequestInterceptor requestInterceptor,
        ApacheHttpResponseInterceptor responseInterceptor) {
        return HttpClientBuilder
            .create()
            .addInterceptorFirst(requestInterceptor)
            .addInterceptorFirst(responseInterceptor)
            .setMaxConnTotal(MAX_IDLE_CONNECTIONS)
            .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
            .build();
    }

    /**
     * Returns OkHttp client for use in tests.
     */
    com.squareup.okhttp.OkHttpClient defaultOkHttpClient(
        com.squareup.okhttp.Interceptor interceptor) {
        final com.squareup.okhttp.OkHttpClient okHttpClient = new com.squareup.okhttp.OkHttpClient()
            .setConnectionPool(
                new com.squareup.okhttp.ConnectionPool(MAX_IDLE_CONNECTIONS,
                    KEEP_ALIVE_DURATION_MS))
            .setDispatcher(new com.squareup.okhttp.Dispatcher());
        okHttpClient.interceptors().add(interceptor);
        return okHttpClient;
    }

    /**
     * Returns default OkHttp3 request for use in tests.
     *
     * @param content Request body content as String. Can be null.
     * @param mediaType Request body media type. Can be null.
     *
     * To add body to request both content and media type should be non null, otherwise request will
     * be empty.
     */
    Request okHttp3Request(String content, String mediaType) {
        Request.Builder requestBuilder = new Request.Builder()
            .url(String.valueOf(server.url("/")));

        if (nonNull(content) && nonNull(mediaType)) {
            requestBuilder.put(RequestBody.create(
                MediaType.parse(mediaType), content));
        }
        return requestBuilder.build();
    }

    /**
     * Returns default Apache HTTP request for use in tests.
     *
     * @param content Request body content as String. Can be null.
     * @param mediaType Request body media type. Can be null.
     *
     * To add body to request both content and media type should be non null, otherwise request will
     * be empty.
     */
    private HttpUriRequest apacheHttpRequest(String content, String mediaType) {
        final HttpPut httpPut = new HttpPut(server.url("/").uri());

        if (nonNull(content) && nonNull(mediaType)) {
            ContentType contentType = ContentType.create(mediaType);

            final HttpEntity entity = new StringEntity(content, contentType);

            httpPut.setEntity(entity);
            httpPut.setHeader(new BasicHeader("Content-Type", mediaType));
        }
        return httpPut;
    }

    /**
     * Returns default OkHttp request for use in tests.
     *
     * @param content Request body content as String. Can be null.
     * @param mediaType Request body media type. Can be null.
     *
     * To add body to request both content and media type should be non null, otherwise request will
     * be empty.
     */
    private com.squareup.okhttp.Request okHttpRequest(String content, String mediaType) {
        com.squareup.okhttp.Request.Builder requestBuilder
            = new com.squareup.okhttp.Request.Builder()
            .url(String.valueOf(server.url("/")));

        if (nonNull(content) && nonNull(mediaType)) {
            requestBuilder.put(com.squareup.okhttp.RequestBody.create(
                com.squareup.okhttp.MediaType.parse(mediaType), content));
        }
        return requestBuilder.build();
    }


}
