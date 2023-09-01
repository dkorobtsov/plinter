package io.github.dkorobtsov.plinter.integration;

import io.github.dkorobtsov.plinter.BaseTest;
import io.github.dkorobtsov.plinter.core.LogWriter;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import okhttp3.mockwebserver.MockResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Objects;

/**
 * Test added as a proof that interceptors can be easily integrated with external logging
 * libraries.
 */
@SuppressWarnings("PMD")
@RunWith(JUnitParamsRunner.class)
public class Log4j2LoggerTest extends BaseTest {

  private static final Logger log = LogManager.getLogger(Log4j2LoggerTest.class);
  private static final StringWriter LOG_WRITER = new StringWriter();
  private static final String ROOT_LOG_PATTERN = "%d{HH:mm:ss.SSS} [%t] %-5level %c{0}:%L - %msg%n";
  private static final String OK_HTTP_LOG_PATTERN = "[OkHTTP] %msg%n";
  private static final String HTTP_LOGGER = "OkHttpLogger";

  private final LogWriter log4j2Writer = new LogWriter() {

    final Logger log = LogManager.getLogger(HTTP_LOGGER);

    {
      final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
      final Configuration config = ctx.getConfiguration();

      final LoggerConfig loggerConfig = new LoggerConfig(HTTP_LOGGER, Level.TRACE, false);
      final PatternLayout layout = PatternLayout
          .newBuilder()
          .withPattern(OK_HTTP_LOG_PATTERN)
          .build();

      final Appender appender = ConsoleAppender
          .newBuilder()
          .withName("OkHttpConsoleAppender")
          .withLayout(layout)
          .build();

      appender.start();

      loggerConfig.addAppender(appender, Level.TRACE, null);
      config.addLogger(HTTP_LOGGER, loggerConfig);
      ctx.updateLoggers();
    }

    @Override
    public void log(String msg) {
      log.debug(msg);
    }
  };

  @BeforeClass
  public static void configureLogger() throws IOException {
    initializeBaseLog4j2Configuration();
  }

  @Test
  @Parameters(method = "interceptors")
  public void interceptorCanBeConfiguredToPrintLogWithLog4j2(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));

    log.debug("Adding test double appender for output validation.");
    addAppender(LOG_WRITER, "TestWriter", OK_HTTP_LOG_PATTERN);

    interceptWithConfig(interceptor,
        io.github.dkorobtsov.plinter.core.LoggerConfig.builder()
            .logger(log4j2Writer)
            .build());

    log.debug("Retrieving logger output for validation.");
    final String logOutput = LOG_WRITER.toString();

    Assertions
        .assertThat(logOutput)
        .doesNotContain("DEBUG")
        .contains("OkHTTP")
        .contains("Request")
        .contains("Response");
  }

  private static void initializeBaseLog4j2Configuration() throws IOException {
    final ConfigurationBuilder<BuiltConfiguration> builder
        = ConfigurationBuilderFactory.newConfigurationBuilder();

    final AppenderComponentBuilder console = builder.newAppender("stdout", "Console");
    final LayoutComponentBuilder layout = builder.newLayout("PatternLayout");
    layout.addAttribute("pattern", ROOT_LOG_PATTERN);
    console.add(layout);
    builder.add(console);

    final RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.DEBUG);
    rootLogger.add(builder.newAppenderRef("stdout"));
    builder.add(rootLogger);

    builder.writeXmlConfiguration(System.out);
    Configurator.initialize(builder.build());
  }

  @SuppressWarnings("SameParameterValue")
  private static void addAppender(final Writer writer, final String writerName, String pattern) {
    final LoggerContext context = LoggerContext.getContext(false);
    final Configuration config = context.getConfiguration();

    removeAppenderIfExists(writerName, config);

    final PatternLayout layout = PatternLayout.newBuilder().withPattern(pattern).build();
    final Appender appender = WriterAppender
        .createAppender(layout, null, writer, writerName, false, true);

    appender.start();
    config.addAppender(appender);
    updateLoggers(appender, config);
  }

  private static void removeAppenderIfExists(String writerName, Configuration config) {
    final Appender appender = config.getAppender(writerName);
    if (Objects.nonNull(appender)) {
      config.getRootLogger().removeAppender(writerName);
    }
  }

  private static void updateLoggers(final Appender appender, final Configuration config) {
    final Level level = null;
    final Filter filter = null;
    for (final LoggerConfig loggerConfig : config.getLoggers().values()) {
      loggerConfig.addAppender(appender, level, filter);
    }
    config.getRootLogger().addAppender(appender, level, filter);
  }

}
