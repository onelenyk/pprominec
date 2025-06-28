import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinx.serialization)
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

configurations {
    create("ktlint")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "dev.onelenyk.pprominec.shared"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    // Ktlint dependency
    add("ktlint", "com.pinterest:ktlint:0.48.2")
}

// Ktlint configuration
ktlint {
    android.set(true)
    verbose.set(true)
    filter {
        exclude { element -> element.file.path.contains("build/") }
        exclude { element -> element.file.path.contains("generated/") }
    }
}

// Ktlint tasks
tasks.register<JavaExec>("ktlint") {
    group = "verification"
    description = "Check Kotlin code style."
    classpath = configurations["ktlint"]
    mainClass.set("com.pinterest.ktlint.Main")
    args("src/**/*.kt")
    // see https://pinterest.github.io/ktlint/install/cli/#command-line-usage for more information
}

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Fix Kotlin code style deviations."
    classpath = configurations["ktlint"]
    mainClass.set("com.pinterest.ktlint.Main")
    args("-F", "src/**/*.kt")
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
    // see https://pinterest.github.io/ktlint/install/cli/#command-line-usage for more information
}
