/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.dkorobtsov.plinter.core.internal;

/**
 * NB: Class copied with some small modifications from OkHttp3 client (removed external dependencies
 * and unused methods). Idea was to remove hard dependency on OkHttp3, so request/response handling
 * logic was made a part of this library.
 *
 * <p>See <a href="https://github.com/square/okhttp">OkHttp3</a>.
 */
@SuppressWarnings({
  "MissingJavadocMethod",
  "MissingJavadocType",
  "PMD"
})
public final class HttpMethod {

  private HttpMethod() {
  }

  public static boolean requiresRequestBody(String method) {
    return method.equals("POST")
      || method.equals("PUT")
      || method.equals("PATCH")
      // WebDAV
      || method.equals("PROPPATCH")
      // CalDAV/CardDAV (defined in WebDAV Versioning)
      || method.equals("REPORT");
  }

  public static boolean permitsRequestBody(String method) {
    return requiresRequestBody(method)
      || method.equals("OPTIONS")
      // Permitted as spec is ambiguous.
      || method.equals("DELETE")
      // (WebDAV) without body: request <allprop/>
      || method.equals("PROPFIND")
      // (WebDAV) may contain a body, but behaviour is unspecified
      || method.equals("MKCOL")
      // (WebDAV) body: create lock, without body: refresh lock
      || method.equals("LOCK");
  }

}
