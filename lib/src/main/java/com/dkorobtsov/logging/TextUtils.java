package com.dkorobtsov.logging;

/**
 * @author ihsan on 30/03/2017.
 */
class TextUtils {

  private TextUtils() {
  }

  static boolean isEmpty(CharSequence str) {
    return str == null || str.length() == 0;
  }
}
