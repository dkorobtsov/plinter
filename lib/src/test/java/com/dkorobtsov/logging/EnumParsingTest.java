package com.dkorobtsov.logging;

import com.dkorobtsov.logging.internal.HttpStatus;
import com.dkorobtsov.logging.utils.Interceptor;
import org.junit.Test;

public class EnumParsingTest {

  @Test(expected = IllegalArgumentException.class)
  public void unknownHttpStatusCodeThrowsIllegalArgumentException() {
    HttpStatus.fromCode(999);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test(expected = IllegalArgumentException.class)
  public void unknownInterceptorThrowsIllegalArgumentException() {
    Interceptor.fromString("UnknownInterceptor");
  }

}
