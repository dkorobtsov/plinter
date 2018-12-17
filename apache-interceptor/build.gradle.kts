val archivesBaseName: String by extra { "apache-interceptor" }
val artefactName: String by extra { "Apache Logging Interceptor" }
val artefactDescription: String by extra { "Logging Interceptor for Apache HTTP Client" }

dependencies {
    implementation(project(":interceptor-core"))
    implementation("org.apache.httpcomponents:httpasyncclient:4.1.4")
    implementation("org.apache.httpcomponents:httpclient:4.5.1")
    implementation("org.apache.httpcomponents:httpmime:4.5.6")
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