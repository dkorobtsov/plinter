dependencies {
    api(project(Dependency.moduleCore))
    implementation(Dependency.apacheMime)
    implementation(Dependency.apacheClient)
    implementation(Dependency.apacheAsyncClient)
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(mapOf(
                "Implementation-Title" to Property.implementationTitleApacheInterceptor,
                "Automatic-Module-Name" to Property.moduleNameApacheInterceptor
        ))
    }
}