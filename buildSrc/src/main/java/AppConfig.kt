object AppConfig {
    const val applicationId = "com.udacity.project4"

    const val minSdkVersion = 21
    const val targetSdkVersion = 31
    const val compileSdkVersion = 31

    const val buildToolsVersion = "30.0.3"
    const val versionCode = 1
    const val versionName = "1.0"

    const val testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    const val cleanTask = "clean"

    const val buildTypeRelease = "release"

    const val testSharedPath = "src/testShared/java"
    const val testDir = "test"
    const val androidTestDir = "androidTest"

    val excludedFiles = arrayOf(
        "META-INF/DEPENDENCIES",
        "META-INF/LICENSE",
        "META-INF/LICENSE.txt",
        "META-INF/license.txt",
        "META-INF/NOTICE",
        "META-INF/NOTICE.txt",
        "META-INF/notice.txt",
        "META-INF/ASL2.0",
        "META-INF/AL2.0",
        "META-INF/LGPL2.1",
        "META-INF/*.kotlin_module"
    )
}
