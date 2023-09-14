dependencies {
  api(project(Dependency.moduleCore))
  implementation(Dependency.okHttpLoggingInterceptor)
}

tasks.named<Jar>("jar") {
  manifest {
    attributes(
      mapOf(
        "Implementation-Title" to Property.implementationTitleOkHttpInterceptor,
        "Automatic-Module-Name" to Property.moduleNameOkHttpInterceptor
      )
    )
  }
}
