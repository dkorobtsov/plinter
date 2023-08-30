package io.github.dkorobtsov.plinter.core.internal;

import io.github.dkorobtsov.plinter.core.LoggerConfig;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class for executing printing requests. Depending on provided {@link LoggerConfig} events
 * are printed in current thread or by manually provided executor.
 */
public final class ClientPrintingExecutor {

  private static final Logger logger = Logger.getLogger(ClientPrintingExecutor.class.getName());

  private ClientPrintingExecutor() {
  }

  public static void printRequest(LoggerConfig loggerConfig, InterceptedRequest request) {
    final Runnable printRequest = () -> Printer.printRequest(loggerConfig, request);
    final ExecutorService executor = (ExecutorService) loggerConfig.executor;
    sendCommandToPrinter(executor, printRequest);
  }

  public static void printResponse(LoggerConfig loggerConfig, InterceptedResponse response) {
    final Runnable printResponse = () -> Printer.printResponse(loggerConfig, response);
    final ExecutorService executor = (ExecutorService) loggerConfig.executor;
    sendCommandToPrinter(executor, printResponse);
  }

  @SuppressWarnings("PMD")
  private static void sendCommandToPrinter(ExecutorService executor, Runnable command) {
    if (Objects.isNull(executor)) {
      command.run();
    } else {
      try {
        executor.execute(command);
        //noinspection ResultOfMethodCallIgnored
        executor.awaitTermination(5, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        Thread.currentThread().interrupt();
      }
    }
  }
}
