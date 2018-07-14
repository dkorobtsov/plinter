package com.dkorobtsov.logging;

import okhttp3.internal.platform.Platform;

/**
 * @author ihsan on 11/07/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public interface Logger {

  Logger DEFAULT = new Logger() {
    @Override
    public void log(int level, String tag, String message) {
      Platform.get().log(level, message, null);
    }
  };

  void log(int level, String tag, String msg);
}
