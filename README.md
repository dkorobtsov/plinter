# Plinter (Pretty Logging Interceptor)

This library makes working with API's easy and convenient.
Just attach interceptor to your favorite HTTP client and forward all requests and responses to any Java logger (or use
default one).
Simple as that.

--------
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![Qodana](https://github.com/dkorobtsov/plinter/actions/workflows/qodana_code_quality.yml/badge.svg)](https://github.com/dkorobtsov/plinter/actions/workflows/qodana_code_quality.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dkorobtsov_plinter&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=dkorobtsov_plinter)

[![Reliability Rating
](https://sonarcloud.io/api/project_badges/measure?project=dkorobtsov_plinter&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=dkorobtsov_plinter)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=dkorobtsov_plinter&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=dkorobtsov_plinter)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=dkorobtsov_plinter&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=dkorobtsov_plinter)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=dkorobtsov_plinter&metric=coverage)](https://sonarcloud.io/summary/new_code?id=dkorobtsov_plinter)

<p >
    <img src="https://raw.githubusercontent.com/dkorobtsov/plinter/master/images/image1.png" alt="Console Logging Example"/>
</p>

### Features

- No configuration needed, works as is
- Easy to customize if needed
- Several output formats supported out of the box
- Supports most popular HTTP clients
- Supports all Java/Kotlin loggers
- Can be used with Feign or Retrofit
- Works with clients created by Swagger-codegen
- Pretty prints all HTTP request and response details
- Pretty prints JSON, XML and HTML bodies, etc (basically any readable content)

### Clients supported

- [OkHttp](#okhttp)
- [OkHttp 3](#okhttp3)
- [Apache HttpClient](#apache-httpclient)

### Loggers supported

Any Java or Kotlin logger - jul, log4j, slf4j, logback, log4j2 etc

# Quickstart

Interceptor should work as is - without any additional configuration, just need to add appropriate dependency.
By default JUL logger will be used with INFO level and minimal format displaying message only.

**NB. Library is not yet deployed to Maven, at the moment can get sources only by adding [JitPack](https://jitpack.io/)
to repositories list.**

### Gradle (Groovy)

```groovy
 allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

### Gradle (Kotlin DSL)

```kotlin
allprojects {
  repositories {
    maven { setUrl("https://jitpack.io") }
  }
}
```

### Maven:

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

```

## OkHttp

To start using interceptor with OkHttp client add following dependency to classpath:

### Maven:

```xml

<dependency>
    <groupId>io.github.dkorobtsov.plinter</groupId>
    <artifactId>okhttp-interceptor</artifactId>
</dependency>
```

### Gradle:

```kotlin
dependencies {
  implementation("io.github.dkorobtsov.plinter:okhttp-interceptor:$LATEST_VERSION")
}
```

Check [releases](https://github.com/dkorobtsov/plinter/releases) for latest interceptor version.

Basic usage example:

```
    OkHttpLoggingInterceptor interceptor = new OkHttpLoggingInterceptor(LoggerConfig.builder().build());
    
    OkHttpClient okHttpClient = new OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build();
```

**Tip:** Can be used with REST client created by swagger-codegen using `okhttp-gson` template.

## OkHttp3

To start using interceptor with OkHttp3 client add following dependency to classpath:

### Maven:

```xml

<dependency>
    <groupId>io.github.dkorobtsov.plinter</groupId>
    <artifactId>okhttp3-interceptor</artifactId>
    <version>$LATEST_VERSION</version>
</dependency>
```

### Gradle:

```kotlin
dependencies {
  implementation("io.github.dkorobtsov.plinter:okhttp3-interceptor:$LATEST_VERSION")
}
```

Check [releases](https://github.com/dkorobtsov/plinter/releases) for latest interceptor version.

Basic usage example:

```
    OkHttp3LoggingInterceptor interceptor = new OkHttp3LoggingInterceptor(LoggerConfig.builder().build());

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build();
```

## Apache HttpClient

To start using interceptor with Apache Http client add following dependency to classpath:

### Maven:

```xml

<dependency>
    <groupId>io.github.dkorobtsov.plinter</groupId>
    <artifactId>apache-interceptor</artifactId>
    <version>$LATEST_VERSION</version>
</dependency>
```

### Gradle:

```kotlin
dependencies {
  implementation("io.github.dkorobtsov.plinter:apache-interceptor:$LATEST_VERSION")
}
```

Check [releases](https://github.com/dkorobtsov/plinter/releases) for latest interceptor version.

Basic usage example:

```
    ApacheRequestInterceptor requestInterceptor = new ApacheRequestInterceptor(LoggerConfig.builder().build());
    ApacheResponseInterceptor responseInterceptor = new ApacheResponseInterceptor(LoggerConfig.builder().build());
    
    CloseableHttpClient client = HttpClientBuilder
        .create()
        .addInterceptorFirst(requestInterceptor)
        .addInterceptorFirst(responseInterceptor)
        .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
        .build();   
```

# Advanced Configuration

Interceptor can be used with any existing Java logger -
just need to provide your own LogWriter implementation.

Simple configuration for Log4j2 with printing in separate thread:

```
    OkHttp3LoggingInterceptor interceptor = new OkHttp3LoggingInterceptor(
        LoggerConfig.builder()
            .logger(new LogWriter() {
              final Logger logger = LogManager.getLogger("HttpLogger");

              @Override
              public void log(String msg) {
                logger.debug(msg);
              }
            })
            .withThreadInfo(true)
            .loggable(true)
            .maxLineLength(180)
            .level(Level.BASIC)
            .executor(Executors.newSingleThreadExecutor(r -> new Thread(r, "HttpPrinter")))
            .build());

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build();

    // Can be used with Retrofit or Feign
    Retrofit retrofitAdapter = new Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .baseUrl("https://.../")
        .client(okHttpClient)
        .build();
```

Or more sophisticated approach with custom logging pattern.

```
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

    OkHttp3LoggingInterceptor interceptor = new OkHttp3LoggingInterceptor(LoggerConfig.builder()
        .logger(log4j2Writer)
        .build());
```

# Logger Configuration

Interceptor can be configured by providing your own LoggerConfig.

Check supported options descriptions below:

### Format

Default logger's format can be easily changed using predefined JUL logging patterns:

```
.format(LogFormatter.JUL_DATE_LEVEL_MESSAGE)
        .JUL_FULL                // [Date][Thread][Level] Message
        .JUL_DATE_LEVEL_MESSAGE  // [Date][Level] Message
        .JUL_THREAD_MESSAGE      // [Thread] Message
        .JUL_LEVEL_MESSAGE       // [Level] Message
        .JUL_DATE_MESSAGE        // [Date] Message
        .JUL_MESSAGE_ONLY        // Message
```

Note that given setting works only with default JUL logger, if logger is provided manually, built-in formatters will be
ignored.

**Tip:** when logger is in "message only" mode, json response can be copied
from console and converted to POJO with [this](http://www.jsonschema2pojo.org/) service in a matter of seconds.

### Executor

Add executor that allows to perform sequential concurrent print.

```
        .executor(Executors.newSingleThreadExecutor(r -> new Thread(r, "HttpPrinter")))
```

### Line Length

If needed, max output length can be modified. Default value: 110. Valid values: 80-180.

```
        .maxLineLength(160) 
```

### Thread Info

If enabled, current thread name will be present in intercepted event's header.

```
        .withThreadInfo(true/false) 
```

### Level

```
.setLevel(Level.BASIC)
	      .NONE       // No logs
	      .BASIC      // Logging url, method, headers and body.
	      .HEADERS    // Logging url, method and headers
	      .BODY       // Logging url, method and body
```

### Loggable

Enable or disable interceptor. If set to false, interceptor will ignore all traffic.

```
        .loggable(true/false) 
```

## Credits

This library was heavily inspired and initially forked from
[LoggingInterceptor for Android](https://github.com/ihsanbal/LoggingInterceptor)
by [Ihsan Bal](https://github.com/ihsanbal)
and completely rewritten by [Dmitri Korobtsov](https://github.com/dkorobtsov) to
provide more flexibility and support for native Java loggers. Support for OkHttp2 and Apache clients introduced
by [Andrew Zakordonets](https://github.com/azakordonets).
Part of internal requests/responses handling logic based on [OkHttp3](https://github.com/square/okhttp) client's code
with slight modifications to remove external dependencies.

-----

**Find this library helpful? Show some support:**

[![GitHub followers](https://img.shields.io/github/followers/dkorobtsov.svg?style=social&label=Follow)](https://github.com/dkorobtsov)
[![GitHub forks](https://img.shields.io/github/forks/dkorobtsov/Plinter.svg?style=social&label=Fork)](https://github.com/dkorobtsov/plinter/fork)
[![GitHub stars](https://img.shields.io/github/stars/dkorobtsov/Plinter.svg?style=social&label=Star)](https://github.com/dkorobtsov/plinter)
[![Twitter Follow](https://img.shields.io/twitter/follow/dkorobtsov.svg?style=social&label=Follow)](https://twitter.com/dkorobtsov)

-----
