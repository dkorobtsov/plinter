import java.nio.charset.StandardCharsets

val projectUrl: String by extra { "https://github.com/dkorobtsov/plinter" }
val projectDescription: String by extra { "HTTP traffic Pretty Logging Interceptor" }
val projectName: String by extra { "Pretty Logging Interceptor" }
val archivesBaseName: String by extra { "plinter" }
val projectVersion: String by extra { "5.1.3-SNAPSHOT" }
val projectGroup: String by extra { "io.github.dkorobtsov.plinter" }
val gradleScriptDir by extra(file("${rootProject.projectDir}/gradle"))

val okioVersion: String by extra { "2.1.0" }

rootProject.extra.set("projectGroup", projectGroup)

plugins {
    id("java-library")
    id("project-report")
    id("org.sonarqube") version "2.6.2"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sonarqube {
    properties {
        property("sonar.dynamicAnalysis", "reuseReports")
        property("sonar.language", "java")
        property("sonar.projectKey", "Plinter")
        property("sonar.organization", "dkorobtsov-github")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.login", project.property("sonar.login"))
        property("sonar.coverage.exclusions", coverageExclusions())
        property("sonar.cpd.exclusions", duplicationExclusions())
        property("sonar.exclusions", sonarExclusions())
        property("sonar.jacoco.reportPaths", "${project.rootDir}/interceptor-tests/build/jacoco/test.exec")
    }
}

configure(listOf(rootProject)) {
    description = projectDescription
}

allprojects {
    group = projectGroup
    version = projectVersion

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        gradlePluginPortal()
        maven { setUrl("http://repo1.maven.org/maven2/") }
    }

    apply(plugin = "java")
    apply(plugin = "maven")
    apply(plugin = "jacoco")

    dependencies {
        implementation("com.squareup.okio:okio:$okioVersion")
    }
}

configure(subprojects) {
    val project = this

    apply(plugin = "java-library")

    // passing through current project SourceSets to quality.gradle.kts
    rootProject.extra.set("sourceSets", listOf(project.sourceSets["main"], project.sourceSets["test"]))
    apply(from = "$gradleScriptDir/quality.gradle.kts")

    tasks.withType(JavaCompile::class) {
        options.encoding = StandardCharsets.UTF_8.displayName()
        options.isDebug = true
        options.isDeprecation = false
        options.compilerArgs.add("-nowarn")
        options.compilerArgs.add("-Xlint:none")
    }

    tasks.named<Jar>("jar") {
        manifest {
            attributes(mapOf(
                    "Implementation-Version" to project.version,
                    "Implementation-Title" to project.name,
                    "Implementation-URL" to projectUrl
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

    tasks {
        getByName<Upload>("uploadArchives") {

            onlyIf {
                // skipping publishing for test only modules
                !project.sourceSets["main"].allJava.isEmpty
            }

            repositories {

                withConvention(MavenRepositoryHandlerConvention::class) {

                    mavenDeployer {

                        withGroovyBuilder {
                            "repository"("url" to uri(mavenUrl()))
                            //"snapshotRepository"("url" to uri("$buildDir/m2/snapshots"))
                        }

                        pom.project {
                            withGroovyBuilder {
                                "licenses" {
                                    "license" {
                                        "name"("MIT")
                                        "url"("https://opensource.org/licenses/MIT")
                                        "distribution"("repo")
                                    }
                                }

                                "scm" {
                                    "url"(projectUrl)
                                    "connection"("scm:$projectUrl.git")
                                    "developerConnection"("scm:$projectUrl.git")
                                }

                                "developers" {
                                    "developer" {
                                        "id"("dkorobtsov")
                                        "name"("Dmitri Korobtsov")
                                        "email"("dmitri.korobtsov@gmail.com")
                                    }
                                }

                                "issueManagement" {
                                    "system"("GitHub issues")
                                    "url"("$projectUrl/issues")
                                }
                            }
                        }
                    }
                }
            }
        }
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

fun mavenUrl(): String {
    return when {
        //this.hasProperty("remote") -> "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
        this.hasProperty("maven.repo.local") -> this.property("maven.repo.local").toString()
        else -> "./build/repo"
    }
}

fun sonarExclusions(): String {
    return arrayOf(
            "**/CacheControl.java",
            "**/InterceptedRequestBody.java",
            "**/InterceptedHeaders.java",
            "**/InterceptedMediaType.java",
            "**/Protocol.java",
            "**/Util.java"
    ).joinToString(separator = ", ")
}

fun duplicationExclusions(): String {
    return arrayOf(
            "**/okhttp/**",
            "**/okhttp3/**"
    ).joinToString(separator = ", ")
}

fun coverageExclusions(): String {
    return arrayOf(
            "**/CacheControl.java",
            "**/HttpHeaders.java",
            "**/InterceptedHeaders.java",
            "**/HttpMethod.java",
            "**/InterceptedMediaType.java",
            "**/InterceptedRequest.java",
            "**/InterceptedRequestBody.java",
            "**/InterceptedResponseBody.java",
            "**/Protocol.java",
            "**/Util.java"
    ).joinToString(separator = ", ")
}