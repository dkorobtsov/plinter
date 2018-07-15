package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.TestUtil.defaultClientWithInterceptor;
import static org.junit.Assert.assertTrue;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import okhttp3.Request;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
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
import org.junit.Rule;
import org.junit.Test;

public class Log4j2LoggerTest {

  @Rule public MockWebServer server = new MockWebServer();
  private static final String LOG_PATTERN = "[%d{HH:mm:ss.SSS}][%t][%-5level][%c{0}] %msg%n";
  private static StringWriter logWriter = new StringWriter();

  @BeforeClass
  public static void configureLogger() throws IOException {
    initializeBaseLog4j2Configuration();
    addAppender(logWriter, "TestWriter");
  }

  @Test
  public void interceptorCanBeConfiguredToPrintLogWithLog4j2() throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200));

    //Creating new custom Log4j2 logger for intercepted OkHttp traffic
    LogWriter log4j2Writer = new LogWriter() {
      final Logger log = LogManager.getLogger("OkHttpLogger");

      @Override
      public void log(String msg) {
        log.debug(msg);
      }
    };

    //Assigning logger to interceptor
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(log4j2Writer)
        .build();

    defaultClientWithInterceptor(interceptor)
        .newCall(defaultRequest())
        .execute();

    final String logOutput = logWriter.toString();

    assertTrue("Severity tag should be present as defined by logging pattern.",
        logOutput.contains("[DEBUG]"));

    assertTrue("Logger name should be present as defined by logging pattern.",
        logOutput.contains("[OkHttpLogger]"));

    assertTrue("Request section should be present in logger output.",
        logOutput.contains("Request"));

    assertTrue("Response section should be present in logger output.",
        logOutput.contains("Response"));

  }

  private Request defaultRequest() {
    return new Request.Builder()
        .url(String.valueOf(server.url("/")))
        .build();
  }

  private static void initializeBaseLog4j2Configuration() throws IOException {
    ConfigurationBuilder<BuiltConfiguration> builder
        = ConfigurationBuilderFactory.newConfigurationBuilder();

    AppenderComponentBuilder console = builder.newAppender("stdout", "Console");
    LayoutComponentBuilder layout = builder.newLayout("PatternLayout");
    layout.addAttribute("pattern", LOG_PATTERN);
    console.add(layout);
    builder.add(console);

    RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.DEBUG);
    rootLogger.add(builder.newAppenderRef("stdout"));
    builder.add(rootLogger);

    //builder.writeXmlConfiguration(System.out);
    Configurator.initialize(builder.build());
  }

  private static void addAppender(final Writer writer, final String writerName) {
    final LoggerContext context = LoggerContext.getContext(false);
    final Configuration config = context.getConfiguration();

    PatternLayout layout = PatternLayout.newBuilder().withPattern(LOG_PATTERN).build();

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

}
