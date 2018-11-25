package com.dkorobtsov.logging;

import com.dkorobtsov.logging.enums.HttpStatusCode;
import org.junit.Test;

public class StatusCodeTest {

    @Test(expected = IllegalArgumentException.class)
    public void unknownStatusCodeThrowsIllegalArgumentException() {
        HttpStatusCode.findMessage(999);
    }

}
