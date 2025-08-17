plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    kotlin("kapt")
    alias(libs.plugins.hiltAndroid)
    id("jacoco")
}

android {
    namespace = "com.an.intelligence.flashyfishki"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.an.intelligence.flashyfishki"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isTestCoverageEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

tasks.withType<JacocoReport> {
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required = true
        html.required = true
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )
    
    val debugTree = fileTree(mapOf(
        "dir" to "$buildDir/intermediates/classes/debug",
        "excludes" to fileFilter
    ))
    
    val mainSrc = "$projectDir/src/main/java"

    sourceDirectories.setFrom(files(listOf(mainSrc)))
    classDirectories.setFrom(files(listOf(debugTree)))
    executionData.setFrom(fileTree(mapOf(
        "dir" to project.buildDir,
        "includes" to listOf("jacoco/testDebugUnitTest.exec")
    )))
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("androidx.room:room-testing:${libs.versions.roomVersion.get()}")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2") 
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("com.google.dagger:hilt-android-testing:${libs.versions.daggerHiltVersion.get()}")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:${libs.versions.daggerHiltVersion.get()}")
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}