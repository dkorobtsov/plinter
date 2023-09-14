dependencies {
  api(project(Property.Module.Core.refence))
  implementation(libs.apache.mime)
  implementation(libs.apache.client)
  implementation(libs.apache.async.client)
}

tasks.named<Jar>("jar") {
  manifest {
    attributes(
      mapOf(
        "Implementation-Title" to Property.Module.Apache.title,
        "Automatic-Module-Name" to Property.Module.Apache.name
      )
    )
  }
}
