package com.dkorobtsov.logging;

import com.dkorobtsov.logging.enums.LoggingFormat;
import org.junit.Assert;
import org.junit.Test;

public class LoggingInterceptorUnitTests {

    @Test
    public void testDefaultLogFormatterIsMessageOnly() {
        final LoggingInterceptor.Builder builder = new LoggingInterceptor.Builder();
        Assert
            .assertEquals("Default logger", LoggingFormat.JUL_MESSAGE_ONLY, builder.getFormat());
    }

    @Test
    public void testSettingFormatForLoggingInterceptor() {
        final LoggingInterceptor.Builder builder = new LoggingInterceptor
            .Builder();
        final LoggingFormat format = LoggingFormat.JUL_DATE_LEVEL_MESSAGE;
        builder.format(format);
        Assert.assertEquals("Log Formatter", format, builder.getFormat());
    }

}
