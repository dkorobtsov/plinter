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

    public static loggerConfigBuilder builder() {
        return new loggerConfigBuilder();
    }

    public String toString() {
        return "loggerConfig(loggable=" + this.isDebug + ", level=" + this.level + ", logger="
            + this.logger + ", formatter=" + this.formatter + ", executor=" + this.executor
            + ", maxLineLength=" + this.maxLineLength + ")";
    }

    public static class loggerConfigBuilder {

        private boolean isDebug;
        private Level level;
        private LogWriter logger;
        private LogFormatter formatter;
        private Executor executor;
        private int maxLineLength;

        loggerConfigBuilder() {
        }

        public LoggerConfig.loggerConfigBuilder loggable(boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        public LoggerConfig.loggerConfigBuilder level(Level level) {
            this.level = level;
            return this;
        }

        public LoggerConfig.loggerConfigBuilder logger(LogWriter logger) {
            this.logger = logger;
            return this;
        }

        public LoggerConfig.loggerConfigBuilder formatter(LogFormatter formatter) {
            this.formatter = formatter;
            return this;
        }

        public LoggerConfig.loggerConfigBuilder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public LoggerConfig.loggerConfigBuilder maxLineLength(int maxLineLength) {
            this.maxLineLength = maxLineLength;
            return this;
        }

        public LoggerConfig build() {
            return new LoggerConfig(isDebug, level, logger, formatter, executor,
                maxLineLength);
        }

        public String toString() {
            return "loggerConfig.loggerConfigBuilder(loggable=" + this.isDebug
                + ", level=" + this.level
                + ", logger=" + this.logger
                + ", formatter=" + this.formatter
                + ", executor=" + this.executor
                + ", maxLineLength=" + this.maxLineLength + ")";
        }
    }
}