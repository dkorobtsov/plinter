val archivesBaseName: String by extra { "interceptor-tests" }
val artefactName: String by extra { "Logging Interceptor Tests" }
val retrofitVersion: String by extra { "2.5.0" }
val moshiVersion: String by extra { "1.8.0" }

buildscript {
    repositories {
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.adarshr:gradle-test-logger-plugin:1.6.0")
    }
}

plugins {
    id("com.adarshr.test-logger") version "1.6.0"
}

dependencies {
    testImplementation(project(":interceptor-core"))
    testImplementation(project(":apache-interceptor"))
    testImplementation(project(":okhttp-interceptor"))
    testImplementation(project(":okhttp3-interceptor"))
    testImplementation("com.dkorobtsov.logging:interceptor-core:5.0-SNAPSHOT") {
        setChanging(true)
    }

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-scalars:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-jaxb:$retrofitVersion")

    // Moshi
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")

    // Apache Client
    testImplementation("org.apache.httpcomponents:httpclient:4.5.1")
    testImplementation("org.apache.httpcomponents:httpasyncclient:4.1.4")
    testImplementation("org.apache.httpcomponents:httpmime:4.5.6")

    //OkHttp3 Client
    testImplementation("com.squareup.okhttp3:logging-interceptor:3.9.1")

    //Mock WebServer
    testImplementation("com.squareup.okhttp:mockwebserver:2.7.5")

    // Log4j2
    testImplementation("org.apache.logging.log4j:log4j-core:2.11.0")

    // AssertJ
    testImplementation("org.assertj:assertj-core:3.11.1")

    // JUnit
    testImplementation("junit:junit:4.12")
    testImplementation("pl.pragmatists:JUnitParams:1.1.1")

    // Spark
    testImplementation("com.sparkjava:spark-core:2.8.0")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(mapOf(
                "Implementation-Title" to artefactName,
                "Automatic-Module-Name" to "${rootProject.extra["projectGroup"]}$archivesBaseName"
        ))
    }
}

tasks.named<Test>("test") {
    useJUnit()

    jacoco {
        toolVersion = "0.8.2"

        enabled = true

        reports {
            html.setEnabled(true)
        }
    }

    outputs.upToDateWhen {
        false
    }

    testlogger {
        setTheme("standard")
        showExceptions = true
        slowThreshold = 500
        showSummary = true
        showPassed = true
        showSkipped = true
        showFailed = true
        showStandardStreams = false
        showPassedStandardStreams = true
        showSkippedStandardStreams = true
        showFailedStandardStreams = true
    }

}