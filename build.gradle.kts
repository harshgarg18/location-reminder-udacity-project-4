// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()

    }
    dependencies {
        ClassPaths.scriptClassPaths.forEach {
            classpath(it)
        }

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register(AppConfig.cleanTask, Delete::class) {
    delete(rootProject.buildDir)
}
