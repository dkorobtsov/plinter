package io.github.dkorobtsov.plinter.apache;

import static io.github.dkorobtsov.plinter.internal.Util.APPLICATION_JSON;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.entity.ContentType;

/**
 * Helper class for Apache request/response converters intended for converting Apache HttpEntity to
 * String and back.
 */
final class ApacheEntityUtil {

  private ApacheEntityUtil() {
  }

  @SuppressWarnings("PMD.AssignmentInOperand")
  static String readApacheHttpEntity(final HttpEntity entity) throws IOException {
    if (nonNull(entity)) {
      final StringBuilder textBuilder = new StringBuilder();
      try (Reader reader = new BufferedReader(
          new InputStreamReader(
              entity.getContent(),
              Charset.forName(UTF_8.name())))) {
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

  static HttpEntity recreateHttpEntityFromString(final String httpEntityContent,
      final HttpEntity entity) {
    final Header contentType = entity.getContentType();
    final String contentTypeValue
        = isNull(contentType)
        ? APPLICATION_JSON
        : contentType.getValue();

    final Header contentEncodingHeader = entity.getContentEncoding();
    final EntityBuilder entityBuilder = EntityBuilder
        .create()
        .setContentType(ContentType.parse(contentTypeValue))
        .setStream(new ByteArrayInputStream(httpEntityContent.getBytes()));

    if (nonNull(contentEncodingHeader)) {
      return entityBuilder
          .setContentEncoding(String
              .format("%s/%s", contentEncodingHeader.getName(),
                  contentEncodingHeader.getValue()))
          .build();
    }
    return entityBuilder.build();
  }

}
