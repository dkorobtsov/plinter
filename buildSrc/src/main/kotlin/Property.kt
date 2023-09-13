@Suppress("MemberVisibilityCanBePrivate", "unused")
object Property {

  const val projectDescription = "HTTP traffic Pretty Logging Interceptor"
  const val projectUrl = "https://github.com/dkorobtsov/plinter"
  const val projectConnection = "scm:${projectUrl}.git"
  const val projectDevConnection = "scm:git@github.com:dkorobtsov/plinter.git"
  const val projectGroup = "io.github.dkorobtsov.plinter"
  const val projectName = "Pretty Logging Interceptor"
  const val projectVersion = "5.2.2-SNAPSHOT"
  const val archivesBaseName = "plinter"

  const val sonarProjectKey = "dkorobtsov_plinter"
  const val sonarHost = "https://sonarcloud.io"
  const val sonarOrganization = "dkorobtsov"

  const val moduleNameInterceptorCore = "$projectGroup.core"
  const val moduleNameInterceptorTests = "$projectGroup.tests"
  const val moduleNameApacheInterceptor = "$projectGroup.apache"
  const val moduleNameOkHttpInterceptor = "$projectGroup.okhttp"
  const val moduleNameOkHttp3Interceptor = "$projectGroup.okhttp3"

  const val implementationTitleInterceptorCore = "Logging Interceptor Core"
  const val implementationTitleInterceptorTests = "Logging Interceptor Tests"
  const val implementationTitleApacheInterceptor = "Apache Logging Interceptor"
  const val implementationTitleOkHttpInterceptor = "OkHttp Logging Interceptor"
  const val implementationTitleOkHttp3Interceptor = "OkHttp3 Logging Interceptor"

}
