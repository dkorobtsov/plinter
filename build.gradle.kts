import com.vanniktech.code.quality.tools.CodeQualityToolsPluginExtension
import java.nio.charset.StandardCharsets

buildscript {
  repositories {
    mavenCentral()
    google()
  }
  dependencies {
    classpath("com.vanniktech:gradle-code-quality-tools-plugin:0.23.0")
  }
}

val gradleScriptDir by extra(file("${rootProject.projectDir}/gradle"))

plugins {
  id("java-library")
  id("project-report")
  id(Dependency.sonarcubeId) version Dependency.sonarcubeVersion
  jacoco
  `maven-publish`
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

sonar {
  properties {
    property("sonar.dynamicAnalysis", "reuseReports")
    property("sonar.language", "java")
    property("sonar.projectKey", Property.sonarProjectKey)
    property("sonar.organization", Property.sonarOrganization)
    property("sonar.host.url", Property.sonarHost)
    property("sonar.coverage.exclusions", SonarConfig.coverageExclusions())
    property("sonar.cpd.exclusions", SonarConfig.duplicationExclusions())
    property("sonar.exclusions", SonarConfig.sonarExclusions())
    property(
      "sonar.coverage.jacoco.xmlReportPaths",
      "${project.rootDir}/interceptor-tests/build/reports/jacoco/test/jacocoTestReport.xml"
    )
  }
}

configure(listOf(rootProject)) {
  description = Property.projectDescription
}

allprojects {
  group = Property.projectGroup
  version = Property.projectVersion

  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven { setUrl("https://repo1.maven.org/maven2/") }
  }

  apply(plugin = "java")
  apply(plugin = "maven-publish")
  apply(plugin = "jacoco")

  dependencies {
    implementation(Dependency.okio)
  }
}

configure(subprojects) {
  apply(plugin = "java-library")
  apply(plugin = "com.vanniktech.code.quality.tools")

  configure<CodeQualityToolsPluginExtension> {
    // Collection of Code Quality Tools
    //
    // For details check:
    // https://github.com/vanniktech/gradle-code-quality-tools-plugin
    failEarly = true
    xmlReports = true
    htmlReports = false
    textReports = false
    ignoreProjects = listOf()

    checkstyle {
      // Performs code style checks on Java code
      //
      // For details check:
      // https://docs.gradle.org/current/userguide/checkstyle_plugin.html
      enabled = true
      toolVersion = "10.12.3"
      configFile = "../config/checkstyle/checkstyle.xml"
      ignoreFailures = false
      showViolations = true
      source = "src"
      include = listOf("**/*.java")
      exclude = listOf("**/gen/**")
    }

    pmd {
      // Performs quality checks on Java code
      //
      // For details check:
      // https://docs.gradle.org/current/userguide/pmd_plugin.html
      enabled = true
      toolVersion = "6.55.0"
      ruleSetFile = "../config/pmd/pmd.xml"
      ignoreFailures = false
      source = "src"
      include = listOf("**/*.java")
      exclude = listOf("**/gen/**")
    }

    lint {
      // Only works if  when one of the Android Plugins
      // (com.android.application, com.android.library, etc.) are applied.
      //
      // For details check:
      // https://developer.android.com/studio/write/lint
      enabled = false
      textReport = true
      textOutput = "stdout"
      abortOnError = null
      warningsAsErrors = null
      checkAllWarnings = null
      baselineFileName = null
      absolutePaths = null
      lintConfig = null
      checkReleaseBuilds = false
      checkTestSources = null
      checkDependencies = null
    }

    ktlint {
      // Kotlin Linter / Formatter, let's keep for build scripts
      //
      // For details check:
      // https://github.com/pinterest/ktlint
      enabled = true
      toolVersion = "0.32.0"
      experimental = false
    }

    detekt {
      // Static Code Analysis for Kotlin
      //
      // For details check:
      // https://github.com/detekt/detekt
      enabled = false
      toolVersion = "1.0.0"
      config = "code_quality_tools/detekt.yml"
      baselineFileName = null
      failFast = true
    }

    // Plugin for finding duplicate java code
    //
    // For details check:
    // https://github.com/aaschmid/gradle-cpd-plugin
    cpd {
      enabled = true
      source = "src"
      language = "java"
      ignoreFailures = null
      minimumTokenCount = 50
    }

    kotlin {
      allWarningsAsErrors = true
    }
  }

  tasks.withType(JavaCompile::class) {
    options.encoding = StandardCharsets.UTF_8.displayName()
    options.isDebug = true
    options.isDeprecation = false
    options.compilerArgs.add("-nowarn")
    options.compilerArgs.add("-Xlint:deprecation")
  }

  tasks.named<Jar>("jar") {
    manifest {
      attributes(
        mapOf(
          "Implementation-Version" to project.version,
          "Implementation-Title" to project.name,
          "Implementation-URL" to Property.projectUrl
        )
      )
    }
  }

  tasks.withType(Javadoc::class) {
    isFailOnError = true
    options.outputLevel = JavadocOutputLevel.QUIET
    (options as StandardJavadocDocletOptions)
      .addStringOption("Xdoclint:none", "-nodeprecated")
  }

  val sourceJar by tasks.creating(Jar::class) {
    from(project.the<SourceSetContainer>().getByName("main").allJava)
    this.archiveClassifier.set("sources")
  }

  val javadocJar by tasks.creating(Jar::class) {
    from(tasks.getByName("javadoc"))
    this.archiveClassifier.set("javadoc")
  }

  artifacts.add("archives", sourceJar)
  artifacts.add("archives", javadocJar)

  tasks.register<Copy>("getDependencies") {
    from(project.sourceSets["main"].runtimeClasspath)
    into("runtime/")
  }
}

tasks.register<Delete>("cleanAll") {
  delete(
    "build",
    "apache-interceptor/build",
    "okhttp-interceptor/build",
    "okhttp3-interceptor/build",
    "interceptor-core/build",
    "interceptor-tests/build"
  )
  delete(
    "out",
    "apache-interceptor/out",
    "okhttp-interceptor/out",
    "okhttp3-interceptor/out",
    "interceptor-core/out",
    "interceptor-tests/out"
  )
  isFollowSymlinks = true
}
