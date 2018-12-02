package com.dkorobtsov.logging;

import static org.junit.Assert.assertEquals;

import com.dkorobtsov.logging.LoggerConfig.LoggerConfigBuilder;
import com.dkorobtsov.logging.enums.LoggingFormat;
import org.junit.Test;

public class LoggingInterceptorUnitTests {

    @Test
    public void testDefaultLogFormatterIsMessageOnly() {
        final LoggerConfigBuilder builder = LoggerConfig.builder();

        assertEquals("Default logger", LoggingFormat.JUL_MESSAGE_ONLY, builder.getFormat());
    }

    @Test
    public void testSettingFormatForLoggingInterceptor() {
        final LoggerConfigBuilder builder = LoggerConfig.builder();
        final LoggingFormat format = LoggingFormat.JUL_DATE_LEVEL_MESSAGE;
        LoggerConfig loggerConfig = builder.format(format).build();

        assertEquals("Log Formatter", format, loggerConfig.format);
    }

}
