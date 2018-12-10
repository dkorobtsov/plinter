package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.internal.Util.APPLICATION_JSON;
import static org.junit.Assert.assertTrue;

import com.dkorobtsov.logging.interceptors.okhttp3.OkHttp3LoggingInterceptor;
import java.io.IOException;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class OutputResizingTest extends BaseTest {

    private static final String TEST_JSON = "{name: \"John\", age: 31, city: \"New York\"}";

    @Test
    @Parameters({
        "okhttp, false", "okhttp, true",
        "okhttp3, false", "okhttp3, true",
        "apacheHttpclientRequest, false", "apacheHttpclientRequest, true"
    })
    public void printerOutputResizingValidation(String loggerVersion,
        boolean provideExecutor) throws IOException {
        final List<String> loggerOutput = interceptedRequest(loggerVersion, provideExecutor,
            TEST_JSON, APPLICATION_JSON, false, 10);

        assertTrue("Interceptor should be able to log simple json body.",
            loggerOutput.contains("Method: @P"));
    }

    @Test(expected = IllegalArgumentException.class)
    @Parameters({
        "9", "501"
    })
    public void invalidOutputLengthHandling(String maxLineLength) {
        new OkHttp3LoggingInterceptor(
            LoggerConfig.builder()
                .maxLineLength(Integer.parseInt(maxLineLength))
                .build());
    }

    @Test
    @Parameters({
        "10", "500"
    })
    public void validOutputLengthHandling(String maxLineLength) {
        OkHttp3LoggingInterceptor interceptor = new OkHttp3LoggingInterceptor(
            LoggerConfig.builder()
                .maxLineLength(Integer.parseInt(maxLineLength))
                .build());

        Assert.assertEquals(Integer.parseInt(maxLineLength),
            interceptor.loggerConfig().maxLineLength);
    }

}
