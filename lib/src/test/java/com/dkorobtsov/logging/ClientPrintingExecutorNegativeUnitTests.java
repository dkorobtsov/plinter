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
        ClientPrintingExecutor.printJsonRequest(request, loggerConfig);
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
        ClientPrintingExecutor.printFileRequest(request, loggerConfig);
    }

    @Test
    public void testInterruptingPrintingJsonResponseDoesntCrashProcess() {
        final ResponseDetails responseDetails = ResponseDetails.builder().build();
        final LoggerConfig loggerConfig = LoggerConfig.builder()
            .executor(Executors.newCachedThreadPool()).build();

        Thread.currentThread().interrupt();
        ClientPrintingExecutor.printFileResponse(responseDetails, loggerConfig);
    }

    @Test
    public void testInterruptingPrintingFileResponseDoesntCrashProcess() {
        final ResponseDetails responseDetails = ResponseDetails.builder().build();
        final LoggerConfig loggerConfig = LoggerConfig.builder()
            .executor(Executors.newCachedThreadPool()).build();

        Thread.currentThread().interrupt();
        ClientPrintingExecutor.printFileResponse(responseDetails, loggerConfig);
    }
}
