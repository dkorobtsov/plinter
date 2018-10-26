package com.dkorobtsov.logging;

class TextUtils {

  private TextUtils() {
  }

  static boolean isEmpty(CharSequence str) {
    return str == null || str.length() == 0;
  }
}
