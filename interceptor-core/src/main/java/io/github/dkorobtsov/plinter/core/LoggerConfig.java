package io.github.dkorobtsov.plinter.core;

import java.util.concurrent.Executor;

/**
 * This class is used to customize Interceptor's behavior. Check {@link LoggerConfigBuilder} for
 * default values.
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class LoggerConfig {

  public final Level level;
  public final LogWriter logger;
  public final Executor executor;
  public final int maxLineLength;
  public final boolean isLoggable;
  public final boolean logByLine;
  public final boolean withThreadInfo;
  public final LoggingFormat format;

  LoggerConfig(boolean isLoggable, Level level, LogWriter logger, boolean logByLine,
               LoggingFormat format, Executor executor, int maxLineLength,
               boolean withThreadInfo) {
    this.logByLine = logByLine;
    this.withThreadInfo = withThreadInfo;
    this.maxLineLength = maxLineLength;
    this.isLoggable = isLoggable;
    this.executor = executor;
    this.format = format;
    this.logger = logger;
    this.level = level;
  }

  /**
   * Constructs a new LoggerConfigBuilder.
   *
   * @return the LoggerConfigBuilder instance
   */
  public static LoggerConfigBuilder builder() {
    return new LoggerConfigBuilder();
  }

  /**
   * Returns a string representation of the LoggerConfig object.
   *
   * @return the string representation of the LoggerConfig object
   */
  @Override
  public String toString() {
    final char lineChar = '─';
    final int lineLength = 80;
    String line = String.valueOf(lineChar).repeat(lineLength);

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
   * <p>
   * To use default configuration use: LoggerConfig.builder().build()
   */
  public static class LoggerConfigBuilder {

    private LoggingFormat format = LoggingFormat.JUL_MESSAGE_ONLY;
    private LogWriter logger = new DefaultLogger(this.format);
    private boolean isLoggable = true;
    private Level level = Level.BASIC;
    private int maxLineLength = 110;
    private boolean logByLine;
    private boolean withThreadInfo;
    private Executor executor;

    /**
     * Sets whether the logger is enabled.
     *
     * @param isLoggable specifies if the logger is enabled
     * @return the LoggerConfigBuilder instance
     */
    public LoggerConfigBuilder loggable(boolean isLoggable) {
      this.isLoggable = isLoggable;
      return this;
    }

    /**
     * Sets the logging level.
     *
     * @param level the logging level
     * @return the LoggerConfigBuilder instance
     * @see Level
     */
    public LoggerConfigBuilder level(Level level) {
      this.level = level;
      return this;
    }

    /**
     * Sets the logger implementation.
     * <p>
     * Usage Example:
     * <pre>
     *                       LoggerConfig config = LoggerConfig.builder()
     *                       .logger(new LogWriter() {
     *                         final Logger log = LogManager.getLogger("OkHttpLogger");
     *
     *                         Override
     *                         public void log(String msg) {
     *                           log.debug(msg);
     *                         }
     *                       })
     *                       .build();
     *               </pre>
     *
     * @param logger use this method to provide your logging interface implementation.
     *               <p>
     * @return the LoggerConfigBuilder instance
     */
    public LoggerConfigBuilder logger(LogWriter logger) {
      this.logger = logger;
      return this;
    }

    /**
     * Sets the format for the default Java Utility Logger.
     *
     * @param format set format for default Java Utility Logger
     *               <p>
     *               (will be ignored in case custom logger is used)
     * @return the LoggerConfigBuilder instance
     */
    public LoggerConfigBuilder format(LoggingFormat format) {
      this.format = format;
      return this;
    }

    /**
     * Sets the executor for printing.
     *
     * @param executor the executor for printing
     * @return the LoggerConfigBuilder instance
     */
    public LoggerConfigBuilder executor(Executor executor) {
      this.executor = executor;
      return this;
    }

    /**
     * Sets the maximum line length when printing request/response body.
     *
     * @param length specifies max line length when printing request/response body
     *               <p>
     *               Min value: 80, Max value: 180, Default: 110
     * @return the LoggerConfigBuilder instance
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
     * Sets whether to log events line by line or as a single log message.
     *
     * @param logByLine if true event will be printed line by line
     *                  (each line - separate log event), otherwise
     *                  whole event will be printed as a single log message
     *                  <p>
     *                  Default: false
     * @return the LoggerConfigBuilder instance
     */
    public LoggerConfigBuilder logByLine(boolean logByLine) {
      this.logByLine = logByLine;
      return this;
    }

    /**
     * Sets whether to include request executor thread name and timestamp in the logs.
     * <p>
     * Example:
     * <pre>
     *   ┌────── Request ────────────────────────────────────────────────────────────────────────
     *   |
     *   | Thread:  pool-31-thread-1                                   Sent:  2018-11-25 01:51:39
     *   ├───────────────────────────────────────────────────────────────────────────────────────
     *   </pre>
     *
     * @param withThreadInfo specifies if request executor thread name and timestamp should be
     *                       printed. Default: false
     * @return the LoggerConfigBuilder instance
     */
    public LoggerConfigBuilder withThreadInfo(boolean withThreadInfo) {
      this.withThreadInfo = withThreadInfo;
      return this;
    }

    /**
     * Builds a LoggerConfig object with the configured values.
     *
     * @return the LoggerConfig object
     */
    public LoggerConfig build() {
      return new LoggerConfig(isLoggable, level, logger,
        logByLine, format, executor, maxLineLength, withThreadInfo);
    }
  }

}
