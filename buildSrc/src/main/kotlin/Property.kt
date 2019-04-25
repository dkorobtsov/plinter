@Suppress("MemberVisibilityCanBePrivate", "unused")
object Property {

    const val projectDescription = "HTTP traffic Pretty Logging Interceptor"
    const val projectUrl = "https://github.com/dkorobtsov/plinter"
    const val projectGroup = "io.github.dkorobtsov.plinter"
    const val projectName = "Pretty Logging Interceptor"
    const val projectVersion = "5.2.1-SNAPSHOT"
    const val archivesBaseName = "plinter"

    const val sonarProjectKey = "Plinter"
    const val sonarHost = "https://sonarcloud.io"
    const val sonarOrganization = "dkorobtsov-github"


    const val moduleNameInterceptorCore = "$projectGroup.interceptor-core"
    const val moduleNameInterceptorTests = "$projectGroup.interceptor-tests"
    const val moduleNameApacheInterceptor = "$projectGroup.apache-interceptor"
    const val moduleNameOkHttpInterceptor = "$projectGroup.okhttp-interceptor"
    const val moduleNameOkHttp3Interceptor = "$projectGroup.okhttp3-interceptor"

    const val implementationTitleInterceptorCore = "Logging Interceptor Core"
    const val implementationTitleInterceptorTests = "Logging Interceptor Tests"
    const val implementationTitleApacheInterceptor = "Apache Logging Interceptor"
    const val implementationTitleOkHttpInterceptor = "OkHttp Logging Interceptor"
    const val implementationTitleOkHttp3Interceptor = "OkHttp3 Logging Interceptor"

}