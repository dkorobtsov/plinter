package io.github.dkorobtsov.plinter.apache;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.entity.ContentType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static io.github.dkorobtsov.plinter.core.internal.Util.APPLICATION_JSON;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Helper class for Apache request/response converters intended for converting
 * Apache HttpEntity to String and back.
 */
final class ApacheEntityUtil {

  private ApacheEntityUtil() {
  }

  static HttpEntity recreateHttpEntityFromByteArray(byte[] httpEntityContent,
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
      .setStream(new ByteArrayInputStream(httpEntityContent));

    if (nonNull(contentEncodingHeader)) {
      return entityBuilder
        .setContentEncoding(String
          .format("%s/%s", contentEncodingHeader.getName(),
            contentEncodingHeader.getValue()))
        .build();
    }
    return entityBuilder.build();
  }

  @SuppressWarnings("PMD")
  static byte[] getEntityBytes(HttpEntity entity) throws IOException {
    final byte[] byteArray;
    final InputStream inputStream = entity.getContent();
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int bytesRead;
    final byte[] data = new byte[1024];
    while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, bytesRead);
    }

    buffer.flush();
    byteArray = buffer.toByteArray();
    return byteArray;
  }

}
