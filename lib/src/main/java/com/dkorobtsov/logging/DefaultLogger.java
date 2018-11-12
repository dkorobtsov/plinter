package com.dkorobtsov.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultLogger implements LogWriter {

    private static final Logger logger = Logger.getLogger("DefaultLogger");

    DefaultLogger(LogFormatter logFormatter) {
        logger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(logFormatter.formatter);
        logger.addHandler(handler);
    }

    @Override
    public void log(String msg) {
        logger.log(Level.INFO, msg);
    }

}
