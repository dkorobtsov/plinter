package com.dkorobtsov.logging;

import okhttp3.internal.platform.Platform;

/**
 * @author ihsan on 11/07/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public interface LogWriter {

  LogWriter DEFAULT = new LogWriter() {
    @Override
    public void log(int level, String tag, String message) {
      Platform.get().log(level, message, null);
    }
  };

  void log(int level, String tag, String msg);
}
