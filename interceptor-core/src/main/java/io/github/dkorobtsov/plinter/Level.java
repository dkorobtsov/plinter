package io.github.dkorobtsov.plinter;

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
   * Example:
   * <pre>{@code
   *  - URL
   *  - Method
   *  - Headers
   * }</pre>
   */
  HEADERS,
  /**
   * Example:
   * <pre>{@code
   *  - URL
   *  - Method
   *  - Body
   * }</pre>
   */
  BODY
}
