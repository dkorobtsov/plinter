@Suppress("MemberVisibilityCanBePrivate", "unused")
object Property {

  object Project {
    const val description = "HTTP traffic Pretty Logging Interceptor"
    const val group = "io.github.dkorobtsov.plinter"
    const val name = "Pretty Logging Interceptor"
    const val archivesName = "plinter"
    const val version = "5.2.2-SNAPSHOT"
    const val url = "https://github.com/dkorobtsov/plinter"
    const val connection = "scm:${url}.git"
    const val devConnection = "scm:git@github.com:dkorobtsov/plinter.git"
  }

  object Sonar {
    val projectKey = "dkorobtsov_plinter"
    val host = "https://sonarcloud.io"
    val org = "dkorobtsov"

    object Exclusions {
      val coverage = arrayOf(
        "**/CacheControl.java",
        "**/HttpHeaders.java",
        "**/InterceptedHeaders.java",
        "**/HttpMethod.java",
        "**/InterceptedMediaType.java",
        "**/InterceptedRequest.java",
        "**/InterceptedRequestBody.java",
        "**/InterceptedResponseBody.java",
        "**/Protocol.java",
        "**/Util.java"
      ).joinToString(separator = ", ")

      val duplication = arrayOf(
        "**/okhttp/**",
        "**/okhttp3/**"
      ).joinToString(separator = ", ")

      val scan = arrayOf(
        "**/CacheControl.java",
        "**/InterceptedRequestBody.java",
        "**/InterceptedHeaders.java",
        "**/InterceptedMediaType.java",
        "**/Protocol.java",
        "**/Util.java",
        "**/build.gradle.kts"
      ).joinToString(separator = ", ")

    }
  }

  object Module {
    object Core {
      const val title = "Logging Interceptor Core"
      const val name = "${Project.group}.core"
      const val refence = ":interceptor-core"
    }

    object Tests {
      const val title = "Logging Interceptor Tests"
      const val name = "${Project.group}.tests"
      const val refence = ":interceptor-tests"
    }

    object Apache {
      const val title = "Apache Logging Interceptor"
      const val name = "${Project.group}.apache"
      const val refence = ":apache-interceptor"
    }

    object OkHttp {
      const val title = "OkHttp Logging Interceptor"
      const val name = "${Project.group}.okhttp"
      const val refence = ":okhttp-interceptor"
    }

    object OkHttp3 {
      const val title = "OkHttp3 Logging Interceptor"
      const val name = "${Project.group}.okhttp3"
      const val refence = ":okhttp3-interceptor"
    }
  }
}
