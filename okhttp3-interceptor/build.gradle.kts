dependencies {
  api(project(Property.Module.Core.refence))
  implementation(libs.okhttp3.interceptor)
}

tasks.named<Jar>("jar") {
  manifest {
    attributes(
      mapOf(
        "Implementation-Title" to Property.Module.OkHttp3.title,
        "Automatic-Module-Name" to Property.Module.OkHttp3.name
      )
    )
  }
}
