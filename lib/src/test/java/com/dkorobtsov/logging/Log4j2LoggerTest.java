package com.dkorobtsov.logging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.squareup.okhttp.mockwebserver.MockResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class Log4j2LoggerTest extends BaseTest {

  private static final String ROOT_LOG_PATTERN = "%d{HH:mm:ss.SSS} [%t] %-5level %c{0}:%L - %msg%n";
  private static final StringWriter logWriter = new StringWriter();
  private final Logger log = LogManager.getLogger(Log4j2LoggerTest.class);

  @BeforeClass
  public static void configureLogger() throws IOException {
    initializeBaseLog4j2Configuration();
  }

  private static void initializeBaseLog4j2Configuration() throws IOException {
    ConfigurationBuilder<BuiltConfiguration> builder
        = ConfigurationBuilderFactory.newConfigurationBuilder();

    AppenderComponentBuilder console = builder.newAppender("stdout", "Console");
    LayoutComponentBuilder layout = builder.newLayout("PatternLayout");
    layout.addAttribute("pattern", ROOT_LOG_PATTERN);
    console.add(layout);
    builder.add(console);

    RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.DEBUG);
    rootLogger.add(builder.newAppenderRef("stdout"));
    builder.add(rootLogger);

    builder.writeXmlConfiguration(System.out);
    Configurator.initialize(builder.build());
  }

  private static void addAppender(final Writer writer, final String writerName, String pattern) {
    final LoggerContext context = LoggerContext.getContext(false);
    final Configuration config = context.getConfiguration();

    PatternLayout layout = PatternLayout.newBuilder().withPattern(pattern).build();

    final Appender appender = WriterAppender
        .createAppender(layout, null, writer, writerName, false, true);
    appender.start();
    config.addAppender(appender);
    updateLoggers(appender, config);
  }

  private static void updateLoggers(final Appender appender, final Configuration config) {
    final Level level = null;
    final Filter filter = null;
    for (final LoggerConfig loggerConfig : config.getLoggers().values()) {
      loggerConfig.addAppender(appender, level, filter);
    }
    config.getRootLogger().addAppender(appender, level, filter);
  }

  @Test
  @Parameters(method = "interceptors")
  public void interceptorCanBeConfiguredToPrintLogWithLog4j2(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final String OK_HTTP_LOG_PATTERN = "[OkHTTP] %msg%n";

    log.debug("Configuring custom Log4j2 logger for intercepted OkHttp traffic.");
    LogWriter log4j2Writer = new LogWriter() {

      final Logger log = LogManager.getLogger("OkHttpLogger");

      {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();

        LoggerConfig loggerConfig = new LoggerConfig("OkHttpLogger", Level.TRACE, false);
        PatternLayout layout = PatternLayout
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
        config.addLogger("OkHttpLogger", loggerConfig);
        ctx.updateLoggers();
      }

      @Override
      public void log(String msg) {
        log.debug(msg);
      }
    };

    log.debug("Adding test double appender for output validation.");
    addAppender(logWriter, "TestWriter", OK_HTTP_LOG_PATTERN);

    interceptWithConfig(interceptor,
        com.dkorobtsov.logging.LoggerConfig.builder()
            .logger(log4j2Writer)
            .build());

    log.debug("Retrieving logger output for validation.");
    final String logOutput = logWriter.toString();

    assertFalse("Severity tag should not be present in custom OkHTTP logger output.",
        logOutput.contains("DEBUG"));

    assertTrue("Logger name should be present as defined by logging pattern.",
        logOutput.contains("OkHTTP"));

    assertTrue("Request section should be present in logger output.",
        logOutput.contains("Request"));

    assertTrue("Response section should be present in logger output.",
        logOutput.contains("Response"));

  }

}
