package io.github.dkorobtsov.plinter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import io.github.dkorobtsov.plinter.LoggerConfig.LoggerConfigBuilder;
import org.junit.Test;

/**
 * Tests validation default configuration values.
 */
public class LoggerConfigDefaultsTest {

  @Test
  public void loggerConfigDefaultConfiguration_shouldPrintMessageOnly() {
    final io.github.dkorobtsov.plinter.LoggerConfig loggerConfig = io.github.dkorobtsov.plinter.LoggerConfig
        .builder().build();

    assertEquals("Default format", io.github.dkorobtsov.plinter.LoggingFormat.JUL_MESSAGE_ONLY, loggerConfig.format);
  }

  @Test
  public void loggerConfigDefaultConfiguration_maxLineLengthIs110() {
    final LoggerConfig loggerConfig = io.github.dkorobtsov.plinter.LoggerConfig.builder().build();

    assertEquals("Max line length", 110, loggerConfig.maxLineLength);
  }

  @Test
  public void loggerConfigDefaultConfiguration_isLoggable() {
    final io.github.dkorobtsov.plinter.LoggerConfig loggerConfig = io.github.dkorobtsov.plinter.LoggerConfig
        .builder().build();

    assertTrue("Is loggable", loggerConfig.isLoggable);
  }

  @Test
  public void loggerConfigDefaultConfiguration_levelSetToBasic() {
    final io.github.dkorobtsov.plinter.LoggerConfig loggerConfig = io.github.dkorobtsov.plinter.LoggerConfig
        .builder().build();

    assertEquals("Logging level", Level.BASIC, loggerConfig.level);
  }

  @Test
  public void loggerConfigDefaultConfiguration_executorIsNotDefined() {
    final io.github.dkorobtsov.plinter.LoggerConfig loggerConfig = io.github.dkorobtsov.plinter.LoggerConfig
        .builder().build();

    assertNull("Executor", loggerConfig.executor);
  }

  @Test
  public void loggerConfigDefaultConfiguration_threadInfoIsDisabled() {
    final io.github.dkorobtsov.plinter.LoggerConfig loggerConfig = io.github.dkorobtsov.plinter.LoggerConfig
        .builder().build();

    assertFalse("Thread info", loggerConfig.withThreadInfo);
  }

  @Test
  public void loggerConfigDefaultConfiguration_loggerIsSet() {
    final io.github.dkorobtsov.plinter.LoggerConfig loggerConfig = io.github.dkorobtsov.plinter.LoggerConfig
        .builder().build();

    assertNotNull("Default Logger", loggerConfig.logger);
  }

  @Test
  public void loggerConfigDefaultConfiguration_formatCanBeChanged() {
    final LoggerConfigBuilder builder = io.github.dkorobtsov.plinter.LoggerConfig.builder();
    final io.github.dkorobtsov.plinter.LoggingFormat format = LoggingFormat.JUL_DATE_LEVEL_MESSAGE;
    final io.github.dkorobtsov.plinter.LoggerConfig loggerConfig = builder.format(format).build();

    assertEquals("Log Formatter", format, loggerConfig.format);
  }

}
