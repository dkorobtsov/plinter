buildscript {
  repositories {
    gradlePluginPortal()
  }

  dependencies {
    classpath(Dependency.testLoggerPlugin)
  }
}

plugins {
  id(Dependency.testLoggerId) version Dependency.testLoggerVersion
}

dependencies {
  testImplementation(project(Dependency.moduleCore))
  testImplementation(project(Dependency.moduleApacheInterceptor))
  testImplementation(project(Dependency.moduleOkHttpInterceptor))
  testImplementation(project(Dependency.moduleOkHttp3Interceptor))

  implementation(Dependency.moshi)
  implementation(Dependency.moshiAdapters)

  implementation(Dependency.retrofit)
  implementation(Dependency.retrofitConverterXml)
  implementation(Dependency.retrofitConverterJson)
  implementation(Dependency.retrofitConverterScalars)

  testImplementation(Dependency.apacheMime)
  testImplementation(Dependency.apacheClient)
  testImplementation(Dependency.apacheAsyncClient)
  testImplementation(Dependency.mockWebServer)
  testImplementation(Dependency.okHttp)
  testImplementation(Dependency.log4j2_code)
  testImplementation(Dependency.sparcCore)
  testImplementation(Dependency.assertJ)
  testImplementation(Dependency.junit)
  testImplementation(Dependency.junitParams)
  testImplementation(Dependency.okHttp3LoggingInterceptor)
}

tasks.named<Jar>("jar") {
  manifest {
    attributes(
      mapOf(
        "Implementation-Title" to Property.implementationTitleInterceptorTests,
        "Automatic-Module-Name" to Property.moduleNameInterceptorTests,
      ),
    )
  }
}

tasks.named<Test>("test") {
  useJUnit()

  jacoco {
    toolVersion = "0.8.10"
    enabled = true
    reports {
      html.required.set(true)
      junitXml.required.set(true)
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

tasks.jacocoTestReport {
  val allClassDirs = files()
  val allSourceDirs = files()

  // depend on interceptor-tests
  dependsOn(":test")
  // we need to be sure that all sources were built before we collect coverage
  dependsOn(rootProject.subprojects.map { "${it.path}:build" })

  rootProject.subprojects.forEach { subproject ->
    val subprojectSourceSet = subproject.extensions.findByType(SourceSetContainer::class.java)!!["main"]
    allClassDirs.from(subprojectSourceSet.output.classesDirs)
    allSourceDirs.from(subprojectSourceSet.allJava.srcDirs)
  }

  sourceDirectories.setFrom(allSourceDirs)
  classDirectories.setFrom(allClassDirs)

  executionData.setFrom(
    fileTree(rootDir.absolutePath).include("**/build/jacoco/test.exec")
  )
  reports {
    xml.required.set(true)
    csv.required.set(false)
    html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
  }
}
