package com.dkorobtsov.logging;

import okhttp3.Request;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientPrintingExecutor {

    private static Runnable createPrintJsonRequestRunnable(final LoggingInterceptor.Builder builder,
                                                           final Request request) {
        return () -> Printer.printJsonRequest(builder.getLogger(), builder.getLevel(), request);
    }

    private static Runnable createFileRequestRunnable(final LoggingInterceptor.Builder builder,
                                                      final Request request) {
        return () -> Printer.printFileRequest(builder.getLogger(), builder.getLevel(), request);
    }

    private static Runnable createPrintJsonResponseRunnable(final LoggingInterceptor.Builder builder,
                                                            ResponseDetails
                                                                responseDetails) {
        return () -> Printer.printJsonResponse(builder.getLogger(), builder.getLevel(), responseDetails);
    }

    private static Runnable createFileResponseRunnable(final LoggingInterceptor.Builder builder,
                                                       ResponseDetails responseDetails) {
        return () -> Printer
            .printFileResponse(builder.getLogger(), builder.getLevel(), responseDetails);
    }



    public static void printFileResponse(ResponseDetails responseDetails, LoggingInterceptor.Builder builder) {
        final ExecutorService executor = (ExecutorService) builder.getExecutor();
        if (executor != null) {
            executor.execute(createFileResponseRunnable(builder, responseDetails));
            try {
                executor.awaitTermination(5, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Printer.printFileResponse(builder.getLogger(), builder.getLevel(), responseDetails);
        }
    }

    public static void printJsonResponse(ResponseDetails responseDetails, LoggingInterceptor.Builder builder) {
        final ExecutorService executor = (ExecutorService) builder.getExecutor();
        if (executor != null) {
            executor.execute(createPrintJsonResponseRunnable(builder, responseDetails));
            try {
                executor.awaitTermination(5, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Printer.printJsonResponse(builder.getLogger(), builder.getLevel(), responseDetails);
        }
    }

    public static void printFileRequest(Request request, LoggingInterceptor.Builder builder) {
        final ExecutorService executor = (ExecutorService) builder.getExecutor();
        if (executor != null) {
            executor.execute(createFileRequestRunnable(builder, request));
            try {
                executor.awaitTermination(5, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Printer.printFileRequest(builder.getLogger(), builder.getLevel(), request);
        }
    }

    public static void printJsonRequest(Request request, LoggingInterceptor.Builder builder) {
        final ExecutorService executor = (ExecutorService) builder.getExecutor();
        if (executor != null) {
            executor.execute(createPrintJsonRequestRunnable(builder, request));
            try {
                executor.awaitTermination(5, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Printer.printJsonRequest(builder.getLogger(), builder.getLevel(), request);
        }
    }
}
