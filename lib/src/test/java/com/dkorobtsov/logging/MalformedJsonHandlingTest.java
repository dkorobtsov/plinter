package com.dkorobtsov.logging;

import static org.junit.Assert.assertTrue;

import com.squareup.okhttp.mockwebserver.MockResponse;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.Test;

public class MalformedJsonHandlingTest extends BaseTest {

  @Test
  public void interceptorAbleToHandleBody_malformedJsonResponse() throws IOException {
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody("? \"test\" : \"test1\"}"));

    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

    assertTrue("Interceptor should be able to log malformed json body.",
        testLogger.formattedOutput().contains("? \"test\" : \"test1\"}"));
  }

  @Test
  public void interceptorAbleToHandleBody_malformedJsonRequest() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);

    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(testLogger)
        .build();

    Request request = new Request.Builder()
        .url(String.valueOf(server.url("/")))
        .put(RequestBody.create(MediaType.parse("application/json"),
            "? \"test\" : \"test1\"}"))
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(request)
        .execute();

    assertTrue("Interceptor should be able to log malformed json body.",
        testLogger.formattedOutput().contains("? \"test\" : \"test1\"}"));
  }

  @Test
  public void interceptorAbleToHandleBody_jsonArrayResponse() throws IOException {
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody("[{\"test1\": \"test1\"}, {\"test2\": \"test2\"}]"));

    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

    assertTrue("Interceptor should be able to log malformed json body.",
        testLogger.formattedOutput()
            .contains("{\"test1\": \"test1\"},"));
    assertTrue("Interceptor should be able to log malformed json body.",
        testLogger.formattedOutput()
            .contains("{\"test2\": \"test2\"}"));
  }

  @Test
  public void interceptorAbleToHandleBody_jsonArrayRequest() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);

    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(testLogger)
        .build();

    Request request = new Request.Builder()
        .url(String.valueOf(server.url("/")))
        .put(RequestBody.create(MediaType.parse("application/json"),
            "[{\"test1\": \"test1\"}, {\"test2\": \"test2\"}]"))
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(request)
        .execute();

    assertTrue("Interceptor should be able to log malformed json body.",
        testLogger.formattedOutput()
            .contains("{\"test1\": \"test1\"},"));
    assertTrue("Interceptor should be able to log malformed json body.",
        testLogger.formattedOutput()
            .contains("{\"test2\": \"test2\"}"));
  }

}
