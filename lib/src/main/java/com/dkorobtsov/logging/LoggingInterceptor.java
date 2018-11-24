package com.dkorobtsov.logging;

import com.dkorobtsov.logging.interceptors.ApacheHttpRequestInterceptor;
import com.dkorobtsov.logging.interceptors.ApacheHttpResponseInterceptor;
import com.dkorobtsov.logging.interceptors.OkHttp3LoggingInterceptor;
import com.dkorobtsov.logging.interceptors.OkHttpLoggingInterceptor;
import java.util.concurrent.Executor;

public class LoggingInterceptor {

    private LoggingInterceptor() {
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings({"unused", "SameParameterValue"})
    public static class Builder {

        private boolean isLoggable = true;
        private int maxLineLength = 110;
        private Level level = Level.BASIC;
        private LogWriter logger;
        private LogFormatter formatter;
        private Executor executor;

        public Builder() {
            formatter = LogFormatter.JUL_MESSAGE_ONLY;
        }

        /**
         * @param logger use this method to provide your logging interface implementation.
         *
         * Example:
         * <pre>
         *         Okhttp3LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
         *         .logger(new LogWriter() {
         *           final Logger log = LogManager.getLogger("OkHttpLogger");
         *
         *           @Override
         *           public void log(String msg) {
         *             log.debug(msg);
         *           }
         *         })
         *         .buildForOkhttp3();
         * </pre>
         */
        public Builder logger(LogWriter logger) {
            this.logger = logger;
            return this;
        }

        /**
         * @param format set format for default Java Utility Logger
         *
         * (will be ignored in case custom logger is used)
         */
        public Builder format(LogFormatter format) {
            this.formatter = format;
            return this;
        }

        LogFormatter getFormat() {
            return formatter;
        }

        /**
         * @param loggable specifies if logger is enabled
         */
        public Builder loggable(boolean loggable) {
            this.isLoggable = loggable;
            return this;
        }

        /**
         * @param level set log level
         */
        public Builder level(Level level) {
            this.level = level;
            return this;
        }

        /**
         * @param length specifies max line length when printing request/response body
         *
         * Min value: 10, Max value: 500, Default: 110
         */
        public Builder maxLineLength(int length) {
            if (length < 10 || length > 500) {
                throw new IllegalArgumentException(
                    "Invalid line length. Should be longer then 10 and shorter then 500.");
            } else {
                this.maxLineLength = length;
            }
            return this;
        }

        /**
         * @param executor manual executor override for printing
         */
        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public OkHttpLoggingInterceptor buildForOkhttp() {
            return new OkHttpLoggingInterceptor(loggerConfig());
        }

        public OkHttp3LoggingInterceptor buildForOkhttp3() {
            return new OkHttp3LoggingInterceptor(loggerConfig());
        }

        public ApacheHttpRequestInterceptor buildForApacheHttpClientRequest() {
            return new ApacheHttpRequestInterceptor(loggerConfig());
        }

        public ApacheHttpResponseInterceptor buildFordApacheHttpClientResponse() {
            return new ApacheHttpResponseInterceptor(loggerConfig());
        }

        private LoggerConfig loggerConfig() {
            return LoggerConfig.builder()
                .executor(this.executor)
                .formatter(this.formatter)
                .logger(this.logger)
                .loggable(this.isLoggable)
                .level(this.level)
                .maxLineLength(this.maxLineLength)
                .build();
        }
    }
}
