package com.dkorobtsov.logging;

/**
 * LogWriter interface responsible for logging intercepted events.
 *
 * Usage example:
 * <pre>
 *   LoggerConfig.builder()
 *     .logger(new LogWriter() {
 *       final Logger logger = LogManager.getLogger("HttpLogger");
 *
 *       Override
 *       public void log(String msg) {
 *         logger.debug(msg);
 *       }
 *     })
 *     .build());
 * </pre>
 */
public interface LogWriter {

  void log(String msg);
}
