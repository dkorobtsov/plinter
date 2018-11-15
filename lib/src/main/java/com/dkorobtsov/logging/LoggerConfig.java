package com.dkorobtsov.logging;

import java.util.concurrent.Executor;

public class LoggerConfig {

    public final boolean isDebug;
    public final Level level;
    public final LogWriter logger;
    public final LogFormatter formatter;
    public final Executor executor;
    public final int maxLineLength;

    LoggerConfig(boolean isDebug, Level level, LogWriter logger, LogFormatter formatter,
        Executor executor, int maxLineLength) {
        this.isDebug = isDebug;
        this.level = level;
        this.logger = logger;
        this.formatter = formatter;
        this.executor = executor;
        this.maxLineLength = maxLineLength;
    }

    public static LoggerConfigBuilder builder() {
        return new LoggerConfigBuilder();
    }

    public static class LoggerConfigBuilder {

        private boolean isDebug;
        private Level level;
        private LogWriter logger;
        private LogFormatter formatter;
        private Executor executor;
        private int maxLineLength;

        LoggerConfigBuilder() {
        }

        public LoggerConfigBuilder loggable(boolean isDebug) {
            this.isDebug = isDebug;
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

        public LoggerConfigBuilder formatter(LogFormatter formatter) {
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

        public LoggerConfig build() {
            return new LoggerConfig(isDebug, level, logger, formatter, executor,
                maxLineLength);
        }

    }
}