package com.dkorobtsov.logging;

import com.dkorobtsov.logging.internal.HttpStatusCode;
import com.dkorobtsov.logging.utils.InterceptorVersion;
import org.junit.Test;

public class EnumParsingTest {

    @Test(expected = IllegalArgumentException.class)
    public void unknownHttpStatusCodeThrowsIllegalArgumentException() {
        HttpStatusCode.findMessage(999);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void unknownInterceptorVersionThrowsIllegalArgumentException() {
        InterceptorVersion.parse("UnknownInterceptor");
    }

}
