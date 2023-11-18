import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
}

allprojects {
    pluginManager.apply(SpotlessPlugin::class)

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        configure<KotlinJvmProjectExtension> {
            jvmToolchain(17)
        }

        configure<SpotlessExtension> {
            kotlin {
                ktlint("1.0.1")
            }
            kotlinGradle {
                ktlint("1.0.1")
            }
        }
}
}
