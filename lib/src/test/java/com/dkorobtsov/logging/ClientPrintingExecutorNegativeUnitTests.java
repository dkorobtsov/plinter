package com.dkorobtsov.logging;

import java.util.concurrent.Executors;
import okhttp3.Request;
import org.junit.Test;

public class ClientPrintingExecutorNegativeUnitTests {

    @Test
    public void testInterruptingPrintingJsonRequestDoesntCrashProcess() {
        final Request request = new Request.Builder()
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
        final Request request = new Request.Builder()
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
