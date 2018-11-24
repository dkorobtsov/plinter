package com.dkorobtsov.logging;

import com.dkorobtsov.logging.converters.ToApacheHttpClientConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Assert;
import org.junit.Test;

public class ToApacheHttpClientConverterUnitTests {

    @Test
    public void testOkHttp3WithEmptyBodyConversion() throws IOException {
        final StringEntity httpEntity = (StringEntity) ToApacheHttpClientConverter
            .okHttp3RequestBodyToStringEntity(null, ContentType.APPLICATION_JSON);
        final InputStream content = httpEntity.getContent();
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        try (BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(content, Charset.defaultCharset()))) {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        Assert.assertEquals("body", "", stringBuilder.toString());
    }
}
