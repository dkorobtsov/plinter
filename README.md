LoggingInterceptor - Interceptor for [OkHttp3](https://github.com/square/okhttp) with pretty logger
(version for use with Java logging frameworks - jul, log4j, slf4j, logback, log4j2 etc)
--------
[![wercker status](https://app.wercker.com/status/a5684f9e7c75dbf62072dcdd2a96cd90/s/ "wercker status")](https://app.wercker.com/project/byKey/a5684f9e7c75dbf62072dcdd2a96cd90)
![quality gate](https://sonarcloud.io/api/project_badges/measure?project=source%3Alib&metric=alert_status)
![reliability](https://sonarcloud.io/api/project_badges/measure?project=LoggingInterceptor%3Alib&metric=reliability_rating)
![security](https://sonarcloud.io/api/project_badges/measure?project=source%3Alib&metric=security_rating)
![coverage](https://sonarcloud.io/api/project_badges/measure?project=source%3Alib&metric=coverage)


Description
-----------
What is the difference from [original](https://github.com/ihsanbal/LoggingInterceptor) repository?
Personally I find this interceptor very useful for API testing purposes but original implementation
works well only with Android. Had to significantly rewrite original library to use it in pure Java
project with log4j2 based logging. So, comparing to original repository, changes are following:

- removed all Android specific stuff (tags, references to BuildConfig etc)
- removed included Android app (along with existing tests)
- updated Java level 8
- refactored Interceptor to make it work without any additional configuration (just plug and play) :)
- fixed some bugs (mostly output related)
- added option to customize logger output (when default JUL logger is used)
- removed some useless params (like option to select between JUL info or warning severity, request/response tags etc)
- added new tests package (can be helpful to figure out how Interceptor should work)
- added new DefaultLogger implementation (basically just manually configured JUL logger)
- reworked builder (to support those above mentioned changes)

todo:
- correctly handle xml response body

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

<p align="left">
    <img src="https://raw.githubusercontent.com/dkorobtsov/LoggingInterceptor/master/images/screenshot3.png"/>
</p>

Format can be changed to one of the defined templates, for example:
```java
    LoggingInterceptor interceptor = new LoggingInterceptor.Builder()
        .loggable(isDebug())
        .level(Level.BASIC)
        .format(LogFormatter.JUL_DATE_LEVEL_MESSAGE)
        .executor(Executors.newSingleThreadExecutor())
        .build();
```

**Tip:** when logger is in "message only" mode, json response can be copied
from console and converted to POJO with [this](http://www.jsonschema2pojo.org/) service in a matter of seconds.

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
<p align="left">
    <img src="https://raw.githubusercontent.com/dkorobtsov/LoggingInterceptor/master/images/screenshot2.png"/>
</p>

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
	compile('com.github.dkorobtsov:LoggingInterceptor:3.2') {
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
	    <version>3.2</version>
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
           .JUL_FULL                // [Date][Thread][Level] Message
           .JUL_DATE_LEVEL_MESSAGE  // [Date][Level] Message
           .JUL_THREAD_MESSAGE      // [Thread] Message
           .JUL_LEVEL_MESSAGE       // [Level] Message
           .JUL_DATE_MESSAGE        // [Date] Message
           .JUL_MESSAGE_ONLY        // Message
```
Note that given setting works only with default JUL logger.

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
