dependencies {
  api(project(Dependency.moduleCore))
  implementation(Dependency.okHttp3LoggingInterceptor)
}

tasks.named<Jar>("jar") {
  manifest {
    attributes(
      mapOf(
        "Implementation-Title" to Property.implementationTitleOkHttp3Interceptor,
        "Automatic-Module-Name" to Property.moduleNameOkHttp3Interceptor
      )
    )
  }
}
