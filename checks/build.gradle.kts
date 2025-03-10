plugins {
    id("java-library")
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.mavenPublish)
}

lint {
    htmlReport = true
    textReport = true
    absolutePaths = false
    ignoreTestSources = true
    warningsAsErrors = true

    informational += "GradleDependency"
    disable += "JavaPluginLanguageLevel"
}

tasks.test {
    jvmArgs("-Djava.awt.headless=true")
    maxParallelForks = Runtime.getRuntime().availableProcessors() * 2
}

dependencies {
    compileOnly(libs.bundles.lint.api)
    testImplementation(libs.bundles.lint.tests)
}
