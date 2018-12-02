package com.dkorobtsov.logging;

import com.dkorobtsov.logging.internal.InterceptedRequest;
import java.util.concurrent.Executors;
import org.junit.Test;

public class ClientPrintingExecutorNegativeUnitTests {

    @Test
    public void testInterruptingPrintingJsonRequestDoesntCrashProcess() {
        final InterceptedRequest request = new InterceptedRequest.Builder()
            .get()
            .addHeader("Content-type", "application/json")
            .url("http://google.com")
            .build();

        final LoggerConfig loggerConfig = LoggerConfig.builder()
            .executor(Executors.newCachedThreadPool()).build();

        Thread.currentThread().interrupt();
        ClientPrintingExecutor.printRequest(loggerConfig, request);
    }

    @Test
    public void testInterruptingPrintingFileRequestDoesntCrashProcess() {
        final InterceptedRequest request = new InterceptedRequest.Builder()
            .get()
            .addHeader("Content-type", "application/zip")
            .url("http://google.com")
            .build();

        final LoggerConfig loggerConfig = LoggerConfig.builder()
            .executor(Executors.newCachedThreadPool()).build();

        Thread.currentThread().interrupt();
        ClientPrintingExecutor.printRequest(loggerConfig, request);
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
