plugins {
    id("com.android.library")
    kotlin("android")
    id("maven-publish")
    id("com.vanniktech.maven.publish")
}

group = "io.github.icyoung"
version = "0.1.0"

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

}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        groupId = "io.github.icyoung",
        artifactId = "log4agent-android-native",
        version = "0.1.0",
    )

    pom {
        name.set("Log4Agent Android")
        description.set("Native Android client for posting mobile logs to a local Log4Agent server.")
        inceptionYear.set("2026")
        url.set("https://github.com/Icyoung/log4agent")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("Icyoung")
                name.set("Icy")
                url.set("https://github.com/Icyoung")
            }
        }
        scm {
            url.set("https://github.com/Icyoung/log4agent")
            connection.set("scm:git:https://github.com/Icyoung/log4agent.git")
            developerConnection.set("scm:git:ssh://git@github.com/Icyoung/log4agent.git")
        }
    }
}
