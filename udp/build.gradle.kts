plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
}

group = "de.frederikbertling.kosc"

kotlin {
    jvmToolchain(17)

    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    jvm()
    // TODO JS does not work on Float arithmetics yet
    // https://youtrack.jetbrains.com/issue/KT-24975/Enforce-range-of-Float-type-in-JS
//    js {
//        useEsModules()
//        browser {
//            testTask {
//                useKarma {
//                    useChromeHeadless()
//                    //useSafari()
//                    //useFirefoxHeadless()
//                }
//            }
//        }
//    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.coroutines.core)
                implementation(libs.kx.io)
                implementation(libs.ktor.network)
                implementation(project(":core"))
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.coroutines.test)
            }
        }

        jvmTest {
            dependencies {
            }
        }

        androidUnitTest {
            dependencies {
                implementation(libs.junit)
            }
        }

        androidInstrumentedTest {
            dependencies {
                implementation(libs.junit)
                implementation(libs.coroutines.test)
                implementation(libs.kotlin.test)
                implementation(libs.ax.test.runner)
                implementation(libs.ax.test.core)
            }
        }
    }
}

android {
    namespace = "de.frederikbertling.kosc.udp"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    testOptions {
        managedDevices {
            localDevices {
                create("pixel8api34") {
                    device = "Pixel 8"
                    apiLevel = 34
                    systemImageSource = "google"
                }
            }
        }
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral(true)

    signAllPublications()

    pom {
        name.set("kOSC UDP server and client implementation")
        description.set("OSC over UDP implementation for kotlin multiplatform")
        inceptionYear.set("2024")
        url.set("https://github.com/burtan/kosc/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("burtan")
                name.set("Frederik Bertling")
                url.set("https://github.com/burtan/")
            }
        }
        scm {
            url.set("https://github.com/burtan/kosc/")
            connection.set("scm:git:git://github.com/burtan/kosc.git")
            developerConnection.set("scm:git:ssh://git@github.com/burtan/kosc.git")
        }
    }
}
