import org.gradle.api.artifacts.dsl.DependencyHandler

object Dependencies {
    // Android UI
    private const val appCompat =  "androidx.appcompat:appcompat:${Versions.appCompatVersion}"
    private const val legacySupport =  "androidx.legacy:legacy-support-v4:${Versions.androidXLegacySupport}"
    private const val androidXAnnotations =  "androidx.annotation:annotation:${Versions.androidXAnnotations}"
    private const val constraintLayout =  "androidx.constraintlayout:constraintlayout:${Versions.constraintVersion}"
    private const val cardView =  "androidx.cardview:cardview:${Versions.cardVersion}"
    private const val material =  "com.google.android.material:material:${Versions.materialVersion}"
    private const val recyclerView =  "androidx.recyclerview:recyclerview:${Versions.recyclerViewVersion}"

    // Architecture Components
    private const val lifeCycleExtension = "androidx.lifecycle:lifecycle-extensions:${Versions.archLifecycleVersion}"
    private const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycleVersion}"
    private const val lifecycleLiveData = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycleVersion}"

    // Navigation dependencies
    private const val navigationFragment = "androidx.navigation:navigation-fragment-ktx:${Versions.navigationVersion}"
    private const val navigationUI = "androidx.navigation:navigation-ui-ktx:${Versions.navigationVersion}"

    // Room dependencies
    private const val roomKtx = "androidx.room:room-ktx:${Versions.roomVersion}"
    private const val roomRuntime = "androidx.room:room-runtime:${Versions.roomVersion}"
    private const val roomCompiler = "androidx.room:room-compiler:${Versions.roomVersion}"

    // FirebaseUI Auth
    private const val firebaseUIAuth = "com.firebaseui:firebase-ui-auth:${Versions.firebaseUIAuthVersion}"
    private const val playServicesAuth = "com.google.android.gms:play-services-auth:${Versions.playServicesAuthVersion}"

    // Maps & Geofencing
    private const val playServicesLocation = "com.google.android.gms:play-services-location:${Versions.playServicesLocationVersion}"
    private const val playServiceMaps = "com.google.android.gms:play-services-maps:${Versions.playServiceMapsVersion}"

    // ---------------------------------------------------------------------------------------------------- //
    // Other dependencies
    private const val gson = "com.google.code.gson:gson:${Versions.gsonVersion}"
//    private const val espresso = "androidx.test.espresso:espresso-idling-resource:${Versions.espressoVersion}"
    private const val coroutine = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutinesVersion}"

    // Dependencies for local unit tests
    private const val junit = "junit:junit:${Versions.junitVersion}"
    private const val hamcrest = "org.hamcrest:hamcrest-all:${Versions.hamcrestVersion}"
    private const val archTesting = "androidx.arch.core:core-testing:${Versions.archTestingVersion}"
    private const val coroutineAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutinesVersion}"
    private const val coroutineTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutinesVersion}"
    private const val robolectric = "org.robolectric:robolectric:${Versions.robolectricVersion}"
    private const val truth = "com.google.truth:truth:${Versions.truthVersion}"
//    testImplementation "org.mockito:mockito-core:$mockitoVersion"

    // AndroidX Test - JVM testing
    private const val junitKtx = "androidx.test.ext:junit-ktx:${Versions.androidXTestExtKotlinRunnerVersion}"
    private const val coreKtx = "androidx.test:core-ktx:${Versions.androidXTestCoreVersion}"
    private const val rules = "androidx.test:rules:${Versions.androidXTestRulesVersion}"

    // ---------------------------------------------------------------------------------------------------- //
    // AndroidX Test - Instrumented testing
    private const val extJUnit = "androidx.test.ext:junit:${Versions.extJUnitVersion}"
    private const val roomTest =  "androidx.room:room-testing:${Versions.roomVersion}"
    private const val robolectricAnnotation =  "org.robolectric:annotations:${Versions.robolectricVersion}"
    private const val espressoCore =  "androidx.test.espresso:espresso-core:${Versions.espressoVersion}"
    private const val espressoContrib =  "androidx.test.espresso:espresso-contrib:${Versions.espressoVersion}"
    private const val espressoIntents =  "androidx.test.espresso:espresso-intents:${Versions.espressoVersion}"
    private const val espressoIdling =  "androidx.test.espresso.idling:idling-concurrent:${Versions.espressoVersion}"
    private const val fragmentTest = "androidx.fragment:fragment-testing:${Versions.fragmentVersion}"
    // Once https://issuetracker.google.com/127986458 is fixed this can be testImplementation
//    implementation "androidx.fragment:fragment-ktx:$fragmentVersion"
    private const val mockitoCore =  "org.mockito:mockito-core:${Versions.mockitoVersion}"
    private const val dexMakerMockito =  "com.linkedin.dexmaker:dexmaker-mockito:${Versions.dexMakerVersion}"


    val appLibraries = arrayListOf<String>().apply {
        add(appCompat)
        add(legacySupport)
        add(androidXAnnotations)
        add(constraintLayout)
        add(cardView)
        add(material)
        add(recyclerView)
        add(lifeCycleExtension)
        add(lifecycleViewModel)
        add(lifecycleLiveData)
        add(navigationFragment)
        add(navigationUI)
        add(roomKtx)
        add(roomRuntime)
        add(firebaseUIAuth)
        add(playServicesAuth)
        add(playServicesLocation)
        add(playServiceMaps)
        add(gson)
        add(coroutine)
    }

    val testLibraries = arrayListOf<String>().apply {
        add(junit)
        add(coreKtx)
        add(junitKtx)
        add(hamcrest)
        add(archTesting)
        add(coroutineAndroid)
        add(coroutineTest)
        add(robolectric)
        add(truth)
        add(rules)
    }

    val androidTestLibraries = arrayListOf<String>().apply {
        add(extJUnit)
        add(coreKtx)
        add(junitKtx)
        add(coroutineTest)
        add(rules)
        add(archTesting)
        add(roomTest)
        add(robolectricAnnotation)
        add(espressoCore)
        add(espressoContrib)
        add(espressoIntents)
        add(espressoIdling)
        add(fragmentTest)
        add(mockitoCore)
        add(dexMakerMockito)
    }

    val kaptLibraries = arrayListOf<String>().apply {
        add(roomCompiler)
    }
}
