import ru.vyarus.gradle.plugin.quality.QualityExtension
import ru.vyarus.gradle.plugin.quality.QualityPlugin

buildscript {
    repositories {
        mavenCentral()
        jcenter()
        gradlePluginPortal()
        maven { setUrl("https://jitpack.io") }
    }

    dependencies {
        classpath("ru.vyarus:gradle-quality-plugin:3.3.0")
    }
}

apply<JavaPlugin>()
apply<QualityPlugin>()
apply<CheckstylePlugin>()
apply<PmdPlugin>()

configure<QualityExtension> {

    checkstyleVersion = "8.12"
    pmdVersion = "6.10.0"
    spotbugsVersion = "3.1.10"
    codenarcVersion = "1.2.1"
    autoRegistration = true
    checkstyle = true
    spotbugs = true
    pmd = true

    // Disabled since there is no Groovy code in project
    codenarc = false

    /**
     * When false, disables quality tasks execution. Allows disabling tasks without removing plugins.
     * Quality tasks are still registered, but skip execution, except when task called directly or through
     * checkQualityMain (or other source set) grouping task.
     */

    enabled = true

    /**
     * The analysis effort level. The value specified should be one of min, default,
     * or max. Higher levels increase precision and find more bugs at the expense of
     * running time and memory consumption. Default is "max".
     */
    spotbugsEffort = "max"

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
    strict = false

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
    htmlReports = true

    /**
     * Source sets to apply checks on.
     * Default is [sourceSets.main] to apply only for project sources, excluding tests.
     */
    sourceSets = rootProject.extra.get("sourceSets") as MutableCollection<SourceSet>?

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