package com.dkorobtsov.logging;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultLogger implements LogWriter {

    private final Logger logger = Logger.getLogger(DefaultLogger.class.getName());

    DefaultLogger(LoggingFormat logFormatter) {
        logger.setUseParentHandlers(false);

        // Sometimes handlers are duplicated, here we are making sure,
        // that only cone console handler will exist
        Arrays.stream(logger.getHandlers()).forEach(logger::removeHandler);

        final ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(logFormatter.formatter);
        logger.addHandler(handler);
    }

    @Override
    public void log(String msg) {
        logger.log(Level.INFO, msg);
    }

    @Override
    public String toString() {
        return "DefaultLogger";
    }

}
