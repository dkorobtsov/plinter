package com.dkorobtsov.logging.interceptors.apache;

import static com.dkorobtsov.logging.internal.Util.APPLICATION_JSON;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.entity.ContentType;

class ApacheEntityUtil {

    private ApacheEntityUtil(){

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
