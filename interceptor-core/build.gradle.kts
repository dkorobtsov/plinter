dependencies {
  implementation(Dependency.json)
}

tasks.named<Jar>("jar") {
  manifest {
    attributes(
      mapOf(
        "Implementation-Title" to Property.implementationTitleInterceptorCore,
        "Automatic-Module-Name" to Property.moduleNameInterceptorCore
      )
    )
  }
}
