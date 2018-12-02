package com.dkorobtsov.logging;

import static org.junit.Assert.assertTrue;

import com.dkorobtsov.logging.utils.InterceptorVersion;
import com.dkorobtsov.logging.utils.TestLogger;
import com.squareup.okhttp.mockwebserver.MockResponse;
import java.io.IOException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class MalformedJsonHandlingTest extends BaseTest {

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void interceptorAbleToHandleBody_malformedJsonResponse(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("? \"test\" : \"test1\"}"));

        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
        attachLoggerToInterceptorWithDefaultRequest(interceptorVersion, testLogger);

        assertTrue("Interceptor should be able to log malformed json body.",
            testLogger.formattedOutput().contains("? \"test\" : \"test1\"}"));
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void interceptorAbleToHandleBody_malformedJsonRequest(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
        final String content = "? \"test\" : \"test1\"}";
        if (interceptorVersion.equals(InterceptorVersion.OKHTTP3.getName())) {
            Request okHttp3Request = new Request.Builder()
                .url(String.valueOf(server.url("/")))
                .put(RequestBody.create(MediaType.parse("application/json"),
                    content))
                .build();
            attachLoggerToInterceptor(interceptorVersion, testLogger, okHttp3Request, null, null);
        } else if (interceptorVersion.equals(InterceptorVersion.OKHTTP.getName())) {
            com.squareup.okhttp.Request okhttpRequest = new com.squareup.okhttp.Request.Builder()
                .url(String.valueOf(server.url("/")))
                .put(com.squareup.okhttp.RequestBody
                    .create(com.squareup.okhttp.MediaType.parse("application/json"), content))
                .build();
            attachLoggerToInterceptor(interceptorVersion, testLogger, null, okhttpRequest, null);
        } else {
            final HttpPut httpPut = new HttpPut(server.url("/").uri());
            httpPut.setEntity(new StringEntity(content));
            httpPut.setHeader(new BasicHeader("Content-Type", "application/json"));
            attachLoggerToInterceptor(interceptorVersion, testLogger, null, null, httpPut);
        }
        assertTrue("Interceptor should be able to log malformed json body.",
            testLogger.formattedOutput().contains(content));
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void interceptorAbleToHandleBody_jsonArrayResponse(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("[{\"test1\": \"test1\"}, {\"test2\": \"test2\"}]"));

        TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
        attachLoggerToInterceptorWithDefaultRequest(interceptorVersion, testLogger);

        assertTrue("Interceptor should be able to log malformed json body.",
            testLogger.formattedOutput()
                .contains("{\"test1\": \"test1\"},"));
        assertTrue("Interceptor should be able to log malformed json body.",
            testLogger.formattedOutput()
                .contains("{\"test2\": \"test2\"}"));
    }

    @Test
    @Parameters({
        "okhttp", "okhttp3", "apacheHttpclientRequest"
    })
    public void interceptorAbleToHandleBody_jsonArrayRequest(String interceptorVersion)
        throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200));
        final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

        final String content = "[{\"test1\": \"test1\"}, {\"test2\": \"test2\"}]";
        if (interceptorVersion.equals(InterceptorVersion.OKHTTP3.getName())) {
            Request okHttp3Request = new Request.Builder()
                .url(String.valueOf(server.url("/")))
                .put(RequestBody.create(MediaType.parse("application/json"),
                    content))
                .build();
            attachLoggerToInterceptor(interceptorVersion, testLogger, okHttp3Request, null, null);
        } else if (interceptorVersion.equals(InterceptorVersion.OKHTTP.getName())) {
            com.squareup.okhttp.Request okhttpRequest = new com.squareup.okhttp.Request.Builder()
                .url(String.valueOf(server.url("/")))
                .put(com.squareup.okhttp.RequestBody
                    .create(com.squareup.okhttp.MediaType.parse("application/json"),
                        content))
                .build();
            attachLoggerToInterceptor(interceptorVersion, testLogger, null, okhttpRequest, null);
        } else {
            final HttpPut httpPut = new HttpPut(server.url("/").uri());
            httpPut.setEntity(new StringEntity(content));
            httpPut.setHeader(new BasicHeader("Contety-Tyoe", "application/json"));
            attachLoggerToInterceptor(interceptorVersion, testLogger, null, null, httpPut);
        }

        assertTrue("Interceptor should be able to log malformed json body.",
            testLogger.formattedOutput()
                .contains("{\"test1\": \"test1\"},"));
        assertTrue("Interceptor should be able to log malformed json body.",
            testLogger.formattedOutput()
                .contains("{\"test2\": \"test2\"}"));
    }

}
