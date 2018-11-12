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
        final LoggingInterceptor.Builder builder = new LoggingInterceptor.Builder()
            .executor(Executors.newCachedThreadPool());
        Thread.currentThread().interrupt();
        ClientPrintingExecutor.printJsonRequest(request, builder);
    }

    @Test
    public void testInterruptingPrintingFileRequestDoesntCrashProcess() {
        final Request request = new Request.Builder()
            .get()
            .url("http://google.com")
            .build();
        final LoggingInterceptor.Builder builder = new LoggingInterceptor.Builder()
            .executor(Executors.newCachedThreadPool());
        Thread.currentThread().interrupt();
        ClientPrintingExecutor.printFileRequest(request, builder);
    }

    @Test
    public void testInterruptingPrintingJsonResponseDoesntCrashProcess() {
        final ResponseDetails responseDetails = ResponseDetails.builder().build();
        final LoggingInterceptor.Builder builder = new LoggingInterceptor.Builder()
            .executor(Executors.newCachedThreadPool());
        Thread.currentThread().interrupt();
        ClientPrintingExecutor.printFileResponse(responseDetails, builder);
    }

    @Test
    public void testInterruptingPrintingFileResponseDoesntCrashProcess() {
        final ResponseDetails responseDetails = ResponseDetails.builder().build();
        final LoggingInterceptor.Builder builder = new LoggingInterceptor.Builder()
            .executor(Executors.newCachedThreadPool());
        Thread.currentThread().interrupt();
        ClientPrintingExecutor.printFileResponse(responseDetails, builder);
    }
}
