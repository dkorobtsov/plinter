val archivesBaseName: String by extra { "apache-interceptor" }
val artefactName: String by extra { "Apache Logging Interceptor" }

dependencies {
    api(project(":interceptor-core"))
    implementation("org.apache.httpcomponents:httpasyncclient:4.1.4")
    implementation("org.apache.httpcomponents:httpclient:4.5.1")
    implementation("org.apache.httpcomponents:httpmime:4.5.6")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(mapOf(
                "Implementation-Title" to artefactName,
                "Automatic-Module-Name" to "${rootProject.extra["projectGroup"]}.$archivesBaseName"
        ))
    }
}