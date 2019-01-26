val archivesBaseName: String by extra { "okhttp3-interceptor" }
val artefactName: String by extra { "OkHttp3 Logging Interceptor" }

dependencies {
    api(project(":interceptor-core"))
    implementation("com.squareup.okhttp3:logging-interceptor:3.9.1")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(mapOf(
                "Implementation-Title" to artefactName,
                "Automatic-Module-Name" to "${rootProject.extra["projectGroup"]}.$archivesBaseName"
        ))
    }
}