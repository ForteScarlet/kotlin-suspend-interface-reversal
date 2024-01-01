plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
}

repositories {
    mavenCentral()
}

kotlin {
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    compileOnly(project(":annotations"))
    ksp(project(":processor"))
    testImplementation(kotlin("test-junit5"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        javaParameters = true
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xjvm-default=all"
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.encoding = "UTF-8"
}
