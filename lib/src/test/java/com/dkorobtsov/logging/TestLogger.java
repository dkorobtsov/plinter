package com.dkorobtsov.logging;

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

/**
 * DefaultLogger double with additional methods for testing purposes. All published events are
 * registered and can be retrieved for validation.
 */
public class TestLogger implements LogWriter {

    private final List<String> events = new ArrayList<>(Collections.emptyList());
    private StreamHandler logOutputHandler;
    private OutputStream logOut;
    private Logger testLogger = Logger.getLogger("TestLogger");

    TestLogger(LogFormatter logFormatter) {
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
        logOutputHandler.flush();
        return logOut.toString();
    }

    /**
     * @return Returns all formatted events published by current logger as String array
     */
    String[] outputAsArray() {
        return formattedOutput().split("\r?\n");
    }

    /**
     * @return Returns first formatted event published by current logger
     */
    String firstFormattedEvent() {
        return outputAsArray()[0].trim();
    }

    /**
     * @return Returns last formatted event published by current logger
     */
    String lastFormattedEvent() {
        return outputAsArray()[outputAsArray().length - 1].trim();
    }

}
