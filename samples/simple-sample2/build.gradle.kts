plugins {
    kotlin("multiplatform")
    alias(libs.plugins.ksp)
}

repositories {
    mavenCentral()
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm()
    js(IR) {
        browser()
        nodejs()
        binaries.library()
    }

    // tier1
    linuxX64()
    macosX64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            compileOnly(project(":annotations"))
        }

        jsMain.dependencies {
            implementation(project(":annotations"))
        }
    }
}

dependencies {
    add("kspJvm", project(":processor"))
    add("kspJs", project(":processor"))
}
