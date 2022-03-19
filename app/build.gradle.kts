plugins {
    // Do not change this order, kotlin plugins need to applied before navigationSafeArgs
    kotlin(Plugins.kotlinPlugins)
    id(Plugins.appPlugins)
}

android {
    compileSdk = AppConfig.compileSdkVersion
    buildToolsVersion = AppConfig.buildToolsVersion

    defaultConfig {
        applicationId = AppConfig.applicationId
        minSdk = AppConfig.minSdkVersion
        targetSdk = AppConfig.targetSdkVersion
        versionCode = AppConfig.versionCode
        versionName = AppConfig.versionName
        multiDexEnabled = true
        testInstrumentationRunner = AppConfig.testInstrumentationRunner
    }

    buildTypes {
        getByName(AppConfig.buildTypeRelease) {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    testOptions.unitTests {
        isIncludeAndroidResources = true
        isReturnDefaultValues = true
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    packagingOptions {
        resources.excludes.addAll(AppConfig.excludedFiles)
    }

    sourceSets {
        val testSharedDir = "$projectDir/${AppConfig.testSharedPath}"

        sourceSets.getByName(AppConfig.testDir) {
            java.srcDirs(testSharedDir)
        }

        sourceSets.getByName(AppConfig.androidTestDir) {
            java.srcDir(testSharedDir)
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(Dependencies.appLibraries)
    kapt(Dependencies.kaptLibraries)

    testImplementation(Dependencies.testLibraries)
    androidTestImplementation(Dependencies.androidTestLibraries)

    // Once https://issuetracker.google.com/127986458 is fixed this can be testImplementation
    debugImplementation(Dependencies.debugTestLibraries)

    androidTestImplementation(Dependencies.koinTest) {
        exclude(group = "org.mockito")
    }
}
