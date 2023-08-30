package io.github.dkorobtsov.plinter.core.internal;


/**
 * Helper class for collecting all response details from external library's response and provide it
 * to {@link ResponseHandler} for internal InterceptedResponse instance creation.
 */
@SuppressWarnings({"PMD.LinguisticNaming", "PMD.AvoidFieldNameMatchingMethodName"})
public class ResponseDetails {

  /**
   * The response body of the intercepted response.
   */
  public final InterceptedResponseBody responseBody;
  /**
   * The media type of the intercepted response.
   */
  public final InterceptedMediaType mediaType;
  /**
   * The headers of the intercepted response.
   */
  public final InterceptedHeaders headers;
  /**
   * Indicates whether the intercepted response is successful or not.
   */
  public final boolean isSuccessful;
  /**
   * The protocol of the intercepted response.
   */
  public final Protocol protocol;
  /**
   * The message of the intercepted response.
   */
  public final String message;
  /**
   * Status code of the intercepted response.
   */
  public final int code;

  /**
   * Constructs a new ResponseDetails object.
   *
   * @param responseBody The response body of the intercepted response.
   * @param headers      The headers of the intercepted response.
   * @param code         The code of the intercepted response.
   * @param isSuccessful Indicates whether the intercepted response is successful or not.
   * @param message      The message of the intercepted response.
   * @param mediaType    The media type of the intercepted response.
   * @param protocol     The protocol of the intercepted response.
   */
  ResponseDetails(InterceptedResponseBody responseBody, InterceptedHeaders headers, int code,
                  boolean isSuccessful, String message, InterceptedMediaType mediaType, Protocol protocol) {

    this.responseBody = responseBody;
    this.isSuccessful = isSuccessful;
    this.mediaType = mediaType;
    this.protocol = protocol;
    this.message = message;
    this.headers = headers;
    this.code = code;
  }

  /**
   * Returns a new instance of ResponseDetailsBuilder.
   *
   * @return The ResponseDetailsBuilder instance.
   */
  public static ResponseDetailsBuilder builder() {
    return new ResponseDetailsBuilder();
  }

  /**
   * Returns the string representation of the ResponseDetails object.
   *
   * @return The string representation of the ResponseDetails object.
   */
  @Override
  public String toString() {
    return "ResponseDetails{"
      + "responseBody=" + responseBody
      + ", headers=" + headers
      + ", code=" + code
      + ", isSuccessful=" + isSuccessful
      + ", message='" + message + '\''
      + ", mediaType=" + mediaType
      + ", protocol=" + protocol
      + '}';
  }

  /**
   * Builder class for constructing ResponseDetails objects.
   */
  public static class ResponseDetailsBuilder {

    private InterceptedResponseBody responseBody;
    private InterceptedMediaType mediaType;
    private InterceptedHeaders headers;
    private boolean isSuccessful;
    private Protocol protocol;
    private String message;
    private int code;

    /**
     * Sets the response body for the ResponseDetailsBuilder.
     *
     * @param responseBody The response body to set.
     * @return The ResponseDetailsBuilder instance.
     */
    public ResponseDetails.ResponseDetailsBuilder responseBody(
      InterceptedResponseBody responseBody) {
      this.responseBody = responseBody;
      return this;
    }

    /**
     * Sets the media type for the ResponseDetailsBuilder.
     *
     * @param mediaType The media type to set.
     * @return The ResponseDetailsBuilder instance.
     */
    public ResponseDetails.ResponseDetailsBuilder mediaType(InterceptedMediaType mediaType) {
      this.mediaType = mediaType;
      return this;
    }

    /**
     * Sets the headers for the ResponseDetailsBuilder.
     *
     * @param headers The headers to set.
     * @return The ResponseDetailsBuilder instance.
     */
    public ResponseDetails.ResponseDetailsBuilder headers(InterceptedHeaders headers) {
      this.headers = headers;
      return this;
    }

    /**
     * Sets the success status for the ResponseDetailsBuilder.
     *
     * @param isSuccessful The success status to set.
     * @return The ResponseDetailsBuilder instance.
     */
    public ResponseDetails.ResponseDetailsBuilder isSuccessful(boolean isSuccessful) {
      this.isSuccessful = isSuccessful;
      return this;
    }

    /**
     * Sets the protocol for the ResponseDetailsBuilder.
     *
     * @param protocol The protocol to set.
     * @return The ResponseDetailsBuilder instance.
     */
    public ResponseDetails.ResponseDetailsBuilder protocol(Protocol protocol) {
      this.protocol = protocol;
      return this;
    }

    /**
     * Sets the message for the ResponseDetailsBuilder.
     *
     * @param message The message to set.
     * @return The ResponseDetailsBuilder instance.
     */
    public ResponseDetails.ResponseDetailsBuilder message(String message) {
      this.message = message;
      return this;
    }

    /**
     * Sets status code for the ResponseDetailsBuilder.
     *
     * @param code The code to set.
     * @return The ResponseDetailsBuilder instance.
     */
    public ResponseDetails.ResponseDetailsBuilder code(int code) {
      this.code = code;
      return this;
    }

    /**
     * Builds a new ResponseDetails object with the provided values.
     *
     * @return The constructed ResponseDetails object.
     */
    public ResponseDetails build() {
      return new ResponseDetails(responseBody, headers, code, isSuccessful, message, mediaType,
        protocol);
    }

    /**
     * Returns the string representation of the ResponseDetailsBuilder object.
     *
     * @return The string representation of the ResponseDetailsBuilder object.
     */
    @Override
    public String toString() {
      return "ResponseDetails.ResponseDetailsBuilder(responseBody=" + this.responseBody
        + ", headers=" + this.headers
        + ", code=" + this.code
        + ", isSuccessful=" + this.isSuccessful
        + ", message=" + this.message
        + ", mediaType=" + this.mediaType + ")";
    }
  }
}
