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

    private static final String JSON_BODY = ""
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

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_JsonRequest(String loggerVersion,
        boolean provideExecutor)
        throws IOException {
        final String[] loggerOutput = interceptedRequest(RequestBody
                .create(MediaType.parse("application/json"), JSON_BODY), loggerVersion,
            provideExecutor);

        assertTrue("Interceptor should be able to log json body.",
            Arrays.asList(loggerOutput).contains("      \"name\": \"doggie\", "));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_JsonResponse(String loggerVersion,
        boolean provideExecutor) throws IOException {
        final String[] loggerOutput = interceptedResponse("application/json", JSON_BODY,
            loggerVersion,
            provideExecutor);

        assertTrue("Interceptor should be able to log json body.",
            Arrays.asList(loggerOutput).contains("      \"name\": \"doggie\", "));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_XmlRequest(String loggerVersion,
        boolean provideExecutor)
        throws IOException {
        final String[] loggerOutput = interceptedRequest(RequestBody
            .create(MediaType.parse("application/xml"), XML_BODY), loggerVersion, provideExecutor);

        assertTrue("Interceptor should be able to handle xml request body.",
            Arrays.asList(loggerOutput)
                .contains("  <?xml version=\"1.0\" encoding=\"UTF-16\"?> "));
        assertTrue("Interceptor should be able to handle xml request body.",
            Arrays.asList(loggerOutput).contains("  </mammals> "));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_XmlResponse(String loggerVersion,
        boolean provideExecutor)
        throws IOException {
        final String[] loggerOutput = interceptedResponse("application/xml", XML_BODY,
            loggerVersion,
            provideExecutor);

        assertTrue("Interceptor should be able to handle xml response body.",
            Arrays.asList(loggerOutput).contains("  <?xml version=\"1.0\" encoding=\"UTF-16\"?> "));
        assertTrue("Interceptor should be able to handle xml response body.",
            Arrays.asList(loggerOutput).contains("  </mammals> "));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_MalformedXmlRequest(String loggerVersion,
        boolean provideExecutor) throws IOException {
        final String[] loggerOutput = interceptedRequest(RequestBody
                .create(MediaType.parse("application/xml"), MALFORMED_XML_BODY), loggerVersion,
            provideExecutor);

        assertTrue("Interceptor should be able to handle malformed xml body.",
            Arrays.asList(loggerOutput)
                .contains("  <?xml version=\"1.0\"?><mammals><animal id=\"0\" "
                    + "species=\"Capra hircus\">Goat</animal>animal id=\"1\" species=\"Panthe "));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_MalformedXmlResponse(String loggerVersion,
        boolean provideExecutor) throws IOException {
        final String[] loggerOutput = interceptedResponse("application/xml", MALFORMED_XML_BODY,
            loggerVersion, provideExecutor);

        assertTrue("Interceptor should be able to handle xml response body.",
            Arrays.asList(loggerOutput)
                .contains("  <?xml version=\"1.0\"?><mammals><animal id=\"0\" "
                    + "species=\"Capra hircus\">Goat</animal>animal id=\"1\" species=\"Panthe "));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_FileRequest(String loggerVersion,
        boolean provideExecutor)
        throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse("application/zip"),
            createFileFromString(JSON_BODY));

        final String[] loggerOutput = interceptedRequest(body, loggerVersion, provideExecutor);

        assertTrue("Interceptor should not log file request body.",
            Arrays.asList(loggerOutput).contains("  Omitted response body "));
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_FileResponse(String loggerVersion,
        boolean provideExecutor) throws IOException {
        final String[] loggerOutput = interceptedResponse("application/zip",
            String.valueOf(createFileFromString(JSON_BODY)), loggerVersion, provideExecutor);

        assertTrue("Interceptor should not log file response body.",
            Arrays.asList(loggerOutput).contains("  Omitted response body "));
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

    private String[] interceptedRequest(RequestBody body, String interceptorVersion,
        boolean provideExecutor) throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        final TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);

        Request okhttp3Request = new Request.Builder()
            .url(String.valueOf(server.url("/")))
            .put(body)
            .build();

        final LoggingInterceptor.Builder builder = new LoggingInterceptor.Builder()
            .logger(testLogger);
        if (provideExecutor) {
            builder.executor(Executors.newSingleThreadExecutor());
        }
        if (interceptorVersion.equals(InterceptorVersion.OKHTTP3.getName())) {
            Okhttp3LoggingInterceptor interceptor = builder
                .buildForOkhttp3();

            defaultOkhttp3ClientWithInterceptor(interceptor)
                .newCall(okhttp3Request)
                .execute();
            return testLogger.outputAsArray();
        } else if (interceptorVersion.equals(InterceptorVersion.OKHTTP.getName())) {
            OkhttpLoggingInterceptor interceptor = builder
                .buildForOkhttp();

            final com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                .url(String.valueOf(server.url("/")))
                .put(convertOkhtt3pRequestBody(okhttp3Request))
                .build();

            defaultOkhttpClientWithInterceptor(interceptor)
                .newCall(request)
                .execute();
            return testLogger.outputAsArray();
        } else if (interceptorVersion
            .equals(InterceptorVersion.APACHE_HTTPCLIENT_REQUEST.getName())) {
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
            return testLogger.outputAsArray();
        } else {
            fail(String
                .format("I didn't recognize %s version. I support 'okhttp' and 'okhttp3' versions",
                    interceptorVersion));
            return new String[1];
        }
    }

    private String[] interceptedResponse(String contentType, String body, String interceptorVersion,
        boolean provideExecutors) throws IOException {
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
        if (interceptorVersion.equals(InterceptorVersion.OKHTTP.getName())) {
            OkhttpLoggingInterceptor interceptor = builder
                .buildForOkhttp();
            defaultOkhttpClientWithInterceptor(interceptor)
                .newCall(defaultOkhttpRequest())
                .execute();
        } else if (interceptorVersion.equals(InterceptorVersion.OKHTTP3.getName())) {
            Okhttp3LoggingInterceptor interceptor = builder
                .buildForOkhttp3();

            defaultOkhttp3ClientWithInterceptor(interceptor)
                .newCall(defaultOkhttp3Request())
                .execute();
        } else if (interceptorVersion
            .equals(InterceptorVersion.APACHE_HTTPCLIENT_REQUEST.getName())) {
            final ApacheHttpRequestInterceptor requestInterceptor = builder
                .buildForApacheHttpClientRequest();

            final ApacheHttpResponseInterceptor responseInterceptor = builder
                .buildFordApacheHttpClientResponse();

            defaultApacheClientWithInterceptors(requestInterceptor, responseInterceptor)
                .execute(defaultApacheHttpRequest());
        } else {
            fail(String.format(
                "I couldn't recognize %s interceptor version. I only support okhttp and okhttp3 versions at the moment",
                interceptorVersion));
            return new String[1];
        }
        return testLogger.outputAsArray();
    }

}
