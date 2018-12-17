val archivesBaseName: String by extra { "okhttp3-interceptor" }
val artefactName: String by extra { "OkHttp3 Logging Interceptor" }
val artefactDescription: String by extra { "Logging Interceptor for OkHttp3 Client" }

dependencies {
    implementation(project(":interceptor-core"))
    implementation("com.squareup.okhttp3:logging-interceptor:3.9.1")
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