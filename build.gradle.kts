import com.github.spotbugs.SpotBugsExtension
import groovy.xml.dom.DOMCategory.attributes
import ru.vyarus.gradle.plugin.quality.QualityExtension
import java.nio.charset.StandardCharsets

val projectUrl: String by extra { "https://github.com/dkorobtsov/LoggingInterceptor" }
val projectDescription: String by extra { "HTTP traffic Pretty Logging Interceptor" }
val projectName: String by extra { "Pretty Logging Interceptor" }
val projectGroup: String by extra { "io.github.dkorobtsov.logging" }
val archivesBaseName: String by extra { "LoggingInterceptor" }
val projectVersion: String by extra { "5.0-SNAPSHOT" }

plugins {
    maven
    jacoco
    id("java-library")
    id("project-report")
    id("org.sonarqube") version "2.6.2"
    id("ru.vyarus.quality") version "3.3.0"
    id("com.github.spotbugs") version "1.6.8"
    id("net.ltgt.errorprone") version "0.6"
}

group = projectGroup
version = projectVersion

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sonarqube {
    properties {
        property("sonar.dynamicAnalysis", "reuseReports")
        property("sonar.language", "java")
        property("sonar.projectKey", "LoggingInterceptor")
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

configure(subprojects) {
    val project = this
    group = projectGroup
    version = projectVersion

    apply(plugin = "java")
    apply(plugin = "maven")
    apply(plugin = "jacoco")
    apply(plugin = "ru.vyarus.quality")

    tasks.withType(JavaCompile::class) {
        options.encoding = StandardCharsets.UTF_8.displayName()
        options.isDebug = true
        options.isDeprecation = false
        options.compilerArgs.add("-nowarn")
        options.compilerArgs.add("-Xlint:none")
    }

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

    configure<QualityExtension> {
        checkstyleVersion = "8.12"
        pmdVersion = "6.10.0"
        spotbugsVersion = "3.1.10"
        codenarcVersion = "1.2.1"
        autoRegistration = true
        checkstyle = true
        pmd = true
        spotbugs = true

        // Disabled since there is no Groovy code in project
        codenarc = false

        /**
         * The priority threshold for reporting bugs. If set to low, all bugs are reported.
         * If set to medium, medium and high priority bugs are reported.
         * If set to high, only high priority bugs are reported. Default is "medium".
         */
        spotbugsEffort = "max"

        /**
         * Javac lint options to show compiler warnings, not visible by default.
         * Applies to all CompileJava tasks.
         * Options will be added as -Xlint:option
         *
         * Full list of options:
         *
         * http://docs.oracle.com/javase/8/docs/technotes/tools/windows/javac.html#BHCJCABJ
         */
        lintOptions = listOf("deprecation", "unchecked")

        /**
         * Strict quality leads to build fail on any violation found. If disabled,
         * all violation are just printed to console.
         */
        strict = true

        /**
         * When false, disables quality tasks execution. Allows disabling tasks without removing plugins.
         * Quality tasks are still registered, but skip execution, except when task called directly or through
         * checkQualityMain (or other source set) grouping task.
         */
        enabled = false

        /**
         * When false, disables reporting quality issues to console. Only gradle general error messages will
         * remain in logs. This may be useful in cases when project contains too many warnings.
         * Also, console reporting require xml reports parsing, which could be time consuming in case of too
         * many errors (large xml reports).
         * True by default.
         */
        consoleReporting = true

        /**
         * When false, no html reports will be built. True by default.
         */
        htmlReports = false

        /**
         * Source sets to apply checks on.
         * Default is [sourceSets.main] to apply only for project sources, excluding tests.
         */
        sourceSets = listOf(project.sourceSets["main"], project.sourceSets["test"])

        /**
         * Source patterns (relative to source dir) to exclude from checks. Simply sets exclusions to quality tasks.
         *
         * Animalsniffer is not affected because
         * it"s a different kind of check (and, also, it operates on classes so source patterns may not comply).
         *
         * Spotbugs (Findbugs) does not support exclusion directly, but plugin will resolve excluded classes and apply
         * them to xml exclude file (default one or provided by user).
         *
         * By default nothing is excluded.
         *
         * IMPORTANT: Patterns are checked relatively to source set dirs (not including them). So you can only
         * match source files and packages, but not absolute file path (this is gradle specific, not plugin).
         *
         * @see org.gradle.api.tasks.SourceTask#exclude(java.lang.Iterable) (base class for all quality tasks)
         */
        exclude = listOf()

        /**
         * User configuration files directory. Files in this directory will be
         * used instead of default (bundled) configs.
         */
        configDir = "config"
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