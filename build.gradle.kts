import java.nio.charset.StandardCharsets

val gradleScriptDir by extra(file("${rootProject.projectDir}/gradle"))

plugins {
    id("java-library")
    id("project-report")
    id(Dependency.sonarcubeId) version Dependency.sonarcubeVersion
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sonarqube {
    properties {
        property("sonar.dynamicAnalysis", "reuseReports")
        property("sonar.language", "java")
        property("sonar.projectKey", Property.sonarProjectKey)
        property("sonar.organization", Property.sonarOrganization)
        property("sonar.host.url", Property.sonarHost)
        property("sonar.login", project.property("sonar.login"))
        property("sonar.coverage.exclusions", SonarConfig.coverageExclusions())
        property("sonar.cpd.exclusions", SonarConfig.duplicationExclusions())
        property("sonar.exclusions", SonarConfig.sonarExclusions())
        property("sonar.jacoco.reportPaths", "${project.rootDir}/interceptor-tests/build/jacoco/test.exec")
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
        jcenter()
        gradlePluginPortal()
        maven { setUrl("http://repo1.maven.org/maven2/") }
    }

    apply(plugin = "java")
    apply(plugin = "maven")
    apply(plugin = "jacoco")

    dependencies {
        implementation(Dependency.okio)
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
                                    "url"(Property.projectUrl)
                                    "connection"("scm:${Property.projectUrl}.git")
                                    "developerConnection"("scm:${Property.projectUrl}.git")
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
                                    "url"("${Property.projectUrl}/issues")
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




