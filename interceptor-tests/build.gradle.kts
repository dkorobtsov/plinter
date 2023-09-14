buildscript {
  repositories {
    gradlePluginPortal()
  }

  dependencies {
    classpath(libs.test.logger)
  }
}

plugins {
  alias(libs.plugins.test.logger.plugin)
}

dependencies {
  testImplementation(project(Property.Module.Core.refence))
  testImplementation(project(Property.Module.Apache.refence))
  testImplementation(project(Property.Module.OkHttp.refence))
  testImplementation(project(Property.Module.OkHttp3.refence))

  implementation(libs.moshi)
  implementation(libs.moshi.adapters)

  implementation(libs.retrofit)
  implementation(libs.retrofit.xml)
  implementation(libs.retrofit.json)
  implementation(libs.retrofit.scalars)

  testImplementation(libs.apache.mime)
  testImplementation(libs.apache.client)
  testImplementation(libs.apache.async.client)
  testImplementation(libs.mock.webserver)
  testImplementation(libs.okhttp)
  testImplementation(libs.log4j2.core)
  testImplementation(libs.sparc)
  testImplementation(libs.assertj)
  testImplementation(libs.junit)
  testImplementation(libs.junit.params)
  testImplementation(libs.okhttp3.interceptor)
}

tasks.named<Jar>("jar") {
  manifest {
    attributes(
      mapOf(
        "Implementation-Title" to Property.Module.Tests.title,
        "Automatic-Module-Name" to Property.Module.Tests.name,
      )
    )
  }
}

tasks.named<Test>("test") {
  useJUnit()

  jacoco {
    toolVersion = libs.versions.jacoco.get()
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
  dependsOn(Property.Module.Tests.refence)
  // we need to be sure that all sources were built before we collect coverage
  dependsOn(rootProject.subprojects.map { "${it.path}:build" })

  rootProject.subprojects.forEach { subproject ->
    val subprojectSourceSet =
      subproject.extensions.findByType(SourceSetContainer::class.java)!!["main"]
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
