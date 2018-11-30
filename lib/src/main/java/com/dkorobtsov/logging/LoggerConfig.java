package com.dkorobtsov.logging;

import com.dkorobtsov.logging.enums.Level;
import com.dkorobtsov.logging.enums.LoggingFormat;
import java.util.concurrent.Executor;

public class LoggerConfig {

    public final boolean isLoggable;
    public final Level level;
    public final LogWriter logger;
    public final LoggingFormat formatter;
    public final Executor executor;
    public final int maxLineLength;
    public final boolean withThreadInfo;

    LoggerConfig(boolean isLoggable, Level level, LogWriter logger, LoggingFormat formatter,
        Executor executor, int maxLineLength, boolean withThreadInfo) {
        this.isLoggable = isLoggable;
        this.level = level;
        this.logger = logger;
        this.formatter = formatter;
        this.executor = executor;
        this.maxLineLength = maxLineLength;
        this.withThreadInfo = withThreadInfo;
    }

    public static LoggerConfigBuilder builder() {
        return new LoggerConfigBuilder();
    }

    public static class LoggerConfigBuilder {

        private boolean isLoggable = true;
        private Level level = Level.BASIC;
        private LoggingFormat formatter = LoggingFormat.JUL_MESSAGE_ONLY;
        private LogWriter logger = new DefaultLogger(this.formatter);
        private Executor executor;
        private int maxLineLength = 110;
        private boolean withThreadInfo = false;

        LoggerConfigBuilder() {
        }

        public LoggerConfigBuilder loggable(boolean isLoggable) {
            this.isLoggable = isLoggable;
            return this;
        }

        public LoggerConfigBuilder level(Level level) {
            this.level = level;
            return this;
        }

        public LoggerConfigBuilder logger(LogWriter logger) {
            this.logger = logger;
            return this;
        }

        public LoggerConfigBuilder formatter(LoggingFormat formatter) {
            this.formatter = formatter;
            return this;
        }

        public LoggerConfigBuilder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public LoggerConfigBuilder maxLineLength(int maxLineLength) {
            this.maxLineLength = maxLineLength;
            return this;
        }

        public LoggerConfigBuilder withThreadInfo(boolean withThreadInfo) {
            this.withThreadInfo = withThreadInfo;
            return this;
        }

        public LoggerConfig build() {
            return new LoggerConfig(isLoggable, level, logger, formatter, executor, maxLineLength,
                withThreadInfo);
        }

    }

    @Override
    public String toString() {
        final String line = "\n────────────────────────────────────────────────────────────────────────────────────────";
        return line
            + "\n LoggerConfig:"
            + line
            + "\n isLoggable     : " + isLoggable
            + "\n level          : " + level
            + "\n logger         : " + logger
            + "\n formatter      : " + formatter
            + "\n executor       : " + executor
            + "\n maxLineLength  : " + maxLineLength
            + "\n withThreadInfo : " + withThreadInfo
            + line;
    }
}