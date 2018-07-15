package com.dkorobtsov.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.internal.platform.Platform;

public class DefaultLogger implements LogWriter {

  private static Logger defaultLogger = Logger.getLogger("DefaultLogger");

  DefaultLogger(LogFormatter logFormatter) {
    defaultLogger.setUseParentHandlers(false);
    ConsoleHandler handler = new ConsoleHandler();
    handler.setFormatter(logFormatter.formatter);
    defaultLogger.addHandler(handler);
  }

  @Override
  public void log(int type, String msg) {
    switch (type) {
      case Platform.INFO:
        defaultLogger.log(Level.INFO, msg);
        break;
      default:
        defaultLogger.log(Level.WARNING, msg);
        break;
    }
  }
}
