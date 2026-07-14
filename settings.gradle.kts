pluginManagement {
    repositories {
        mavenLocal()
        maven { url = uri("https://dl.google.com/dl/android/maven2/") }
        maven { url = uri("https://repo1.maven.org/maven2/") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        maven { url = uri("https://dl.google.com/dl/android/maven2/") }
        maven { url = uri("https://repo1.maven.org/maven2/") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
    }
}

rootProject.name = "NotePad"
include(":app")
