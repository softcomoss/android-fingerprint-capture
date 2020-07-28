plugins {
    id("commons.android-library")
}

repositories {
    flatDir {
        dirs("libs")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(group = "", name = "DermalogAndroidCommon-2.0.2", ext = "aar")
    implementation(group = "", name = "DermalogBiometricPassportSdk-1.7.5.1944", ext = "aar")
    implementation(group = "", name = "DermalogBPZF10-1.0.7.1943", ext = "aar")
    implementation(group = "", name = "DermalogImageContainer-2.5.1.1938-SNAPSHOT", ext = "aar")
    implementation(group = "", name = "DermalogJNA-5.4.0.0", ext = "aar")
    implementation(group = "", name = "DermalogNistQualityCheck-1.8.0.1938-SNAPSHOT", ext = "aar")
    implementation(group = "", name = "FourprintSegmentation2-1.15.0.1925", ext = "aar")

    api(project(ProjectDependencies.core))

    implementation(AsyncDependencies.coroutines)
    implementation(AsyncDependencies.coroutinesAndroid)
}
