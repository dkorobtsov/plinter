val archivesBaseName: String by extra { "okhttp-interceptor" }
val artefactName: String by extra { "OkHttp Logging Interceptor" }
val artefactDescription: String by extra { "Logging Interceptor for OkHttp Client" }

dependencies {
    implementation(project(":interceptor-core"))
    implementation("com.squareup.okhttp:logging-interceptor:2.7.5")
    implementation("com.dkorobtsov.logging:interceptor-core:5.0-SNAPSHOT") {
        setChanging(true)
    }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(mapOf(
                "Implementation-Title" to artefactName,
                "Implementation-Description" to artefactDescription,
                "Automatic-Module-Name" to "com.dkorobtsov.logging$archivesBaseName"
        ))
    }
}