import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotest.multiplatform)
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
    namespace = "de.frederikbertling.kosc.core"
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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.DEFAULT, true)
    signAllPublications()

    coordinates("de.frederikbertling.kosc", "core", "0.1.0")

    pom {
        name.set("kOSC core library")
        description.set("OSC implementation for kotlin multiplatform")
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
