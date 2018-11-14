package com.dkorobtsov.logging;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.Request;

public class ClientPrintingExecutor {

    private static final Logger logger = Logger.getLogger(ClientPrintingExecutor.class.getName());

    private ClientPrintingExecutor() {
    }

    public static void printFileResponse(ResponseDetails responseDetails,
        LoggingInterceptor.Builder builder) {
        final ExecutorService executor = (ExecutorService) builder.getExecutor();
        if (executor != null) {
            executor.execute(createFileResponseRunnable(builder, responseDetails));
            handleThreadTermination(executor);
        } else {
            Printer.printFileResponse(builder.getLogger(), builder.getLevel(), responseDetails);
        }
    }

    public static void printJsonResponse(ResponseDetails responseDetails,
        LoggingInterceptor.Builder builder) {
        final ExecutorService executor = (ExecutorService) builder.getExecutor();
        if (executor != null) {
            executor.execute(createPrintJsonResponseRunnable(builder, responseDetails));
            handleThreadTermination(executor);
        } else {
            Printer.printJsonResponse(builder.getLogger(), builder.getLevel(), responseDetails);
        }
    }

    public static void printFileRequest(Request request, LoggingInterceptor.Builder builder) {
        final ExecutorService executor = (ExecutorService) builder.getExecutor();
        if (executor != null) {
            executor.execute(createFileRequestRunnable(builder, request));
            handleThreadTermination(executor);
        } else {
            Printer.printFileRequest(builder.getLogger(), builder.getLevel(), request);
        }
    }

    public static void printJsonRequest(Request request, LoggingInterceptor.Builder builder) {
        final ExecutorService executor = (ExecutorService) builder.getExecutor();
        if (executor != null) {
            executor.execute(createPrintJsonRequestRunnable(builder, request));
            handleThreadTermination(executor);
        } else {
            Printer.printJsonRequest(builder.getLogger(), builder.getLevel(), request);
        }
    }

    private static Runnable createFileResponseRunnable(final LoggingInterceptor.Builder builder,
        ResponseDetails responseDetails) {
        return () -> Printer
            .printFileResponse(builder.getLogger(), builder.getLevel(), responseDetails);
    }

    private static Runnable createPrintJsonResponseRunnable(
        final LoggingInterceptor.Builder builder,
        ResponseDetails
            responseDetails) {
        return () -> Printer
            .printJsonResponse(builder.getLogger(), builder.getLevel(), responseDetails);
    }

    private static Runnable createFileRequestRunnable(final LoggingInterceptor.Builder builder,
        final Request request) {
        return () -> Printer.printFileRequest(builder.getLogger(), builder.getLevel(), request);
    }

    private static Runnable createPrintJsonRequestRunnable(final LoggingInterceptor.Builder builder,
        final Request request) {
        return () -> Printer.printJsonRequest(builder.getLogger(), builder.getLevel(), request);
    }

    private static void handleThreadTermination(ExecutorService executor) {
        try {
            executor.awaitTermination(5, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
