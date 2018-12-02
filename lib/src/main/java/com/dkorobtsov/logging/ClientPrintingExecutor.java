package com.dkorobtsov.logging;

import com.dkorobtsov.logging.internal.InterceptedRequest;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientPrintingExecutor {

    private static final Logger logger = Logger.getLogger(ClientPrintingExecutor.class.getName());

    private ClientPrintingExecutor() {
    }

    public static void printRequest(final LoggerConfig loggerConfig,
        final InterceptedRequest request, final boolean hasPrintableBody) {
        Runnable printRequest = () -> Printer.printRequest(loggerConfig, request, hasPrintableBody);
        final ExecutorService executor = (ExecutorService) loggerConfig.executor;
        sendCommandToPrinter(executor, printRequest);
    }

    public static void printResponse(LoggerConfig loggerConfig,
        InterceptedResponse responseDetails) {
        Runnable printResponse = () -> Printer.printResponse(loggerConfig, responseDetails);
        final ExecutorService executor = (ExecutorService) loggerConfig.executor;
        sendCommandToPrinter(executor, printResponse);
    }

    private static void sendCommandToPrinter(ExecutorService executor, Runnable command) {
        if (Objects.isNull(executor)) {
            command.run();
        } else {
            try {
                executor.execute(command);
                executor.awaitTermination(5, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
