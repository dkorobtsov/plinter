package com.dkorobtsov.logging;

import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public enum LogFormatter {

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

  final SimpleFormatter formatter;
  LogFormatter defaultFormat;

  LogFormatter(SimpleFormatter simpleFormatter) {
    this.formatter = simpleFormatter;
  }

  void setDefault(LogFormatter formatter){
    this.defaultFormat = formatter;
  }
}
