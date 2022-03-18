import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.kotlin
import org.gradle.plugin.use.PluginDependenciesSpec


fun DependencyHandler.implementation(list: List<String>) {
    list.forEach {
        add("implementation", it)
    }
}

fun DependencyHandler.testImplementation(list: List<String>) {
    list.forEach {
        add("testImplementation", it)
    }
}

fun DependencyHandler.androidTestImplementation(list: List<String>) {
    list.forEach {
        add("androidTestImplementation", it)
    }
}

fun DependencyHandler.kapt(list: List<String>) {
    list.forEach {
        add("kapt", it)
    }
}

fun DependencyHandler.debugImplementation(list: List<String>) {
    list.forEach {
        add("debugImplementation", it)
    }
}

fun PluginDependenciesSpec.id(list: List<String>) {
    list.forEach {
        id(it)
    }
}

fun PluginDependenciesSpec.kotlin(list: List<String>) {
    list.forEach {
        kotlin(it)
    }
}
