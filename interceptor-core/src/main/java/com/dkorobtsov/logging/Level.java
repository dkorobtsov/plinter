package com.dkorobtsov.logging;

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
