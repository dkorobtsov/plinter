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
        LoggerConfig config) {
        final ExecutorService executor = (ExecutorService) config.executor;
        if (executor != null) {
            executor.execute(createFileResponseRunnable(config, responseDetails));
            handleThreadTermination(executor);
        } else {
            Printer.printFileResponse(config.logger, config.level,
                config.maxLineLength, responseDetails
            );
        }
    }

    public static void printJsonResponse(ResponseDetails responseDetails,
        LoggerConfig config) {
        final ExecutorService executor = (ExecutorService) config.executor;
        if (executor != null) {
            executor.execute(createPrintJsonResponseRunnable(config, responseDetails));
            handleThreadTermination(executor);
        } else {
            Printer.printJsonResponse(config.logger, config.level,
                config.maxLineLength, responseDetails);
        }
    }

    public static void printFileRequest(Request request, LoggerConfig config) {
        final ExecutorService executor = (ExecutorService) config.executor;
        if (executor != null) {
            executor.execute(createFileRequestRunnable(config, request));
            handleThreadTermination(executor);
        } else {
            Printer.printFileRequest(config.logger, config.level,
                config.maxLineLength, request);
        }
    }

    public static void printJsonRequest(Request request, LoggerConfig config) {
        final ExecutorService executor = (ExecutorService) config.executor;
        if (executor != null) {
            executor.execute(createPrintJsonRequestRunnable(config, request));
            handleThreadTermination(executor);
        } else {
            Printer.printJsonRequest(config.logger, config.level,
                config.maxLineLength, request);
        }
    }

    private static Runnable createFileResponseRunnable(final LoggerConfig config,
        final ResponseDetails responseDetails) {
        return () -> Printer
            .printFileResponse(config.logger, config.level,
                config.maxLineLength, responseDetails);
    }

    private static Runnable createPrintJsonResponseRunnable(final LoggerConfig config,
        final ResponseDetails responseDetails) {
        return () -> Printer
            .printJsonResponse(config.logger, config.level,
                config.maxLineLength, responseDetails);
    }

    private static Runnable createFileRequestRunnable(final LoggerConfig config,
        final Request request) {
        return () -> Printer.printFileRequest(config.logger, config.level,
            config.maxLineLength, request);
    }

    private static Runnable createPrintJsonRequestRunnable(final LoggerConfig config,
        final Request request) {
        return () -> Printer.printJsonRequest(config.logger, config.level,
            config.maxLineLength, request);
    }

    private static void handleThreadTermination(ExecutorService executor) {
        try {
            executor.awaitTermination(5, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
