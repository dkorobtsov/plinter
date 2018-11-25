package com.dkorobtsov.logging;

import com.dkorobtsov.logging.enums.LoggingFormat;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.stream.Collectors;

/**
 * DefaultLogger double with additional methods for testing purposes. All published events are
 * registered and can be retrieved for validation.
 */
public class TestLogger implements LogWriter {

    private static final Logger logger = Logger.getLogger(TestLogger.class.getName());

    private final List<String> events = new ArrayList<>(Collections.emptyList());
    private StreamHandler logOutputHandler;
    private OutputStream logOut;
    private Logger testLogger = Logger.getLogger("TestLogger");

    TestLogger(LoggingFormat logFormatter) {
        testLogger.setUseParentHandlers(false);

        // Removing existing handlers for new instance
        Arrays.stream(testLogger.getHandlers()).forEach(testLogger::removeHandler);

        // Configuring output to console
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(logFormatter.formatter);
        testLogger.addHandler(consoleHandler);

        // Configuring output to stream
        logOut = new ByteArrayOutputStream();
        logOutputHandler = new StreamHandler(logOut, logFormatter.formatter);

        testLogger.addHandler(logOutputHandler);
    }

    @Override
    public void log(String msg) {
        testLogger.log(Level.INFO, msg);
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
    String firstRawEvent() {
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
    String formattedOutput() {
        try {
            // Don't like this solution, but without this wait tests verifying
            // logger output with manually added executor are randomly failing
            // (part of output is missing). Suppose root cause is that we are
            // flashing output before all lines get in buffer
            // NB: random failures occur when value < 25
            Thread.sleep(30);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        logOutputHandler.flush();
        return logOut.toString();
    }

    /**
     * @return Returns all formatted events published by current logger as String array
     */
    List<String> loggerOutput(boolean preserveTrailingSpaces) {
        if (preserveTrailingSpaces) {
            return Arrays.asList(formattedOutput().split("\r?\n"));
        }
        return Arrays.stream(formattedOutput()
            .split("\r?\n"))
            .map(String::trim).collect(Collectors.toList());
    }

    /**
     * @return Returns first formatted event published by current logger
     */
    String firstFormattedEvent(boolean preserveTrailingSpaces) {
        return loggerOutput(preserveTrailingSpaces).get(0);
    }

    /**
     * @return Returns last formatted event published by current logger
     */
    String lastFormattedEvent(boolean preserveTrailingSpaces) {
        return loggerOutput(preserveTrailingSpaces)
            .get(loggerOutput(preserveTrailingSpaces).size() - 1);
    }

}
