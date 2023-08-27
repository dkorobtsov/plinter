@Suppress("MemberVisibilityCanBePrivate", "unused")
object Dependency {

    const val moduleCore = ":interceptor-core"
    const val moduleApacheInterceptor = ":apache-interceptor"
    const val moduleOkHttpInterceptor = ":okhttp-interceptor"
    const val moduleOkHttp3Interceptor = ":okhttp3-interceptor"

    // Versions
    const val okioVersion = "2.1.0"
    const val moshiVersion = "1.8.0"
    const val okHttpVersion = "2.7.5"
    const val sonarcubeVersion = "2.6.2"
    const val testLoggerVersion = "1.6.0"
    const val retrofitVersion = "2.5.0"

    // Plugins
    const val sonarcubeId = "org.sonarqube"
    const val testLoggerId = "com.adarshr.test-logger"
    const val testLoggerPlugin = "com.adarshr:gradle-test-logger-plugin:$testLoggerVersion"

    // Libraries
    const val json = "org.json:json:20180130"
    const val okio = "com.squareup.okio:okio:$okioVersion"

    const val retrofit = "com.squareup.retrofit2:retrofit:$retrofitVersion"
    const val retrofitConverterJson = "com.squareup.retrofit2:converter-moshi:$retrofitVersion"
    const val retrofitConverterScalars = "com.squareup.retrofit2:converter-scalars:$retrofitVersion"

    const val retrofitConverterXml = "com.squareup.retrofit2:converter-jaxb:$retrofitVersion"
    const val moshi = "com.squareup.moshi:moshi:$moshiVersion"
    const val okHttp = "com.squareup.okhttp:okhttp:$okHttpVersion"

    // Test Libraries
    const val moshiAdapters = "com.squareup.moshi:moshi-adapters:$moshiVersion"
    const val apacheMime = "org.apache.httpcomponents:httpmime:4.5.6"
    const val apacheClient = "org.apache.httpcomponents:httpclient:4.5.1"
    const val apacheAsyncClient = "org.apache.httpcomponents:httpasyncclient:4.1.4"
    const val okHttp3LoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:3.9.1"
    const val okHttpLoggingInterceptor = "com.squareup.okhttp:logging-interceptor:2.7.5"
    const val mockWebServer = "com.squareup.okhttp3:mockwebserver:4.11.0"
    const val log4j2_code = "org.apache.logging.log4j:log4j-core:2.11.0"
    const val junitParams = "pl.pragmatists:JUnitParams:1.1.1"
    const val assertJ = "org.assertj:assertj-core:3.11.1"
    const val junit = "junit:junit:4.12"
    const val sparcCore = "com.sparkjava:spark-core:2.8.0"

}