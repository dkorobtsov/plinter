package com.dkorobtsov.logging;

import com.dkorobtsov.logging.internal.InterceptedRequest;
import java.util.concurrent.Executors;
import org.junit.Test;

public class ClientPrintingExecutorNegativeUnitTests {

    @Test
    public void testInterruptingPrintingJsonRequestDoesntCrashProcess() {
        final InterceptedRequest request = new InterceptedRequest.Builder()
            .get()
            .url("http://google.com")
            .build();

        final LoggerConfig loggerConfig = LoggerConfig.builder()
            .executor(Executors.newCachedThreadPool()).build();

        Thread.currentThread().interrupt();
        ClientPrintingExecutor.printRequest(loggerConfig, request, false);
    }

    @Test
    public void testInterruptingPrintingFileRequestDoesntCrashProcess() {
        final InterceptedRequest request = new InterceptedRequest.Builder()
            .get()
            .url("http://google.com")
            .build();

        final LoggerConfig loggerConfig = LoggerConfig.builder()
            .executor(Executors.newCachedThreadPool()).build();

        Thread.currentThread().interrupt();
        ClientPrintingExecutor.printRequest(loggerConfig, request, true);
    }

    @Test
    public void testInterruptingPrintingResponseDoesntCrashProcess() {
        final InterceptedResponse responseDetails = InterceptedResponse.builder().build();
        final LoggerConfig loggerConfig = LoggerConfig.builder()
            .executor(Executors.newCachedThreadPool()).build();

        Thread.currentThread().interrupt();
        ClientPrintingExecutor.printResponse(loggerConfig, responseDetails);
    }

}
