object ClassPaths {
    private const val gradle = "com.android.tools.build:gradle:${Versions.gradleVersion}"
    private const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"
    private const val navigationSafeArgs = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.navigationVersion}"
    private const val googleServices = "com.google.gms:google-services:${Versions.googleServicesVersion}"

    val scriptClassPaths = arrayListOf<String>().apply {
        add(gradle)
        add(kotlin)
        add(navigationSafeArgs)
        add(googleServices)
    }
}
