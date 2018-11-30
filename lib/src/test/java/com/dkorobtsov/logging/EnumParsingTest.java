package com.dkorobtsov.logging;

import com.dkorobtsov.logging.enums.HttpStatusCode;
import com.dkorobtsov.logging.enums.InterceptorVersion;
import org.junit.Test;

public class EnumParsingTest {

    @Test(expected = IllegalArgumentException.class)
    public void unknownHttpStatusCodeThrowsIllegalArgumentException() {
        HttpStatusCode.findMessage(999);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownInterceptorVersionThrowsIllegalArgumentException() {
        InterceptorVersion.parse("UnknownInterceptor");
    }

}
