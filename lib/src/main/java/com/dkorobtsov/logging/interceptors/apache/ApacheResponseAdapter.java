package com.dkorobtsov.logging.interceptors.apache;

import com.dkorobtsov.logging.ResponseDetails;
import com.dkorobtsov.logging.enums.HttpStatusCode;
import com.dkorobtsov.logging.internal.InterceptedHeaders;
import com.dkorobtsov.logging.internal.InterceptedMediaType;
import com.dkorobtsov.logging.internal.InterceptedResponseBody;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.entity.ContentType;

public class ApacheResponseAdapter {

    private static final String APPLICATION_JSON = "application/json";
    private static final Logger logger = Logger.getLogger(ApacheResponseAdapter.class.getName());

    ApacheResponseAdapter() {

    }

    public static ResponseDetails responseDetails(HttpResponse httpResponse) {
        if (Objects.isNull(httpResponse)) {
            return null;
        } else {
            final int code = httpResponse.getStatusLine().getStatusCode();
            final InterceptedResponseBody responseBody = interceptedResponseBody(httpResponse);

            return ResponseDetails.builder()
                .code(httpResponse.getStatusLine().getStatusCode())
                .headers(interceptedHeaders(httpResponse.getAllHeaders()))
                .isSuccessful(code >= 200 && code <= 300)
                .mediaType(responseBody.contentType())
                .message(HttpStatusCode.findMessage(code))
                .responseBody(responseBody)
                .build();
        }
    }

    private static InterceptedHeaders interceptedHeaders(Header[] headers) {
        final InterceptedHeaders.Builder headersBuilder = new InterceptedHeaders.Builder();
        Arrays.stream(headers).forEach(it -> headersBuilder.add(it.getName(), it.getValue()));
        return headersBuilder.build();
    }

    public static InterceptedResponseBody interceptedResponseBody(HttpResponse response) {
        final HttpEntity entity = response.getEntity();
        if (entity != null) {
            String requestBodyString;
            try {
                requestBodyString = readApacheHttpEntity(entity);
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                return InterceptedResponseBody.create(InterceptedMediaType.parse(APPLICATION_JSON),
                    "[LoggingInterceptorError] : could not parse response body");
            }
            final Header contentType = response.getEntity().getContentType();
            final String contentTypeValue
                = contentType == null ? ""
                : contentType.getValue();

            final HttpEntity newEntity = recreateHttpEntityFromString(requestBodyString, entity);
            response.setEntity(newEntity);

            return InterceptedResponseBody
                .create(InterceptedMediaType.parse(contentTypeValue), requestBodyString);
        }
        return InterceptedResponseBody
            .create(InterceptedMediaType.parse(APPLICATION_JSON), "");
    }

    static String readApacheHttpEntity(HttpEntity entity) throws IOException {
        if (entity != null) {
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader
                (entity.getContent(), Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
                return textBuilder.toString();
            }
        } else {
            return "";
        }
    }

    static HttpEntity recreateHttpEntityFromString(String httpEntityContent,
        HttpEntity entity) {
        final Header contentType = entity.getContentType();
        final String contentTypeValue
            = contentType == null ? APPLICATION_JSON
            : contentType.getValue();

        final Header contentEncodingHeader = entity.getContentEncoding();
        final EntityBuilder entityBuilder = EntityBuilder
            .create()
            .setContentType(ContentType.parse(contentTypeValue))
            .setStream(new ByteArrayInputStream(httpEntityContent.getBytes()));
        if (contentEncodingHeader != null) {
            return entityBuilder
                .setContentEncoding(String
                    .format("%s/%s", contentEncodingHeader.getName(),
                        contentEncodingHeader.getValue()))
                .build();
        }
        return entityBuilder.build();
    }

}
