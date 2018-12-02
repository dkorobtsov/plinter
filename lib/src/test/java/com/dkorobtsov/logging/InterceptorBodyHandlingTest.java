package com.dkorobtsov.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
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

        assertThat(loggerOutput).contains("     \"city\": \"New York\", ");
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

        assertThat(loggerOutput).contains("     \"city\": \"New York\", ");
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

        assertThat(loggerOutput).containsSequence("     \"name\": \"doggie\", ");
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

        assertThat(loggerOutput).containsSequence("     \"name\": \"doggie\", ");
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
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_htmlRequest(String loggerVersion,
        boolean provideExecutor)
        throws IOException {
        final List<String> loggerOutput = interceptedRequest(RequestBody
                .create(MediaType.parse("text/html"), HTML_BODY),
            loggerVersion, provideExecutor, false);

        assertThat(loggerOutput).contains("<title>Error 404 Not Found</title>");
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

        assertThat(loggerOutput).contains("<title>Error 404 Not Found</title>");
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

        assertThat(loggerOutput).contains("<?xml version=\"1.0\" encoding=\"UTF-16\"?>");
        assertThat(loggerOutput).contains("</mammals>");
    }

    @Test
    @Parameters({
/*        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",*/
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_xmlResponse(String loggerVersion,
        boolean provideExecutor)
        throws IOException {
        final List<String> loggerOutput = interceptedResponse("application/xml", XML_BODY,
            loggerVersion,
            provideExecutor, false);

        assertThat(loggerOutput).contains("<?xml version=\"1.0\" encoding=\"UTF-16\"?>");
        assertThat(loggerOutput).contains("</mammals>");
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
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

        assertThat(loggerOutput).contains("  Omitted response body ");
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

        assertThat(loggerOutput).contains("  Omitted response body ");
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


}
