pluginManagement {
    repositories {
        mavenCentral()
        jcenter()
        google()
        gradlePluginPortal()
        mavenLocal()
        maven { setUrl("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        jcenter()
        google()
        mavenLocal()
        maven { setUrl("https://jitpack.io") }
    }
}

rootProject.name = "NumberIdentify"
include(":app")
 