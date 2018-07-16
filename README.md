LoggingInterceptor - Interceptor for [OkHttp3](https://github.com/square/okhttp) with pretty logger
--------


Basic Usage
-----------
Interceptor should work as is - without any additional parameters.
By default JUL logger will be used with INFO level and minimal format
displaying message only.

```java
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder().build();
    OkHttpClient okHttpClient = new OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build();

    // Interceptor can be used with retrofit
    Retrofit retrofitAdapter = new Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .baseUrl("https://.../")
        .client(okHttpClient)
        .build();
```
Example:
screenshot1.png

Format can be changed to one of the defined templates, for example:
```java
    LoggingInterceptor interceptor1 = new LoggingInterceptor.Builder()
        .loggable(isDebug())
        .level(Level.BASIC)
        .format(LogFormatter.JUL_DATE_LEVEL_MESSAGE)
        .build();
```

Advanced Usage
--------------
Interceptor can be configured to be used with any existing Java logger -
just need to provide own LogWriter implementation.

Simple configuration for Log4j2:
```java
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(new LogWriter() {
          final Logger log = LogManager.getLogger("OkHttpLogger");

          @Override
          public void log(String msg) {
            log.debug(msg);
          }
        })
        .build();
```

Or more sophisticated approach with custom logging pattern.
```java
    LogWriter log4j2Writer = new LogWriter() {
      final String OK_HTTP_LOG_PATTERN = "[OkHTTP] %msg%n";
      final Logger log = LogManager.getLogger("OkHttpLogger");

      {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();

        LoggerConfig loggerConfig = new LoggerConfig("OkHttpLogger", Level.TRACE, false);
        PatternLayout layout = PatternLayout
            .newBuilder()
            .withPattern(OK_HTTP_LOG_PATTERN)
            .build();

        final Appender appender = ConsoleAppender
            .newBuilder()
            .withName("OkHttpConsoleAppender")
            .withLayout(layout)
            .build();

        appender.start();

        loggerConfig.addAppender(appender, Level.TRACE, null);
        config.addLogger("OkHttpLogger", loggerConfig);
        ctx.updateLoggers();
      }

      @Override
      public void log(String msg) {
        log.debug(msg);
      }
    };

    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .logger(log4j2Writer)
        .build();
```
Example:
screenshot2.png

Download
--------

Gradle:
```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}

dependencies {
	compile('com.github.dkorobtsov:LoggingInterceptor:3.0.0') {
        	exclude group: 'org.json', module: 'json'
    	}
}
```

Maven:
```xml
<repository>
   <id>jitpack.io</id>
   <url>https://jitpack.io</url>
</repository>

<dependency>
	    <groupId>com.github.dkorobtsov</groupId>
	    <artifactId>LoggingInterceptor</artifactId>
	    <version>3.0.0</version>
</dependency>
```


Executor
--------
Add executor for allows to perform sequential concurrent print.

Format
------
Predefined JUL logging patterns:
```java
.format(LogFormatter.JUL_DATE_LEVEL_MESSAGE)
           .JUL_DATE_LEVEL_MESSAGE  // [Date][Debug] Message
           .JUL_LEVEL_MESSAGE       // [Debug] Message
           .JUL_DATE_MESSAGE        // [Date] Message
           .JUL_MESSAGE_ONLY        // Message
```
Level
--------

```java
setLevel(Level.BASIC)
	      .NONE     // No logs
	      .BASIC    // Logging url, method, headers and body.
	      .HEADERS  // Logging url, method and headers
	      .BODY     // Logging url, method and body
```	

Platform - [Platform](https://github.com/square/okhttp/blob/master/okhttp/src/main/java/okhttp3/internal/platform/Platform.java)
--------

```java
loggable(true/false) // enable/disable sending logs output.
```

Header - [Recipes](https://github.com/square/okhttp/wiki/Recipes)
--------

```java
addHeader("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9 ") // Adding to request
```
