package com.dkorobtsov.logging;

import com.squareup.okhttp.mockwebserver.MockResponse;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
@RunWith(JUnitParamsRunner.class)
public class MalformedJsonHandlingTest extends BaseTest {

  @Test
  @Parameters({
      "okhttp", "okhttp3"
  })
  public void interceptorAbleToHandleBody_malformedJsonResponse(String interceptorVersion) throws IOException {
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody("? \"test\" : \"test1\"}"));

    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
    attachLoggerToInterceptorWithDefaultRequest(interceptorVersion, testLogger);

    assertTrue("Interceptor should be able to log malformed json body.",
        testLogger.formattedOutput().contains("? \"test\" : \"test1\"}"));
  }

  @Test
  @Parameters({
      "okhttp", "okhttp3"
  })
  public void interceptorAbleToHandleBody_malformedJsonRequest(String interceptorVersion) throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
    if (interceptorVersion.equals(InterceptorVersion.OKHTTP3.getName())) {
      Request okhttp3Request = new Request.Builder()
          .url(String.valueOf(server.url("/")))
          .put(RequestBody.create(MediaType.parse("application/json"),
              "? \"test\" : \"test1\"}"))
          .build();
      attachLoggerToInterceptor(interceptorVersion, testLogger, okhttp3Request, null);
    } else {
      com.squareup.okhttp.Request okhttpRequest = new com.squareup.okhttp.Request.Builder()
        .url(String.valueOf(server.url("/")))
        .put(com.squareup.okhttp.RequestBody.create(com.squareup.okhttp.MediaType.parse("application/json"), "? \"test\" : \"test1\"}"))
        .build();
      attachLoggerToInterceptor(interceptorVersion, testLogger, null, okhttpRequest);
    }
    assertTrue("Interceptor should be able to log malformed json body.",
        testLogger.formattedOutput().contains("? \"test\" : \"test1\"}"));
  }

  @Test
  @Parameters({
      "okhttp", "okhttp3"
  })
  public void interceptorAbleToHandleBody_jsonArrayResponse(String interceptorVersion) throws IOException {
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody("[{\"test1\": \"test1\"}, {\"test2\": \"test2\"}]"));

    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
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
      "okhttp", "okhttp3"
  })
  public void interceptorAbleToHandleBody_jsonArrayRequest(String interceptorVersion) throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);

    if (interceptorVersion.equals(InterceptorVersion.OKHTTP3.getName())) {
      Request okhttp3Request = new Request.Builder()
          .url(String.valueOf(server.url("/")))
          .put(RequestBody.create(MediaType.parse("application/json"),
              "[{\"test1\": \"test1\"}, {\"test2\": \"test2\"}]"))
          .build();
      attachLoggerToInterceptor(interceptorVersion, testLogger, okhttp3Request, null);
    } else {
      com.squareup.okhttp.Request okhttpRequest = new com.squareup.okhttp.Request.Builder()
          .url(String.valueOf(server.url("/")))
          .put(com.squareup.okhttp.RequestBody.create(com.squareup.okhttp.MediaType.parse("application/json"),
              "[{\"test1\": \"test1\"}, {\"test2\": \"test2\"}]"))
          .build();
      attachLoggerToInterceptor(interceptorVersion, testLogger, null, okhttpRequest);
    }

    assertTrue("Interceptor should be able to log malformed json body.",
        testLogger.formattedOutput()
            .contains("{\"test1\": \"test1\"},"));
    assertTrue("Interceptor should be able to log malformed json body.",
        testLogger.formattedOutput()
            .contains("{\"test2\": \"test2\"}"));
  }

}
