plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
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
                implementation(kotlin("test"))
                implementation(libs.kotest.assertions.core)
                implementation(libs.coroutines.test)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.robolectric)
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
