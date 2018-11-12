package com.dkorobtsov.logging;

import org.junit.Assert;
import org.junit.Test;

public class LogginInterceptorUnitTests {

    @Test
    public void testDefaultLogFormatterIsMessageOnly() {
        final LoggingInterceptor.Builder builder = new LoggingInterceptor.Builder();
        Assert
            .assertEquals("Default logger", builder.getFormatter(), LogFormatter.JUL_MESSAGE_ONLY);
    }

    @Test
    public void testSettingFormatForLoggingInterceptor() {
        final LoggingInterceptor.Builder builder = new LoggingInterceptor
            .Builder();
        final LogFormatter format = LogFormatter.JUL_DATE_LEVEL_MESSAGE;
        builder.format(format);
        Assert.assertEquals("Log Formatter", format, builder.getFormatter());
    }
}
