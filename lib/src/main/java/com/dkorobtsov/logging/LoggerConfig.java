package com.dkorobtsov.logging;

import java.util.concurrent.Executor;

public class LoggerConfig {

  public final boolean isLoggable;
  public final Level level;
  public final LogWriter logger;
  public final LoggingFormat format;
  public final Executor executor;
  public final int maxLineLength;
  public final boolean withThreadInfo;

  LoggerConfig(boolean isLoggable, Level level, LogWriter logger, LoggingFormat format,
      Executor executor, int maxLineLength, boolean withThreadInfo) {
    this.isLoggable = isLoggable;
    this.level = level;
    this.logger = logger;
    this.format = format;
    this.executor = executor;
    this.maxLineLength = maxLineLength;
    this.withThreadInfo = withThreadInfo;
  }

  public static LoggerConfigBuilder builder() {
    return new LoggerConfigBuilder();
  }

  @Override
  public String toString() {
    final String line = "\n────────────────────────────────────────────────────────────────────────────────────────";
    return line
        + "\n LoggerConfig:"
        + line
        + "\n isLoggable     : " + isLoggable
        + "\n level          : " + level
        + "\n logger         : " + logger
        + "\n format         : " + format
        + "\n executor       : " + executor
        + "\n maxLineLength  : " + maxLineLength
        + "\n withThreadInfo : " + withThreadInfo
        + line;
  }

  public static class LoggerConfigBuilder {

    private boolean isLoggable = true;
    private Level level = Level.BASIC;
    private LoggingFormat format = LoggingFormat.JUL_MESSAGE_ONLY;
    private LogWriter logger = new DefaultLogger(this.format);
    private Executor executor;
    private int maxLineLength = 110;
    private boolean withThreadInfo = false;

    LoggerConfigBuilder() {
    }

    /**
     * @param isLoggable specifies if logger is enabled
     */
    public LoggerConfigBuilder loggable(boolean isLoggable) {
      this.isLoggable = isLoggable;
      return this;
    }

    /**
     * @param level sets logging level
     * @see Level
     */
    public LoggerConfigBuilder level(Level level) {
      this.level = level;
      return this;
    }

    /**
     * @param logger use this method to provide your logging interface implementation.
     *
     * Example:
     * <pre>
     *         LoggerConfig config = LoggerConfig.builder()
     *         .logger(new LogWriter() {
     *           final Logger log = LogManager.getLogger("OkHttpLogger");
     *
     *           @Override
     *           public void log(String msg) {
     *             log.debug(msg);
     *           }
     *         })
     *         .build();
     * </pre>
     */
    public LoggerConfigBuilder logger(LogWriter logger) {
      this.logger = logger;
      return this;
    }

    /**
     * @param format set format for default Java Utility Logger
     *
     * (will be ignored in case custom logger is used)
     */
    public LoggerConfigBuilder format(LoggingFormat format) {
      this.format = format;
      return this;
    }

    /**
     * @param executor manual executor override for printing
     */
    public LoggerConfigBuilder executor(Executor executor) {
      this.executor = executor;
      return this;
    }

    /**
     * @param length specifies max line length when printing request/response body
     *
     * Min value: 10, Max value: 500, Default: 110
     */
    public LoggerConfigBuilder maxLineLength(int length) {
      if (length < 10 || length > 500) {
        throw new IllegalArgumentException(
            "Invalid line length. Should be longer then 10 and shorter then 500.");
      } else {
        this.maxLineLength = length;
      }
      return this;
    }

    /**
     * @param withThreadInfo specifies if request executor thread name and timestamp should be
     * printed. Default: false
     *
     * <pre>
     * Example:
     *
     * ┌────── Request ────────────────────────────────────────────────────────────────────────
     * |
     * | Thread:  pool-31-thread-1                                   Sent:  2018-11-25 01:51:39
     * ├───────────────────────────────────────────────────────────────────────────────────────
     * </pre>
     */
    public LoggerConfigBuilder withThreadInfo(boolean withThreadInfo) {
      this.withThreadInfo = withThreadInfo;
      return this;
    }

    public LoggerConfig build() {
      return new LoggerConfig(isLoggable, level, logger,
          format, executor, maxLineLength, withThreadInfo);
    }
  }
}