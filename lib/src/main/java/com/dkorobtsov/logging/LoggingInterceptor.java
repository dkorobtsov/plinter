package com.dkorobtsov.logging;

import com.dkorobtsov.logging.interceptors.ApacheHttpRequestInterceptor;
import com.dkorobtsov.logging.interceptors.ApacheHttpResponseInterceptor;
import com.dkorobtsov.logging.interceptors.Okhttp3LoggingInterceptor;
import com.dkorobtsov.logging.interceptors.OkhttpLoggingInterceptor;

import java.util.concurrent.Executor;

public class LoggingInterceptor {
    @SuppressWarnings( {"unused", "SameParameterValue"})
    public static class Builder {

        public boolean isDebug() {
            return isDebug;
        }

        private boolean isDebug = true;
        private Level level = Level.BASIC;
        private LogWriter logger;
        private LogFormatter formatter;

        LogFormatter getFormatter() {
            return formatter;
        }

        private Executor executor;

        public Builder() {
            formatter = LogFormatter.JUL_MESSAGE_ONLY;
        }

        /**
         * @param logger manual logging interface
         * @return Builder
         * @see LogWriter
         */
        Builder logger(LogWriter logger) {
            this.logger = logger;
            return this;
        }

        LogWriter getLogger() {
            if (logger == null) {
                logger = new DefaultLogger(formatter);
            }
            return logger;
        }

        /**
         * @param format set Java Utility Logger format (will be ignored in case custom logger is used)
         * @return Builder
         */
        public Builder format(LogFormatter format) {
            this.formatter = format;
            return this;
        }

        /**
         * @param isDebug set can sending log output
         * @return Builder
         */
        Builder loggable(boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        /**
         * @param level set log level
         * @return Builder
         * @see Level
         */
        public Builder level(Level level) {
            this.level = level;
            return this;
        }

        public Level getLevel() {
            return level;
        }

        /**
         * @param executor manual executor for printing
         * @return Builder
         * @see LogWriter
         */
        Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        Executor getExecutor() {
            return executor;
        }


        OkhttpLoggingInterceptor buildForOkhttp() {
            return new OkhttpLoggingInterceptor(this);
        }

        Okhttp3LoggingInterceptor buildForOkhttp3() {
            return new Okhttp3LoggingInterceptor(this);
        }

        ApacheHttpRequestInterceptor buildForApacheHttpClientRequest() {
            return new ApacheHttpRequestInterceptor(this);
        }

        ApacheHttpResponseInterceptor builFordApacheHttpClientResponse() {
            return new ApacheHttpResponseInterceptor(this);
        }
    }
}
