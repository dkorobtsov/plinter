package com.dkorobtsov.logging;

import static org.junit.Assert.assertTrue;

import com.squareup.okhttp.mockwebserver.MockResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

  private static final String XML_BODY = "<?xml version=\"1.0\"?><mammals>" +
      "<animal id=\"0\" species=\"Capra hircus\">Goat</animal>" +
      "<animal id=\"1\" species=\"Panthera pardus\">Leopard</animal>" +
      "<animal id=\"2\" species=\"Equus zebra\">Zebra</animal> </mammals>";

  private static final String MALFORMED_XML_BODY = "<?xml version=\"1.0\"?><mammals>" +
      "<animal id=\"0\" species=\"Capra hircus\">Goat</animal>" +
      "animal id=\"1\" species=\"Panthera pardus\">Leopard</animal>" +
      "<animal id=\"2\" species=\"Equus zebra\">Zebra</animal> </mammals>";

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
  public void interceptorAbleToHandleBody_JsonRequest() throws IOException {
    final String[] loggerOutput = interceptedRequest(RequestBody
        .create(MediaType.parse("application/json"), JSON_BODY));

    assertTrue("Interceptor should be able to log json body.",
        Arrays.asList(loggerOutput).contains("      \"name\": \"doggie\", "));
  }

  @Test
  public void interceptorAbleToHandleBody_JsonResponse() throws IOException {
    final String[] loggerOutput = interceptedResponse("application/json", JSON_BODY);

    assertTrue("Interceptor should be able to log json body.",
        Arrays.asList(loggerOutput).contains("      \"name\": \"doggie\", "));
  }

  @Test
  public void interceptorAbleToHandleBody_XmlRequest() throws IOException {
    final String[] loggerOutput = interceptedRequest(RequestBody
        .create(MediaType.parse("application/xml"), XML_BODY));

    assertTrue("Interceptor should be able to handle xml request body.",
        Arrays.asList(loggerOutput)
            .contains("  <?xml version=\"1.0\" encoding=\"UTF-16\"?> "));
    assertTrue("Interceptor should be able to handle xml request body.",
        Arrays.asList(loggerOutput).contains("  </mammals> "));
  }

  @Test
  public void interceptorAbleToHandleBody_XmlResponse() throws IOException {
    final String[] loggerOutput = interceptedResponse("application/xml", XML_BODY);

    assertTrue("Interceptor should be able to handle xml response body.",
        Arrays.asList(loggerOutput)
            .contains("  <?xml version=\"1.0\" encoding=\"UTF-16\"?> "));
    assertTrue("Interceptor should be able to handle xml response body.",
        Arrays.asList(loggerOutput).contains("  </mammals> "));
  }

  @Test
  public void interceptorAbleToHandleBody_MalformedXmlRequest() throws IOException {
    final String[] loggerOutput = interceptedRequest(RequestBody
        .create(MediaType.parse("application/xml"), MALFORMED_XML_BODY));

    assertTrue("Interceptor should be able to handle malformed xml body.",
        Arrays.asList(loggerOutput)
            .contains("  <?xml version=\"1.0\"?><mammals><animal id=\"0\" "
                + "species=\"Capra hircus\">Goat</animal>animal id=\"1\" species=\"Panthe "));
  }

  @Test
  public void interceptorAbleToHandleBody_MalformedXmlResponse() throws IOException {
    final String[] loggerOutput = interceptedResponse("application/xml", MALFORMED_XML_BODY);

    assertTrue("Interceptor should be able to handle xml response body.",
        Arrays.asList(loggerOutput)
            .contains("  <?xml version=\"1.0\"?><mammals><animal id=\"0\" "
                + "species=\"Capra hircus\">Goat</animal>animal id=\"1\" species=\"Panthe "));
  }

  @Test
  public void interceptorAbleToHandleBody_FileRequest() throws IOException {
    RequestBody body = RequestBody.create(MediaType.parse("application/zip"),
        createFileFromString(JSON_BODY));

    final String[] loggerOutput = interceptedRequest(body);

    assertTrue("Interceptor should not log file request body.",
        Arrays.asList(loggerOutput).contains("  Omitted response body "));
  }

  @Test
  public void interceptorAbleToHandleBody_FileResponse() throws IOException {
    final String[] loggerOutput = interceptedResponse("application/zip",
        String.valueOf(createFileFromString(JSON_BODY)));

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

  private String[] interceptedRequest(RequestBody body) throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);

    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(testLogger)
        .build();

    Request request = new Request.Builder()
        .url(String.valueOf(server.url("/")))
        .put(body)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(request)
        .execute();
    return testLogger.outputAsArray();
  }

  private String[] interceptedResponse(String contentType, String body) throws IOException {
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", contentType)
        .setBody(body));

    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();
    return testLogger.outputAsArray();
  }

}
