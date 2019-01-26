val archivesBaseName: String by extra { "interceptor-core" }
val artefactName: String by extra { "Logging Interceptor Core" }

dependencies {
    implementation("org.json:json:20180130")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(mapOf(
                "Implementation-Title" to artefactName,
                "Automatic-Module-Name" to "${rootProject.extra["projectGroup"]}.$archivesBaseName"
        ))
    }
}