import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialisation)
    `maven-publish`
}

publishing {
    publications {
        register( "kotlinMultiplatformPublication", MavenPublication::class.java) {
            groupId = "com.niallermoran"
            artifactId = "charteasy"
            version ="1.0"
            afterEvaluate {
                from(components.findByName("kotlinMultiplatform"))
            }
        }
    }
    repositories {

        /**
         * Use this when publishing for public use on jitpack
         */
        maven { url = uri("https://jitpack.io") }

    }
}


kotlin {


    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                @OptIn(ExperimentalKotlinGradlePluginApi::class) compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {

        androidMain.dependencies {
        }

        iosMain.dependencies {
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
        }
        getByName("commonTest") {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}

android {
    namespace = "com.github.niallermoran.charts"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

/*

android {
    namespace 'com.github.niallermoran.charts'
    compileSdk 34
    defaultConfig {
        minSdk 26
    }

    publishing {
        singleVariant('release') {
            withSourcesJar()
            withJavadocJar()
        }
    }

    compileSdk 34

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = '17'
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    kotlinOptions {
        jvmTarget = "17"
    }




}


dependencies {

    def composeBom = platform('androidx.compose:compose-bom:2024.09.02')
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation "androidx.appcompat:appcompat:1.7.0"
    implementation "androidx.core:core-ktx:1.13.1"
    implementation "com.google.android.material:material:1.12.0"
    implementation "androidx.fragment:fragment-ktx:1.8.3"
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.20"

    // Compose
    implementation "androidx.compose.runtime:runtime"
    implementation "androidx.compose.ui:ui"
    implementation "androidx.compose.foundation:foundation"
    implementation "androidx.compose.foundation:foundation-layout"
    implementation "androidx.compose.material:material"
    implementation "androidx.compose.material:material"
    implementation "androidx.compose.runtime:runtime-livedata"
    implementation "androidx.compose.ui:ui-tooling-preview"
    implementation "androidx.compose.material:material-icons-extended"
    implementation "androidx.activity:activity-compose:1.9.2"
    debugImplementation "androidx.compose.ui:ui-tooling"

    // Testing dependencies
    androidTestImplementation "androidx.arch.core:core-testing:2.2.0"
    androidTestImplementation "androidx.test.espresso:espresso-contrib:3.6.1"
    androidTestImplementation "androidx.test.espresso:espresso-core:3.6.1"

    // Compose testing dependencies
    androidTestImplementation "androidx.compose.ui:ui-test"
    androidTestImplementation "androidx.compose.ui:ui-test-junit4"
    debugImplementation "androidx.compose.ui:ui-test-manifest:1.7.2"

    implementation "androidx.constraintlayout:constraintlayout-compose:1.0.1"

}


 */
