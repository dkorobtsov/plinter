dependencies {
  api(project(Property.Module.Core.refence))
  implementation(libs.okhttp.interceptor)
}

tasks.named<Jar>("jar") {
  manifest {
    attributes(
      mapOf(
        "Implementation-Title" to Property.Module.OkHttp.title,
        "Automatic-Module-Name" to Property.Module.OkHttp.name
      )
    )
  }
}
