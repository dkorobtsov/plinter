package io.github.dkorobtsov.plinter.apache;

import static io.github.dkorobtsov.plinter.core.internal.Util.APPLICATION_JSON;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import io.github.dkorobtsov.plinter.core.internal.SuppressFBWarnings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

  @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING", justification = "False positive.")
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

  @SuppressWarnings("PMD.AssignmentInOperand")
  static byte[] getEntityBytes(HttpEntity entity) throws IOException {
    final byte[] byteArray;
    final InputStream inputStream = entity.getContent();
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int nRead;
    final byte[] data = new byte[1024];
    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();
    byteArray = buffer.toByteArray();
    return byteArray;
  }

}
