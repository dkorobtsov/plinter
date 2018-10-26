package com.dkorobtsov.logging;

import com.squareup.okhttp.mockwebserver.MockResponse;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.dkorobtsov.logging.OkhttpTypesConverter.convertOkhtt3pRequestBody;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(JUnitParamsRunner.class)
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
  @Parameters({
      "okhttp", "okhttp3"
  })
  public void interceptorAbleToHandleBody_JsonRequest(String loggerVersion) throws IOException {
    final String[] loggerOutput = interceptedRequest(RequestBody
        .create(MediaType.parse("application/json"), JSON_BODY), loggerVersion);

    assertTrue("Interceptor should be able to log json body.",
        Arrays.asList(loggerOutput).contains("      \"name\": \"doggie\", "));
  }

  @Test
  @Parameters({
      "okhttp", "okhttp3"
  })
  public void interceptorAbleToHandleBody_JsonResponse(String loggerVersion) throws IOException {
    final String[] loggerOutput = interceptedResponse("application/json", JSON_BODY, loggerVersion);

    assertTrue("Interceptor should be able to log json body.",
        Arrays.asList(loggerOutput).contains("      \"name\": \"doggie\", "));
  }

  @Test
  @Parameters({
      "okhttp", "okhttp3"
  })
  public void interceptorAbleToHandleBody_XmlRequest(String loggerVersion) throws IOException {
    final String[] loggerOutput = interceptedRequest(RequestBody
        .create(MediaType.parse("application/xml"), XML_BODY), loggerVersion);

    assertTrue("Interceptor should be able to handle xml request body.",
        Arrays.asList(loggerOutput)
            .contains("  <?xml version=\"1.0\" encoding=\"UTF-16\"?> "));
    assertTrue("Interceptor should be able to handle xml request body.",
        Arrays.asList(loggerOutput).contains("  </mammals> "));
  }

  @Test
  @Parameters({
      "okhttp", "okhttp3"
  })
  public void interceptorAbleToHandleBody_XmlResponse(String loggerVersion) throws IOException {
    final String[] loggerOutput = interceptedResponse("application/xml", XML_BODY, loggerVersion);

    assertTrue("Interceptor should be able to handle xml response body.",
        Arrays.asList(loggerOutput)
            .contains("  <?xml version=\"1.0\" encoding=\"UTF-16\"?> "));
    assertTrue("Interceptor should be able to handle xml response body.",
        Arrays.asList(loggerOutput).contains("  </mammals> "));
  }

  @Test
  @Parameters({
      "okhttp", "okhttp3"
  })
  public void interceptorAbleToHandleBody_MalformedXmlRequest(String loggerVersion) throws IOException {
    final String[] loggerOutput = interceptedRequest(RequestBody
        .create(MediaType.parse("application/xml"), MALFORMED_XML_BODY), loggerVersion);

    assertTrue("Interceptor should be able to handle malformed xml body.",
        Arrays.asList(loggerOutput)
            .contains("  <?xml version=\"1.0\"?><mammals><animal id=\"0\" "
                + "species=\"Capra hircus\">Goat</animal>animal id=\"1\" species=\"Panthe "));
  }

  @Test
  @Parameters({
      "okhttp", "okhttp3"
  })
  public void interceptorAbleToHandleBody_MalformedXmlResponse(String loggerVersion) throws IOException {
    final String[] loggerOutput = interceptedResponse("application/xml", MALFORMED_XML_BODY, loggerVersion);

    assertTrue("Interceptor should be able to handle xml response body.",
        Arrays.asList(loggerOutput)
            .contains("  <?xml version=\"1.0\"?><mammals><animal id=\"0\" "
                + "species=\"Capra hircus\">Goat</animal>animal id=\"1\" species=\"Panthe "));
  }

  @Test
  @Parameters({
      "okhttp", "okhttp3"
  })
  public void interceptorAbleToHandleBody_FileRequest(String loggerVersion) throws IOException {
    RequestBody body = RequestBody.create(MediaType.parse("application/zip"),
        createFileFromString(JSON_BODY));

    final String[] loggerOutput = interceptedRequest(body, loggerVersion);

    assertTrue("Interceptor should not log file request body.",
        Arrays.asList(loggerOutput).contains("  Omitted response body "));
  }

  @Test
  @Parameters({
      "okhttp", "okhttp3"
  })
  public void interceptorAbleToHandleBody_FileResponse(String loggerVersion) throws IOException {
    final String[] loggerOutput = interceptedResponse("application/zip",
        String.valueOf(createFileFromString(JSON_BODY)), loggerVersion);

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

  private String[] interceptedRequest(RequestBody body, String loggerVersion) throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);

    Request okhttp3Request = new Request.Builder()
        .url(String.valueOf(server.url("/")))
        .put(body)
        .build();

    if (loggerVersion.equals(InterceptorVersion.OKHTTP3.getName())) {
      Okhttp3LoggingInterceptor interceptor = new Okhttp3LoggingInterceptor.Builder()
          .logger(testLogger)
          .build();

      defaultClientWithInterceptor(interceptor)
          .newCall(okhttp3Request)
          .execute();
      return testLogger.outputAsArray();
    } else if (loggerVersion.equals(InterceptorVersion.OKHTTP.getName())){
      OkhttpLoggingInterceptor interceptor = new OkhttpLoggingInterceptor.Builder()
          .logger(testLogger)
          .build();

      final com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
          .url(String.valueOf(server.url("/")))
          .put(convertOkhtt3pRequestBody(okhttp3Request))
          .build();

      defaultClientWithInterceptor(interceptor)
          .newCall(request)
          .execute();
      return testLogger.outputAsArray();
    } else {
      fail(String.format("I didn't recognize %s version. I support 'okhttp' and 'okhttp3' versions", loggerVersion));
      return new String[1];
    }
  }

  private String[] interceptedResponse(String contentType, String body, String interceptorVersion) throws IOException {
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", contentType)
        .setBody(body));

    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
    if (interceptorVersion.equals(InterceptorVersion.OKHTTP.getName())) {
      OkhttpLoggingInterceptor interceptor = new OkhttpLoggingInterceptor.Builder()
          .logger(testLogger)
          .build();
      defaultClientWithInterceptor(interceptor)
          .newCall(defaultOkhttRequest())
          .execute();
    } else if (interceptorVersion.equals(InterceptorVersion.OKHTTP3.getName())) {
      Okhttp3LoggingInterceptor interceptor = new Okhttp3LoggingInterceptor.Builder()
          .logger(testLogger)
          .build();

      defaultClientWithInterceptor(interceptor)
          .newCall(defaultOkhtt3Request())
          .execute();
    } else {
      fail(String.format("I couldn't recognize %s interceptor version. I only support okhttp and okhttp3 versions at the moment", interceptorVersion));
    }
    return testLogger.outputAsArray();
  }

}
