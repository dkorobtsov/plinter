package io.github.dkorobtsov.plinter.core;

import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Collection of Logger format configurations intended to be used with DefaultLogger.
 *
 * Default format: {@link LoggingFormat#JUL_MESSAGE_ONLY}
 *
 * NB. Note that formatting options provided in this class will not work in case interceptor is
 * configured to use custom {@link LogWriter} implementation.
 */
@SuppressWarnings({"MultipleStringLiterals", "AvoidSynchronizedAtMethodLevel", "PMD"})
public enum LoggingFormat {

  JUL_FULL(new SimpleFormatter() {
    @Override
    public synchronized String format(LogRecord lr) {
      return String.format("[%1$tF %1$tT][%2$s][%3$-7s] %4$s %n",
          new Date(lr.getMillis()),
          Thread.currentThread().getName(),
          lr.getLevel().getLocalizedName(),
          lr.getMessage()
      );
    }
  }),

  JUL_DATE_MESSAGE(new SimpleFormatter() {
    @Override
    public synchronized String format(LogRecord lr) {
      return String.format("[%1$tF %1$tT] %2$s %n",
          new Date(lr.getMillis()),
          lr.getMessage()
      );
    }
  }),

  JUL_DATE_LEVEL_MESSAGE(new SimpleFormatter() {
    @Override
    public synchronized String format(LogRecord lr) {
      return String.format("[%1$tF %1$tT] [%2$-7s] %3$s %n",
          new Date(lr.getMillis()),
          lr.getLevel().getLocalizedName(),
          lr.getMessage()
      );
    }
  }),

  JUL_LEVEL_MESSAGE(new SimpleFormatter() {
    @Override
    public synchronized String format(LogRecord lr) {
      return String.format("[%1$s] %2$s %n",
          lr.getLevel().getLocalizedName(),
          lr.getMessage()
      );
    }
  }),

  JUL_THREAD_MESSAGE(new SimpleFormatter() {
    @Override
    public synchronized String format(LogRecord lr) {
      return String.format("[%1$s] %2$s %n",
          Thread.currentThread().getName(),
          lr.getMessage()
      );
    }
  }),

  JUL_MESSAGE_ONLY(new SimpleFormatter() {
    @Override
    public synchronized String format(LogRecord lr) {
      return String.format("%1$s %n",
          lr.getMessage()
      );
    }
  });

  public final SimpleFormatter formatter;

  LoggingFormat(SimpleFormatter simpleFormatter) {
    this.formatter = simpleFormatter;
  }

}


