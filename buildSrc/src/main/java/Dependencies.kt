@file:Suppress("KDocMissingDocumentation")

object PluginDependencies {
    const val ANDROID_MAVEN = "com.github.dcendents.android-maven"

    const val SPOTLESS = "plugins.spotless"
}

object ProjectDependencies {
    const val core = ":core"
    const val dermalog = ":dermalog"
    const val kojak = ":kojak"
}

object RootDependencies {

    object Versions {
        const val kotlin = "1.3.72"
    }

    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
}

object AndroidXDependencies {

    object Versions {
        const val appCompat = "1.1.0"
        const val coreKtx = "1.3.0"
    }

    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
}

object AsyncDependencies {

    object Versions {
        const val coroutines = "1.3.7"
    }

    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val coroutinesAndroid =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    const val coroutinesTest =
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"

}

object UtilityDependencies {

    object Versions {
        const val androidUtils = "master-SNAPSHOT"
    }

    const val androidUtils = "com.github.softcomoss:android-utils:${Versions.androidUtils}"
}

object TestingDependencies {

    object Versions {
        const val roboelectric = "4.3"
        const val mockito = "2.25.0"
        const val jUnit = "4.12"
        const val androidJUnit = "1.1.1"
        const val androidTest = "1.1.0"
        const val androidTestRunner = "1.1.1"
    }
    const val jUnit = "junit:junit:${Versions.jUnit}"
    const val androidJUnit = "androidx.test.ext:junit:${Versions.androidJUnit}"
    const val androidTestRunner = "androidx.test:runner:${Versions.androidTestRunner}"
    const val androidTest = "androidx.test:core:${Versions.androidTest}"
    const val mockitoCore = "org.mockito:mockito-core:${Versions.mockito}"
    const val roboelectric = "org.robolectric:robolectric:${Versions.roboelectric}"
}

object ClasspathDependencies {

    object Versions {
        const val androidMaven = "2.1"
        const val gradle = "4.2.0-alpha04"
        const val spotless = "4.3.0"
    }

    const val allopen = "org.jetbrains.kotlin:kotlin-allopen:${RootDependencies.Versions.kotlin}"
    const val androidMaven = "com.github.dcendents:android-maven-gradle-plugin:${Versions.androidMaven}"
    const val gradle = "com.android.tools.build:gradle:${Versions.gradle}"
    const val spotless = "com.diffplug.spotless:spotless-plugin-gradle:${Versions.spotless}"
}