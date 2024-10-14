import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.changelog)
}

allprojects {
    pluginManager.apply(SpotlessPlugin::class)

    configure<SpotlessExtension> {
        kotlinGradle {
            ktlint("1.3.1")
            target("*.kts")
        }
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        configure<KotlinJvmProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
                allWarningsAsErrors.set(
                    !properties.containsKey("android.injected.invoked.from.ide"),
                )
            }
        }

        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        configure<SpotlessExtension> {
            kotlin {
                ktlint("1.3.1")
            }
        }
    }
}

version = property("VERSION_NAME").toString()
