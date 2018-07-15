package com.dkorobtsov.logging;

public interface LogWriter {

  void log(int level, String msg);
}
