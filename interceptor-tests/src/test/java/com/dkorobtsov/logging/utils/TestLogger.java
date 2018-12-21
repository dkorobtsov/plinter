package com.dkorobtsov.logging.utils;

import static com.dkorobtsov.logging.utils.TestUtil.PRINTING_THREAD_PREFIX;
import static java.lang.Thread.State.RUNNABLE;

import com.dkorobtsov.logging.LogWriter;
import com.dkorobtsov.logging.LoggingFormat;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.stream.Collectors;

/**
 * DefaultLogger double with additional methods for testing purposes. All published events are
 * registered and can be retrieved for validation.
 */
public class TestLogger implements LogWriter {

  private static final org.apache.logging.log4j.Logger logger
      = org.apache.logging.log4j.LogManager.getLogger(TestLogger.class.getName());

  private static final String REGEX_LINE_SEPARATOR = "\r?\n";
  private final List<String> events = new ArrayList<>(Collections.emptyList());
  private StreamHandler logOutputHandler;
  private OutputStream logOut;
  private Logger testLogger = Logger.getLogger(TestLogger.class.getName());

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

  @Override
  public void log(String msg) {
    testLogger.info(msg);
    events.add(msg);
  }

  /**
   * @return Returns raw messages (in case we want to check content only and  don't care about
   * format)
   */
  private List<String> rawMessages() {
    return events;
  }

  /**
   * @return Returns first formatted event published by current logger
   */
  public String firstRawEvent() {
    return rawMessages().get(0).trim();
  }

  /**
   * @return Returns last formatted event published by current logger
   */
  String lastRawEvent() {
    return rawMessages().get(rawMessages().size() - 1).trim();
  }

  /**
   * @return Returns all formatted events published by current logger as String
   */
  public String formattedOutput() {
    waitForPrinterThreadToFinish();
    logOutputHandler.flush();
    logOutputHandler.close();
    return logOut.toString();
  }

  /**
   * Method waits up to 30 ms until any active Printer thread becomes idle.
   */
  private void waitForPrinterThreadToFinish() {
    try {
      // Hack for cases when printer is working in separate thread
      // If we will flush the buffer before logger finished publishing all current events,
      // (and we don't know when this actually happens!)
      // tests with manually added executor will eventually fail.
      // So idea is simple - we won't flush buffer until there is at least one
      // active printing thread.
      final Optional<Thread> printerThread = Thread.getAllStackTraces().keySet()
          .stream()
          .filter(it -> it.getName().startsWith(PRINTING_THREAD_PREFIX))
          .filter(it -> it.getState().equals(RUNNABLE))
          .findFirst();

      // obviously we don't want to fall into to endless loop
      int maxThreadSleep = 30;
      int i = 0;
      while (printerThread.isPresent()
          && printerThread.get().getState().equals(RUNNABLE) && i < maxThreadSleep) {
        Thread.sleep(2);
        i++;
      }
    } catch (InterruptedException e) {
      logger.error(e);
    }
  }

  /**
   * @return Returns all formatted events published by current logger as String array
   */
  public List<String> loggerOutput(boolean preserveTrailingSpaces) {
    if (preserveTrailingSpaces) {
      return Arrays.asList(formattedOutput().split(REGEX_LINE_SEPARATOR));
    }
    return Arrays.stream(formattedOutput()
        .split(REGEX_LINE_SEPARATOR))
        .map(String::trim).collect(Collectors.toList());
  }

  /**
   * @return Returns first formatted event published by current logger
   */
  public String firstFormattedEvent(boolean preserveTrailingSpaces) {
    return loggerOutput(preserveTrailingSpaces).get(0);
  }

  /**
   * @return Returns last formatted event published by current logger
   */
  public String lastFormattedEvent(boolean preserveTrailingSpaces) {
    return loggerOutput(preserveTrailingSpaces)
        .get(loggerOutput(preserveTrailingSpaces).size() - 1);
  }

  @Override
  public String toString() {
    return "TestLogger";
  }

}
