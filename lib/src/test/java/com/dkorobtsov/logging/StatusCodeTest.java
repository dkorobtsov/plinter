package com.dkorobtsov.logging;

import org.junit.Test;

public class StatusCodeTest {

    @Test(expected = IllegalArgumentException.class)
    public void unknownStatusCodeThrowsIllegalArgumentException() {
        HttpStatusCodes.findMessage(999);
    }

}
