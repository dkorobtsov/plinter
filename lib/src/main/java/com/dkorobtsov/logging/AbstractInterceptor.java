package com.dkorobtsov.logging;

import com.dkorobtsov.logging.enums.Level;

public abstract class AbstractInterceptor {

    protected LoggerConfig loggerConfig;

    public LoggerConfig loggerConfig() {
        return this.loggerConfig;
    }

    protected boolean skipLogging() {
        return !loggerConfig.isLoggable || loggerConfig.level == Level.NONE;
    }

}
