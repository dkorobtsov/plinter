package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.internal.Util.APPLICATION_JSON;
import static com.dkorobtsov.logging.internal.Util.APPLICATION_XML;
import static com.dkorobtsov.logging.internal.Util.APPLICATION_ZIP;
import static com.dkorobtsov.logging.internal.Util.TEXT_HTML;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
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

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_simpleJsonRequest(String loggerVersion,
        boolean provideExecutor) throws IOException {

        final List<String> loggerOutput = interceptedRequest(loggerVersion, provideExecutor,
            SIMPLE_JSON, APPLICATION_JSON, true);

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

        final List<String> loggerOutput = interceptedResponse(loggerVersion, provideExecutor,
            SIMPLE_JSON, APPLICATION_JSON, true);

        assertThat(loggerOutput).contains("     \"city\": \"New York\", ");
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_preformattedJsonRequest(String loggerVersion,
        boolean provideExecutor) throws IOException {

        final List<String> loggerOutput = interceptedRequest(loggerVersion, provideExecutor,
            PREFORMATTED_JSON_BODY, APPLICATION_JSON, true);

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

        final List<String> loggerOutput = interceptedResponse(loggerVersion, provideExecutor,
            PREFORMATTED_JSON_BODY, APPLICATION_JSON, true);

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

        final List<String> loggerOutput = interceptedRequest(loggerVersion, provideExecutor,
            MALFORMED_JSON_BODY, APPLICATION_JSON, false);

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

        final List<String> loggerOutput = interceptedResponse(loggerVersion, provideExecutor,
            MALFORMED_JSON_BODY, APPLICATION_JSON, false);

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
        boolean provideExecutor) throws IOException {

        final List<String> loggerOutput = interceptedRequest(loggerVersion, provideExecutor,
            HTML_BODY, TEXT_HTML, false);

        assertThat(loggerOutput).contains("<title>Error 404 Not Found</title>");
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_htmlResponse(String loggerVersion,
        boolean provideExecutor) throws IOException {

        final List<String> loggerOutput = interceptedResponse(loggerVersion, provideExecutor,
            HTML_BODY, "text/html", false);

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

        final List<String> loggerOutput = interceptedRequest(loggerVersion, provideExecutor,
            MALFORMED_HTML_BODY, TEXT_HTML, false);

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

        final List<String> loggerOutput = interceptedResponse(loggerVersion, provideExecutor,
            MALFORMED_HTML_BODY, "text/html", false);

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
        boolean provideExecutor) throws IOException {

        final List<String> loggerOutput = interceptedRequest(loggerVersion, provideExecutor,
            XML_BODY, APPLICATION_XML, false);

        assertThat(loggerOutput).contains("<?xml version=\"1.0\" encoding=\"UTF-16\"?>");
        assertThat(loggerOutput).contains("</mammals>");
    }

    @Test
    @Parameters({
        "okhttp, true", "okhttp, false",
        "okhttp3, true", "okhttp3, false",
        "apacheHttpclientRequest, true", "apacheHttpclientRequest, false"
    })
    public void interceptorAbleToHandleBody_xmlResponse(String loggerVersion,
        boolean provideExecutor) throws IOException {

        final List<String> loggerOutput = interceptedResponse(loggerVersion, provideExecutor,
            XML_BODY, APPLICATION_XML, false);

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

        final List<String> loggerOutput = interceptedRequest(loggerVersion, provideExecutor,
            MALFORMED_XML_BODY, APPLICATION_XML, false);

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

        final List<String> loggerOutput = interceptedResponse(loggerVersion, provideExecutor,
            MALFORMED_XML_BODY, APPLICATION_XML, false);

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
        boolean provideExecutor) throws IOException {

        final List<String> loggerOutput = interceptedRequest(loggerVersion, provideExecutor,
            PREFORMATTED_JSON_BODY, APPLICATION_ZIP, true);

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

        final List<String> loggerOutput = interceptedResponse(loggerVersion, provideExecutor,
            PREFORMATTED_JSON_BODY, APPLICATION_ZIP,
            true);

        assertThat(loggerOutput).contains("  Omitted response body ");
    }


}
