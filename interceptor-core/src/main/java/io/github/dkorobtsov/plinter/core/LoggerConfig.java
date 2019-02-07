package io.github.dkorobtsov.plinter.core;

import java.util.concurrent.Executor;

/**
 * This class is used to customize Interceptor's behavior. Check {@link LoggerConfigBuilder} for
 * default values.
 */
public class LoggerConfig {

  public final Level level;
  public final LogWriter logger;
  public final Executor executor;
  public final int maxLineLength;
  public final boolean isLoggable;
  public final boolean withThreadInfo;
  public final LoggingFormat format;

  LoggerConfig(boolean isLoggable, Level level, LogWriter logger, LoggingFormat format,
      Executor executor, int maxLineLength, boolean withThreadInfo) {
    this.withThreadInfo = withThreadInfo;
    this.maxLineLength = maxLineLength;
    this.isLoggable = isLoggable;
    this.executor = executor;
    this.format = format;
    this.logger = logger;
    this.level = level;
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

  /**
   * Helper class for creating {@link LoggerConfig} instances.
   *
   * To use default configuration use: LoggerConfig.builder().build()
   */
  public static class LoggerConfigBuilder {

    private LoggingFormat format = LoggingFormat.JUL_MESSAGE_ONLY;
    private LogWriter logger = new DefaultLogger(this.format);
    private boolean isLoggable = true;
    private Level level = Level.BASIC;
    private int maxLineLength = 110;
    private boolean withThreadInfo;
    private Executor executor;

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
     *           Override
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
     * Min value: 80, Max value: 180, Default: 110
     */
    public LoggerConfigBuilder maxLineLength(int length) {
      if (length < 80 || length > 180) {
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
