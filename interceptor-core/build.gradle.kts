val archivesBaseName: String by extra { "interceptor-core" }
val artefactName: String by extra { "Logging Interceptor Core" }
val artefactDescription: String by extra { "Core Library for Logging Interceptor" }

dependencies {
    implementation("org.json:json:20180130")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(mapOf(
                "Automatic-Module-Name" to "com.dkorobtsov.logging:$archivesBaseName"
        ))
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