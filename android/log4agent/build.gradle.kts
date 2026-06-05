plugins {
    id("com.android.library")
    kotlin("android")
    id("maven-publish")
}

group = "dev.log4agent"
version = "0.1.0-SNAPSHOT"

android {
    namespace = "dev.log4agent.android"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        minSdk = 23
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    api("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation(kotlin("test"))
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_REPOSITORY") ?: "OWNER/Log4Agent"}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }
            groupId = "dev.log4agent"
            artifactId = "log4agent-android"
            version = "0.1.0-SNAPSHOT"
        }
    }
}
