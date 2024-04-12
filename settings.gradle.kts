pluginManagement {
    repositories {
        mavenCentral()
        jcenter()
        google()
        gradlePluginPortal()
        mavenLocal()
        maven { setUrl("https://jitpack.io") }

        // tflite 下载tflite的模型和opencv
        maven {
            name= "ossrh-snapshot"
            setUrl("https://oss.sonatype.org/content/repositories/snapshots")
        }
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
