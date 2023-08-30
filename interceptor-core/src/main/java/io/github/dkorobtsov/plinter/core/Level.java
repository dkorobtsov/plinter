package io.github.dkorobtsov.plinter.core;

/**
 * Logging levels regulating Printer output. Depending on chosen level will be printed full
 * requests/responses, only headers, only body or nothing at all.
 */
@SuppressWarnings("JavadocStyle")
public enum Level {
  /**
   * No logs.
   */
  NONE,
  /**
   * Basic Logging, will include both body and headers.
   * Example:
   * <pre>{@code
   *  - URL
   *  - Method
   *  - Headers
   *  - Body
   * }</pre>
   */
  BASIC,
  /**
   * Only headers.
   * Example:
   * <pre>{@code
   *  - URL
   *  - Method
   *  - Headers
   * }</pre>
   */
  HEADERS,
  /**
   * Will log only body.
   * Example:
   * <pre>{@code
   *  - URL
   *  - Method
   *  - Body
   * }</pre>
   */
  BODY
}
