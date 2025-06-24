plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)  // Add this line

}

android {
    namespace = "dev.onelenyk.pprominec"
    compileSdk = 35
    defaultConfig {
        applicationId = "dev.onelenyk.pprominec"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            storeFile = file("release_keystore.jks")
            storePassword = "20101998"
            keyAlias = "key0"
            keyPassword = "20101998"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildTypes.all {
        setProperty("archivesBaseName", "pprominec-v${defaultConfig.versionName}")
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)

    implementation(libs.decompose)
    implementation(libs.decompose.compose.jetbrains)

    implementation(libs.geographiclib.java)


    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    ///
    implementation(libs.kotlinx.datetime)

    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.kotlin.test)

    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)

    implementation("org.mapsforge:mapsforge-core:0.25.0")
    implementation("org.mapsforge:mapsforge-map:0.25.0")
    implementation("org.mapsforge:mapsforge-map-android:0.25.0")

    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.datastore:datastore-core:1.1.7")

    implementation("io.insert-koin:koin-android:3.5.3")
    implementation("io.insert-koin:koin-androidx-compose:3.5.3")

    implementation("com.guolindev.permissionx:permissionx:1.8.1")

    implementation("org.osmdroid:osmdroid-android:6.1.20")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

