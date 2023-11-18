import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.changelog)
}

allprojects {
    pluginManager.apply(SpotlessPlugin::class)

    configure<SpotlessExtension> {
        kotlinGradle {
            ktlint("1.0.1")
            target("**/*.kts")
        }
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        configure<KotlinJvmProjectExtension> {
            jvmToolchain(17)
        }

        configure<SpotlessExtension> {
            kotlin {
                ktlint("1.0.1")
            }
        }
    }
}

private val VERSION_NAME by properties
version = VERSION_NAME.toString()
