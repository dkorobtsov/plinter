import groovy.xml.dom.DOMCategory.attributes
import java.nio.charset.StandardCharsets

val projectUrl: String by extra { "https://github.com/dkorobtsov/LoggingInterceptor" }
val archivesBaseName: String by extra { "LoggingInterceptor" }
val projectName: String by extra { "Pretty Logging Interceptor" }
val projectDescription: String by extra { "Library for pretty logging HTTP traffic" }
val allTestCoverageFile: String by extra { "/${project.rootDir}/jacoco/allTestCoverage.exec" }

plugins {
    java
    maven
    jacoco
    id("project-report")
    id("org.sonarqube") version "2.6.2"
}

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
        property("sonar.jacoco.reportPaths", allTestCoverageFile)
        property("sonar.dynamicAnalysis", "reuseReports")
        property("sonar.jacoco.reportPath", allTestCoverageFile)
        property("sonar.language", "java")
        property("sonar.projectKey", "LoggingInterceptor")
        property("sonar.organization", "dkorobtsov-github")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.login", project.property("sonar.login"))
    }
}

configure(listOf(rootProject)) {
    description = "HTTP traffic Pretty Logger"
}

configure(subprojects) {
    val project = this
    group = "com.dkorobtsov.logging"
    version = "5.0-SNAPSHOT"

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

    tasks.register<Delete>("cleanAll") {
        delete("out", "apache-interceptor/out")
        delete("out", "okhttp-interceptor/out")
        delete("out", "okhttp3-interceptor/out")
        delete("out", "interceptor-core/out")
        delete("out", "interceptor-tests/out")
        isFollowSymlinks = true
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
