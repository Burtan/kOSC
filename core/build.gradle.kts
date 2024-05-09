import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotest.multiplatform)
    id("maven-publish")
}

group = "io.kosc"

kotlin {
    jvmToolchain(17)

    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    jvm()
    js {
        useEsModules()
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    useSafari()
                    useFirefoxHeadless()
                }
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.coroutines.core)
                implementation(libs.kx.io)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotest.framework.datatest)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }
    }
}

android {
    namespace = "io.kosc.core"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

publishing {
    repositories {
        mavenCentral {
            credentials {
                username = System.getenv("MAVEN_USER")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}
