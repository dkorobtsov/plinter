package io.github.dkorobtsov.plinter.utils;

import io.github.dkorobtsov.plinter.core.LogWriter;
import io.github.dkorobtsov.plinter.core.LoggingFormat;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.stream.Collectors;

import static io.github.dkorobtsov.plinter.utils.TestUtil.PRINTING_THREAD_PREFIX;

/**
 * DefaultLogger double with additional methods for testing purposes. All published events are
 * registered and can be retrieved for validation.
 */
@SuppressWarnings("PMD")
public class TestLogger implements LogWriter {

  private static final org.apache.logging.log4j.Logger logger
    = org.apache.logging.log4j.LogManager.getLogger(TestLogger.class.getName());

  private static final String REGEX_LINE_SEPARATOR = "\r?\n";
  private static final int MAX_WAIT_TIME_MS = 3000;
  private static final int SLEEP_INTERVAL_MS = 2;
  private final List<String> events = new ArrayList<>(Collections.emptyList());
  private final StreamHandler logOutputHandler;
  private final OutputStream logOut;
  private final Logger testLogger = Logger.getLogger(TestLogger.class.getName());

  /**
   * Constructs a TestLogger with the specified logging format.
   *
   * @param logFormatter The logging format to use.
   */
  public TestLogger(LoggingFormat logFormatter) {
    testLogger.setUseParentHandlers(false);

    // Removing existing handlers for new instance
    Arrays.stream(testLogger.getHandlers()).forEach(testLogger::removeHandler);

    // Configuring output to console
    final ConsoleHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setFormatter(logFormatter.formatter);
    testLogger.addHandler(consoleHandler);

    // Configuring output to stream
    logOut = new ByteArrayOutputStream();
    logOutputHandler = new StreamHandler(logOut, logFormatter.formatter);

    testLogger.addHandler(logOutputHandler);
  }

  /**
   * Logs the specified message and registers it as an event.
   *
   * @param msg The message to log.
   */
  @Override
  public void log(String msg) {
    testLogger.info(msg);
    events.add(msg);
  }

  /**
   * Returns the raw messages published by the current logger.
   *
   * @return The raw messages.
   */
  private List<String> rawMessages() {
    return events;
  }

  /**
   * Returns the first formatted event published by the current logger.
   *
   * @return The first formatted event.
   */
  @SuppressWarnings("unused")
  public String firstRawEvent() {
    return rawMessages().get(0).trim();
  }

  /**
   * Returns the last formatted event published by the current logger.
   *
   * @return The last formatted event.
   */
  @SuppressWarnings("unused")
  String lastRawEvent() {
    return rawMessages().get(rawMessages().size() - 1).trim();
  }

  /**
   * Returns all formatted events published by the current logger as a string.
   *
   * @return The formatted output.
   */
  public String formattedOutput() {
    waitForPrinterThreadToFinish();
    logOutputHandler.flush();
    logOutputHandler.close();
    return logOut.toString();
  }

  /**
   * Waits until any active Printer thread becomes idle, up to a maximum of 3 seconds.
   */
  @SuppressWarnings("BusyWait") // by design
  private void waitForPrinterThreadToFinish() {
    // Hack for cases when printer is working in separate thread
    // If we will flush the buffer before logger finished publishing all current events,
    // (and we don't know when this actually happens!)
    // tests with manually added executor will eventually fail.
    // So idea is simple - we won't flush buffer until there is at least one
    // active printing thread.
    try {
      long startTime = System.currentTimeMillis();

      while (System.currentTimeMillis() - startTime < MAX_WAIT_TIME_MS) {
        if (!isPrinterThreadRunning()) {
          return;
        }
        Thread.sleep(SLEEP_INTERVAL_MS);
      }

      logger.warn("Printer thread did not finish within the timeout period");

    } catch (InterruptedException e) {
      logger.error("Thread was interrupted", e);
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Checks if any Printer thread is running.
   *
   * @return {@code true} if a Printer thread is running, {@code false} otherwise.
   */
  private boolean isPrinterThreadRunning() {
    return Thread.getAllStackTraces().keySet().stream()
      .anyMatch(thread ->
        thread.getName().startsWith(PRINTING_THREAD_PREFIX)
          && thread.getState().equals(Thread.State.RUNNABLE));
  }

  /**
   * Returns all formatted events published by the current logger as a list of strings.
   *
   * @param preserveTrailingSpaces {@code true} to preserve trailing spaces,
   *                               {@code false} otherwise.
   * @return The logger output.
   */
  public List<String> loggerOutput(boolean preserveTrailingSpaces) {
    if (preserveTrailingSpaces) {
      return Arrays.asList(formattedOutput().split(REGEX_LINE_SEPARATOR));
    }
    return Arrays.stream(formattedOutput()
        .split(REGEX_LINE_SEPARATOR))
      .map(String::trim)
      .collect(Collectors.toList());
  }

  /**
   * Returns the first formatted event published by the current logger.
   *
   * @param preserveTrailingSpaces {@code true} to preserve trailing spaces,
   *                               {@code false} otherwise.
   * @return The first formatted event.
   * @throws AssertionError If the output is empty.
   */
  public String firstFormattedEvent(boolean preserveTrailingSpaces) {
    return loggerOutput(preserveTrailingSpaces).stream()
      .filter(it -> !it.isEmpty())
      .findFirst()
      .orElseThrow(()
        -> new AssertionError("Output should not be empty."));
  }

  /**
   * Returns the last formatted event published by the current logger.
   *
   * @param preserveTrailingSpaces {@code true} to preserve trailing spaces,
   *                               {@code false} otherwise.
   * @return The last formatted event.
   */
  public String lastFormattedEvent(boolean preserveTrailingSpaces) {
    return loggerOutput(preserveTrailingSpaces)
      .get(loggerOutput(preserveTrailingSpaces).size() - 1);
  }

  /**
   * Returns a string representation of the TestLogger.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return "TestLogger";
  }

}
