import groovy.xml.dom.DOMCategory.attributes
import java.nio.charset.StandardCharsets

val projectUrl: String by extra { "https://github.com/dkorobtsov/LoggingInterceptor" }
val projectDescription: String by extra { "HTTP traffic Pretty Logger" }
val projectName: String by extra { "Pretty Logging Interceptor" }
val projectGroup: String by extra { "com.dkorobtsov.logging" }
val archivesBaseName: String by extra { "LoggingInterceptor" }
val projectVersion: String by extra { "5.0-SNAPSHOT" }

plugins {
    maven
    jacoco
    id("java-library")
    id("project-report")
    id("org.sonarqube") version "2.6.2"
}

group = projectGroup
version = projectVersion

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
    options.isDebug = true
}

sonarqube {
    properties {
        property("sonar.jacoco.reportPaths", "${project.rootDir}/interceptor-tests/build/jacoco/test.exec")
        property("sonar.dynamicAnalysis", "reuseReports")
        property("sonar.language", "java")
        property("sonar.projectKey", "LoggingInterceptor")
        property("sonar.organization", "dkorobtsov-github")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.login", project.property("sonar.login"))
        property("sonar.coverage.exclusions", coverageExclusions())
        property("sonar.cpd.exclusions", duplicationExclusions())
        property("sonar.exclusions", sonarExclusions())
    }
}

configure(listOf(rootProject)) {
    description = projectDescription
}

configure(subprojects) {
    val project = this
    group = projectGroup
    version = projectVersion

    apply(plugin = "java")
    apply(plugin = "maven")
    apply(plugin = "jacoco")

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven { setUrl("http://repo1.maven.org/maven2/") }
    }

    dependencies {
        implementation("com.squareup.okio:okio:2.1.0")
    }

    tasks.named<Jar>("jar") {
        manifest {
            attributes(mapOf(
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to project.version,
                    "Implementation-URL" to projectUrl
            ))
        }
    }

    val sourceSets = project.the<SourceSetContainer>()

    val sourceJar by tasks.creating(Jar::class) {
        from(sourceSets.getByName("main").allJava)
        classifier = "sources"
    }

    val javadocJar by tasks.creating(Jar::class) {
        from(tasks.getByName("javadoc"))
        classifier = "javadoc"
    }

    tasks.withType(Javadoc::class) {
        isFailOnError = true
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    artifacts.add("archives", sourceJar)
    artifacts.add("archives", javadocJar)


    fun mavenUrl(): String {
        return when {
            //this.hasProperty("remote") -> "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            this.hasProperty("maven.repo.local") -> this.property("maven.repo.local").toString()
            else -> "./build/repo"
        }
    }

    tasks.register<Copy>("getDependencies") {
        from(project.sourceSets["main"].runtimeClasspath)
        into("runtime/")
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

        testLogging {
            showStandardStreams = false
            events("passed", "skipped", "failed")
        }
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
                                "parent" {
                                    "groupId"(project.group)
                                    "artifactId"(archivesBaseName)
                                    "version"(project.version)
                                }

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
            "**/interceptors/okhttp/**",
            "**/interceptors/okhttp3/**"
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