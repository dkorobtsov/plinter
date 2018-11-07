package com.dkorobtsov.logging;

import okhttp3.Request;
import org.junit.Test;

import java.util.concurrent.Executors;

public class ClientPrintingExecutorNegativeUnitTests {

    @Test
    public void testInteruptingPrintingJsonRequestDoesntCrashProcess() {
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
    public void testInteruptingPrintingFileRequestDoesntCrashProcess() {
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
    public void testInteruptingPrintingJsonResponseDoesntCrashProcess() {
        final ResponseDetails responseDetails = ResponseDetails.builder().build();
        final LoggingInterceptor.Builder builder = new LoggingInterceptor.Builder()
            .executor(Executors.newCachedThreadPool());
        Thread.currentThread().interrupt();
        ClientPrintingExecutor.printFileResponse(responseDetails, builder);
    }

    @Test
    public void testInteruptingPrintingFileResponseDoesntCrashProcess() {
        final ResponseDetails responseDetails = ResponseDetails.builder().build();
        final LoggingInterceptor.Builder builder = new LoggingInterceptor.Builder()
            .executor(Executors.newCachedThreadPool());
        Thread.currentThread().interrupt();
        ClientPrintingExecutor.printFileResponse(responseDetails, builder);
    }
}
