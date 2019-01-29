val archivesBaseName: String by extra { "okhttp-interceptor" }
val artefactName: String by extra { "OkHttp Logging Interceptor" }

dependencies {
    api(project(":interceptor-core"))
    implementation("com.squareup.okhttp:logging-interceptor:2.7.5")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(mapOf(
                "Implementation-Title" to artefactName,
                "Automatic-Module-Name" to "${rootProject.extra["projectGroup"]}.$archivesBaseName"
        ))
    }
}