import java.nio.charset.StandardCharsets

val gradleScriptDir by extra(file("${rootProject.projectDir}/gradle"))

plugins {
  id("java-library")
  id("project-report")
  id(Dependency.sonarcubeId) version Dependency.sonarcubeVersion
  jacoco
  `maven-publish`
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
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
    property("sonar.coverage.jacoco.xmlReportPaths", "${project.rootDir}/interceptor-tests/build/reports/jacoco/test/jacocoTestReport.xml")
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
  val project = this

  apply(plugin = "java-library")

  tasks.withType(JavaCompile::class) {
    options.encoding = StandardCharsets.UTF_8.displayName()
    options.isDebug = true
    options.isDeprecation = false
    options.compilerArgs.add("-nowarn")
    options.compilerArgs.add("-Xlint:deprecation")
  }

  tasks.named<Jar>("jar") {
    manifest {
      attributes(mapOf(
          "Implementation-Version" to project.version,
          "Implementation-Title" to project.name,
          "Implementation-URL" to Property.projectUrl
      ))
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
  delete("build",
      "apache-interceptor/build",
      "okhttp-interceptor/build",
      "okhttp3-interceptor/build",
      "interceptor-core/build",
      "interceptor-tests/build")
  delete("out",
      "apache-interceptor/out",
      "okhttp-interceptor/out",
      "okhttp3-interceptor/out",
      "interceptor-core/out",
      "interceptor-tests/out")
  isFollowSymlinks = true
}
