package io.github.dkorobtsov.plinter.core;

/**
 * Parent class for all interceptors - to keep some common logic.
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public abstract class AbstractInterceptor {

  protected LoggerConfig loggerConfig;

  public LoggerConfig loggerConfig() {
    return this.loggerConfig;
  }

  protected boolean skipLogging() {
    return !loggerConfig.isLoggable || loggerConfig.level == Level.NONE;
  }

}
