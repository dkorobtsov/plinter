package com.dkorobtsov.logging;

import static org.junit.Assert.assertTrue;

import com.squareup.okhttp.mockwebserver.MockResponse;
import java.io.IOException;
import java.util.Arrays;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.Test;

public class InterceptorXMLBodyHandlingTest extends BaseTest {

  private static final String XML_BODY = "<?xml version=\"1.0\"?><mammals>" +
      "<animal id=\"0\" species=\"Capra hircus\">Goat</animal>" +
      "<animal id=\"1\" species=\"Panthera pardus\">Leopard</animal>" +
      "<animal id=\"2\" species=\"Equus zebra\">Zebra</animal> </mammals>";

  @Test
  public void interceptorAbleToHandleBody_XmlResponse() throws IOException {
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/xml")
        .setBody(XML_BODY));

    TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(testLogger)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

    assertTrue("Interceptor should be able to handle xml response body.",
        Arrays.asList(testLogger.outputAsArray())
            .contains("  <?xml version=\"1.0\" encoding=\"UTF-16\"?> "));
    assertTrue("Interceptor should be able to handle xml response body.",
        Arrays.asList(testLogger.outputAsArray()).contains("  </mammals> "));
  }

  @Test
  public void interceptorAbleToHandleBody_XmlRequest() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LogFormatter.JUL_MESSAGE_ONLY);

    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(testLogger)
        .build();

    Request request = new Request.Builder()
        .url(String.valueOf(server.url("/")))
        .put(RequestBody.create(MediaType.parse("application/xml"), XML_BODY))
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(request)
        .execute();

    assertTrue("Interceptor should be able to handle xml request body.",
        Arrays.asList(testLogger.outputAsArray())
            .contains("  <?xml version=\"1.0\" encoding=\"UTF-16\"?> "));
    assertTrue("Interceptor should be able to handle xml request body.",
        Arrays.asList(testLogger.outputAsArray()).contains("  </mammals> "));
  }

}
