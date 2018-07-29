package com.dkorobtsov.logging;

import static org.junit.Assert.assertTrue;

import com.squareup.okhttp.mockwebserver.MockResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class InterceptorBodyHandlingTest extends BaseTest {

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

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void interceptorAbleToHandleBody_JsonResponse() throws IOException {
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(JSON_BODY));

    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

    assertTrue("Interceptor should be able to log json body.",
        testLogger.formattedOutput().contains("\"name\": \"doggie\","));
  }

  @Test
  public void interceptorAbleToHandleBody_JsonRequest() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);

    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(testLogger)
        .build();

    Request request = new Request.Builder()
        .url(String.valueOf(server.url("/")))
        .put(RequestBody.create(MediaType.parse("application/json"), JSON_BODY))
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(request)
        .execute();

    assertTrue("Interceptor should be able to log json body.",
        testLogger.formattedOutput().contains("\"name\": \"doggie\","));
  }

  @Test
  public void interceptorAbleToHandleBody_FileRequest() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);

    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(testLogger)
        .build();

    Request request = new Request.Builder()
        .url(String.valueOf(server.url("/")))
        .put(RequestBody.create(MediaType.parse("application/zip"),
                createFileFromString(JSON_BODY)))
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(request)
        .execute();

    assertTrue("Interceptor should not log file request body.",
        testLogger.formattedOutput().contains("Omitted request body "));
  }

  @Test
  public void interceptorAbleToHandleBody_FileResponse() throws IOException {
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/zip")
        .setBody(String.valueOf(createFileFromString(JSON_BODY))));

    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

    assertTrue("Interceptor should not log file response body.",
        testLogger.formattedOutput().contains("Omitted response body"));
  }

  private File createFileFromString(String val) throws IOException {
    File file = temporaryFolder.newFile();
    byte[] bytes = val.getBytes(StandardCharsets.UTF_8);
    Source source = Okio.source(new ByteArrayInputStream(bytes));
    try (BufferedSink b = Okio.buffer(Okio.sink(file))) {
      b.writeAll(source);
    }
    return file;
  }

}
