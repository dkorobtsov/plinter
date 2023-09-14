dependencies {
  implementation(libs.json)
}

tasks.named<Jar>("jar") {
  manifest {
    attributes(
      mapOf(
        "Implementation-Title" to Property.Module.Core.title,
        "Automatic-Module-Name" to Property.Module.Core.name
      )
    )
  }
}
