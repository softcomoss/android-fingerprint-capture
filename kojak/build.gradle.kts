plugins {
    id("commons.android-library")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    api(project(ProjectDependencies.core))

    implementation(AsyncDependencies.coroutines)
    implementation(AsyncDependencies.coroutinesAndroid)
}
