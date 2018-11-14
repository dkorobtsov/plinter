package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.converters.ToOkhttpConverter.convertOkhtt3pRequestBody;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.dkorobtsov.logging.converters.ToApacheHttpClientConverter;
import com.dkorobtsov.logging.interceptors.ApacheHttpRequestInterceptor;
import com.dkorobtsov.logging.interceptors.ApacheHttpResponseInterceptor;
import com.dkorobtsov.logging.interceptors.Okhttp3LoggingInterceptor;
import com.dkorobtsov.logging.interceptors.OkhttpLoggingInterceptor;
import com.squareup.okhttp.mockwebserver.MockResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class InterceptorBodyHandlingTest extends BaseTest {

    private static final String SIMPLE_JSON = "{name: \"John\", age: 31, city: \"New York\"}";

    private static final String PREFORMATTED_JSON_BODY = ""
        + "  {\n"
        + "    \"id\": 431169,\n"
        + "    \"category\": {\n"
        + "      \"id\": 0,\n"
        + "      \"name\": \"string\"\n"
        + "    },\n"
        + "    \"name\": \"doggie\",\n"
        + "    \"photoUrls\": [\n"
        + "      \"string\"\n"
        + "    ],\n"
        + "    \"tags\": [\n"
        + "      {\n"
        + "        \"id\": 0,\n"
        + "        \"name\": \"string\"\n"
        + "      }\n"
        + "    ],\n"
        + "    \"status\": \"available\"\n"
        + "  }";

    private static final String MALFORMED_JSON_BODY = ""
        + "  {\n"
        + "    \"id\": 431169,\n"
        + "    \"category\": {\n"
        + "      \"id\": 0,\n"
        + "      \"name\": \"string\"\n"
        + "    \"name\": \"doggie\",\n"
        + "    \"photoUrls\": [\n"
        + "      \"string\"\n"
        + "    \"tags\": [\n"
        + "      {\n"
        + "        \"id\": 0,\n"
        + "        \"name\": \"string\"\n"
        + "      }\n"
        + "    ],\n"
        + "    \"status\": \"available\"\n"
        + "  }";

    private static final String XML_BODY =
        "<?xml version=\"1.0\" encoding=\"UTF-16\"?>"
            + "<mammals>"
            + "<animal id=\"0\" species=\"Capra hircus\">Goat</animal>"
            + "<animal id=\"1\" species=\"Panthera pardus\">Leopard</animal>"
            + "<animal id=\"2\" species=\"Equus zebra\">Zebra</animal> "
            + "</mammals>";

    private static final String MALFORMED_XML_BODY =
        "<?xml version=\"1.0\" encoding=\"UTF-16\"?>"
            + "<mammals>"
            + "<animal id=\"0\" species=\"Capra hircus\">Goat</animal>"
            + "animal id=\"1\" species=\"Panthera pardus\">Leopard</animal>"
            + "<animal id=\"2\" species=\"Equus zebra\">Zebra</animal> "
            + "</mammals>";

    private static final String HTML_BODY = ""
        + "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>"
        + "<title>Error 404 Not Found</title></head><body>"
        + "<div style=\"font-family:Arial,Helvetica,sans-serif;\"><h2>HTTP ERROR 404</h2>"
        + "<pre>Not Found</pre></div><tr><th></th><th></th><th></th><th></th></tr></body></html>";

    private static final String MALFORMED_HTML_BODY = ""
        + "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>"
        + "<title>Error 404 Not Found</title></head><body>"
        + "<div style=\"font-family:Arial,Helvetica,sans-serif;\"><h2>HTTP ERROR 404</h2>"
        + "<pre>Not Found</pre></div><tr><th></th><th><th></th><th></th></tr></body></html>";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_simpleJsonRequest(String loggerVersion,
        boolean provideExecutor)
        throws IOException {
        final List<String> loggerOutput = interceptedRequest(RequestBody
                .create(MediaType.parse("application/json"), SIMPLE_JSON),
            loggerVersion, provideExecutor, true);

        assertTrue("Interceptor should be able to log simple json body.",
            loggerOutput.contains("     \"city\": \"New York\", "));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_simpleJsonResponse(String loggerVersion,
        boolean provideExecutor) throws IOException {
        final List<String> loggerOutput = interceptedResponse("application/json", SIMPLE_JSON,
            loggerVersion, provideExecutor, true);

        assertTrue("Interceptor should be able to log json body.",
            loggerOutput.contains("     \"city\": \"New York\", "));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_preformattedJsonRequest(String loggerVersion,
        boolean provideExecutor)
        throws IOException {
        final List<String> loggerOutput = interceptedRequest(RequestBody
                .create(MediaType.parse("application/json"), PREFORMATTED_JSON_BODY),
            loggerVersion, provideExecutor, true);

        assertTrue("Interceptor should be able to log json body.",
            loggerOutput.contains("     \"name\": \"doggie\", "));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_preformattedJsonResponse(String loggerVersion,
        boolean provideExecutor) throws IOException {
        final List<String> loggerOutput = interceptedResponse("application/json",
            PREFORMATTED_JSON_BODY,
            loggerVersion, provideExecutor, true);

        assertTrue("Interceptor should be able to log json body.",
            loggerOutput.contains("     \"name\": \"doggie\", "));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_malformedJsonRequest(String loggerVersion,
        boolean provideExecutor) throws IOException {
        final List<String> loggerOutput = interceptedRequest(RequestBody
                .create(MediaType.parse("application/json"), MALFORMED_JSON_BODY), loggerVersion,
            provideExecutor, false);

        loggerOutput
            .stream()
            .filter(it ->
                it.startsWith("\"status\": \"available\""))
            .findFirst()
            .orElseThrow(() ->
                new AssertionError("Interceptor should be able to handle xml response body."));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_malformedJsonResponse(String loggerVersion,
        boolean provideExecutor) throws IOException {
        final List<String> loggerOutput = interceptedResponse(
            "application/json", MALFORMED_JSON_BODY, loggerVersion, provideExecutor, false);

        loggerOutput
            .stream()
            .filter(it ->
                it.startsWith("\"status\": \"available\""))
            .findFirst()
            .orElseThrow(() ->
                new AssertionError("Interceptor should be able to handle xml response body."));
    }

    @Test
    @Parameters({
        // "okhttp, true" - disabled since randomly fails on wercker for unknown reason
        "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_htmlRequest(String loggerVersion,
        boolean provideExecutor)
        throws IOException {
        final List<String> loggerOutput = interceptedRequest(RequestBody
                .create(MediaType.parse("text/html"), HTML_BODY),
            loggerVersion, provideExecutor, false);

        assertTrue("Interceptor should be able to handle html request body.",
            loggerOutput
                .contains(
                    "<title>Error 404 Not Found</title>"));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_htmlResponse(String loggerVersion,
        boolean provideExecutor)
        throws IOException {
        final List<String> loggerOutput = interceptedResponse(
            "text/html", HTML_BODY, loggerVersion, provideExecutor, false);

        assertTrue("Interceptor should be able to handle html request body.",
            loggerOutput
                .contains(
                    "<title>Error 404 Not Found</title>"));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_malformedHtmlRequest(String loggerVersion,
        boolean provideExecutor) throws IOException {
        final List<String> loggerOutput = interceptedRequest(RequestBody
                .create(MediaType.parse("text/html"), MALFORMED_HTML_BODY), loggerVersion,
            provideExecutor, false);

        loggerOutput
            .stream()
            .filter(it ->
                it.startsWith(
                    "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>"))
            .findFirst()
            .orElseThrow(() ->
                new AssertionError("Interceptor should be able to handle html request body."));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_malformedHtmlResponse(String loggerVersion,
        boolean provideExecutor) throws IOException {
        final List<String> loggerOutput = interceptedResponse(
            "text/html", MALFORMED_HTML_BODY, loggerVersion, provideExecutor, false);

        loggerOutput
            .stream()
            .filter(it ->
                it.startsWith(
                    "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>"))
            .findFirst()
            .orElseThrow(() ->
                new AssertionError("Interceptor should be able to handle html response body."));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_xmlRequest(String loggerVersion,
        boolean provideExecutor)
        throws IOException {
        final List<String> loggerOutput = interceptedRequest(RequestBody
                .create(MediaType.parse("application/xml"), XML_BODY),
            loggerVersion, provideExecutor, false);

        assertTrue("Interceptor should be able to handle xml request body.",
            loggerOutput
                .contains("<?xml version=\"1.0\" encoding=\"UTF-16\"?>"));
        assertTrue("Interceptor should be able to handle xml request body.",
            loggerOutput.contains("</mammals>"));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_xmlResponse(String loggerVersion,
        boolean provideExecutor)
        throws IOException {
        final List<String> loggerOutput = interceptedResponse("application/xml", XML_BODY,
            loggerVersion,
            provideExecutor, false);

        assertTrue("Interceptor should be able to handle xml response body.",
            loggerOutput.contains("<?xml version=\"1.0\" encoding=\"UTF-16\"?>"));
        assertTrue("Interceptor should be able to handle xml response body.",
            loggerOutput.contains("</mammals>"));
    }

    @Test
    @Parameters({
        // "okhttp, true" - disabled since randomly fails on wercker for unknown reason
        "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_malformedXmlRequest(String loggerVersion,
        boolean provideExecutor) throws IOException {
        final List<String> loggerOutput = interceptedRequest(RequestBody
                .create(MediaType.parse("application/xml"), MALFORMED_XML_BODY), loggerVersion,
            provideExecutor, false);

        loggerOutput
            .stream()
            .filter(it ->
                it.startsWith(
                    "<?xml version=\"1.0\" encoding=\"UTF-16\"?><mammals>"))
            .findFirst()
            .orElseThrow(() ->
                new AssertionError("Interceptor should be able to handle xml request body."));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_malformedXmlResponse(String loggerVersion,
        boolean provideExecutor) throws IOException {
        final List<String> loggerOutput = interceptedResponse("application/xml", MALFORMED_XML_BODY,
            loggerVersion, provideExecutor, false);

        loggerOutput
            .stream()
            .filter(it ->
                it.startsWith(
                    "<?xml version=\"1.0\" encoding=\"UTF-16\"?><mammals>"))
            .findFirst()
            .orElseThrow(() ->
                new AssertionError("Interceptor should be able to handle xml response body."));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_fileRequest(String loggerVersion,
        boolean provideExecutor)
        throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse("application/zip"),
            createFileFromString(PREFORMATTED_JSON_BODY));

        final List<String> loggerOutput = interceptedRequest(body, loggerVersion, provideExecutor,
            true);

        assertTrue("Interceptor should not log file request body.",
            loggerOutput.contains("  Omitted response body "));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_fileResponse(String loggerVersion,
        boolean provideExecutor) throws IOException {
        final List<String> loggerOutput = interceptedResponse("application/zip",
            String.valueOf(createFileFromString(PREFORMATTED_JSON_BODY)), loggerVersion,
            provideExecutor, true);

        assertTrue("Interceptor should not log file response body.",
            loggerOutput.contains("  Omitted response body "));
    }

    @SuppressWarnings("SameParameterValue")
    private File createFileFromString(String val) throws IOException {
        File file = temporaryFolder.newFile();
        byte[] bytes = val.getBytes(StandardCharsets.UTF_8);
        Source source = Okio.source(new ByteArrayInputStream(bytes));
        try (BufferedSink b = Okio.buffer(Okio.sink(file))) {
            b.writeAll(source);
        }
        return file;
    }

    private List<String> interceptedRequest(RequestBody body, String loggerVersion,
        boolean provideExecutor, boolean preserveTrailingSpaces) throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        final TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);

        Request okhttp3Request = new Request.Builder()
            .url(String.valueOf(server.url("/")))
            .put(body)
            .build();

        final LoggingInterceptor.Builder builder = new LoggingInterceptor.Builder()
            .logger(testLogger);

        if (provideExecutor) {
            builder.executor(Executors.newCachedThreadPool());
        }

        InterceptorVersion interceptorVersion = InterceptorVersion.parse(loggerVersion);
        switch (interceptorVersion) {
            case OKHTTP:
                OkhttpLoggingInterceptor okhttpLoggingInterceptor = builder
                    .buildForOkhttp();

                final com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(String.valueOf(server.url("/")))
                    .put(convertOkhtt3pRequestBody(okhttp3Request))
                    .build();

                defaultOkhttpClientWithInterceptor(okhttpLoggingInterceptor)
                    .newCall(request)
                    .execute();

                return testLogger.loggerOutput(preserveTrailingSpaces);

            case OKHTTP3:
                Okhttp3LoggingInterceptor okhttp3LoggingInterceptor = builder
                    .buildForOkhttp3();

                defaultOkhttp3ClientWithInterceptor(okhttp3LoggingInterceptor)
                    .newCall(okhttp3Request)
                    .execute();

                return testLogger.loggerOutput(preserveTrailingSpaces);

            case APACHE_HTTPCLIENT_REQUEST:
                final ApacheHttpRequestInterceptor requestInterceptor = builder
                    .buildForApacheHttpClientRequest();

                final ApacheHttpResponseInterceptor responseInterceptor = builder
                    .buildFordApacheHttpClientResponse();

                final HttpPut httpPut = new HttpPut(server.url("/").uri());
                final MediaType mediaType =
                    body.contentType() == null ? MediaType
                        .parse(ContentType.APPLICATION_JSON.toString())
                        : body.contentType();

                ContentType contentType = ContentType.create(
                    String.format("%s/%s", Objects.requireNonNull(mediaType).type(),
                        mediaType.subtype()));

                final HttpEntity entity = ToApacheHttpClientConverter
                    .okhttp3RequestBodyToStringEntity(body, contentType);

                httpPut.setEntity(entity);
                httpPut.setHeader(new BasicHeader("Content-Type", mediaType.toString()));
                defaultApacheClientWithInterceptors(requestInterceptor, responseInterceptor)
                    .execute(httpPut);

                return testLogger.loggerOutput(preserveTrailingSpaces);

            default:
                fail(String
                    .format(
                        "I didn't recognize %s version. I support 'okhttp' and 'okhttp3' versions",
                        loggerVersion));
                return Arrays.asList(new String[1]);
        }
    }

    private List<String> interceptedResponse(String contentType, String body,
        String loggerVersion,
        boolean provideExecutors, boolean preserveTrailingSpaces) throws IOException {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", contentType)
            .setBody(body));

        TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
        final LoggingInterceptor.Builder builder = new LoggingInterceptor.Builder()
            .logger(testLogger);

        if (provideExecutors) {
            builder.executor(Executors.newCachedThreadPool());
        }

        InterceptorVersion interceptorVersion = InterceptorVersion.parse(loggerVersion);
        switch (interceptorVersion) {
            case OKHTTP:
                OkhttpLoggingInterceptor okhttpLoggingInterceptor = builder
                    .buildForOkhttp();
                defaultOkhttpClientWithInterceptor(okhttpLoggingInterceptor)
                    .newCall(defaultOkhttpRequest())
                    .execute();
                break;

            case OKHTTP3:
                Okhttp3LoggingInterceptor okhttp3LoggingInterceptor = builder
                    .buildForOkhttp3();

                defaultOkhttp3ClientWithInterceptor(okhttp3LoggingInterceptor)
                    .newCall(defaultOkhttp3Request())
                    .execute();
                break;

            case APACHE_HTTPCLIENT_REQUEST:
                final ApacheHttpRequestInterceptor requestInterceptor = builder
                    .buildForApacheHttpClientRequest();

                final ApacheHttpResponseInterceptor responseInterceptor = builder
                    .buildFordApacheHttpClientResponse();

                defaultApacheClientWithInterceptors(requestInterceptor, responseInterceptor)
                    .execute(defaultApacheHttpRequest());
                break;

            default:
                fail(String.format(
                    "I couldn't recognize %s interceptor version. I only support okhttp and okhttp3 versions at the moment",
                    loggerVersion));
                return Arrays.asList(new String[1]);

        }

        return testLogger.loggerOutput(preserveTrailingSpaces);
    }

}
