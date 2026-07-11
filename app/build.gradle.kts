import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

/**
 * Loads signing configuration from either environment variables (used by CI)
 * or a local, git-ignored keystore.properties file. Never falls back to the
 * debug key for release builds.
 */
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { load(it) }
    }
}

fun signingValue(envName: String, propName: String): String? =
    System.getenv(envName) ?: keystoreProps.getProperty(propName)

android {
    namespace = "com.fuelnivo.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fuelnivo.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            val storeFilePath = signingValue("ANDROID_KEYSTORE_FILE", "storeFile")
            val storePass = signingValue("ANDROID_KEYSTORE_PASSWORD", "storePassword")
            val alias = signingValue("ANDROID_KEY_ALIAS", "keyAlias")
            val keyPass = signingValue("ANDROID_KEY_PASSWORD", "keyPassword")

            if (storeFilePath != null && storePass != null && alias != null && keyPass != null) {
                storeFile = file(storeFilePath)
                storePassword = storePass
                keyAlias = alias
                keyPassword = keyPass
                storeType = "PKCS12"
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            // Staged enablement (see README > R8/Proguard). Ship the first verified
            // release with these set to false. After installing and testing that
            // non-minified release, flip BOTH to true, rebuild, and re-verify
            // serialization + DataStore behavior. The ProGuard keep rules for
            // kotlinx.serialization are already in place for the minified build.
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            } else {
                // Fail clearly instead of silently signing a release with the debug key.
                gradle.taskGraph.whenReady {
                    val buildingRelease = allTasks.any {
                        it.name.contains("Release", ignoreCase = true) &&
                            (it.name.startsWith("assemble") || it.name.startsWith("bundle"))
                    }
                    if (buildingRelease) {
                        throw GradleException(
                            "Release signing configuration is missing. Provide " +
                                "ANDROID_KEYSTORE_FILE/ANDROID_KEYSTORE_PASSWORD/" +
                                "ANDROID_KEY_ALIAS/ANDROID_KEY_PASSWORD (env) or a " +
                                "keystore.properties file. Refusing to sign with the debug key."
                        )
                    }
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.google.material.components)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    coreLibraryDesugaring(libs.android.desugar.jdk.libs)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
