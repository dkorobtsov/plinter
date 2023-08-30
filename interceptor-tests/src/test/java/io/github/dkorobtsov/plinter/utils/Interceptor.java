package io.github.dkorobtsov.plinter.utils;

/**
 * Supported interceptor types - intended to be used as parameter in data-driven jUnit tests.
 */
public enum Interceptor {

  /**
   * Interceptor for OkHttp.
   */
  OKHTTP("okhttp"),

  /**
   * Interceptor for OkHttp3.
   */
  OKHTTP3("okhttp3"),

  /**
   * Interceptor for Apache HttpClient request.
   */
  APACHE_HTTPCLIENT_REQUEST("apacheHttpclientRequest");

  /**
   * The name of the interceptor.
   */
  @SuppressWarnings("unused")
  public final String name;

  /**
   * Constructs an Interceptor with the specified name.
   *
   * @param name The name of the interceptor.
   */
  Interceptor(String name) {
    this.name = name;
  }

  /**
   * Returns the Interceptor enum constant corresponding to the given name.
   *
   * @param name The name of the interceptor.
   * @return The Interceptor enum constant.
   * @throws IllegalArgumentException If the given name does not
   *                                  match any known interceptor version.
   */
  public static Interceptor fromString(String name) {
    switch (name) {
      case "okhttp":
        return OKHTTP;
      case "okhttp3":
        return OKHTTP3;
      case "apacheHttpclientRequest":
        return APACHE_HTTPCLIENT_REQUEST;
      default:
        throw new IllegalArgumentException("Unknown interceptor version: " + name);
    }
  }

}
