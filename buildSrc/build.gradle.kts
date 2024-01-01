plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

val kotlin = "1.9.21"
val dokka = "1.9.10"
val gradleCommon = "0.2.0"
val gradlePublishPluginVersion = "1.2.1"
val nexusPublishPlugin = "1.3.0"

dependencies {
    implementation(gradleApi())
    implementation(kotlin("gradle-plugin", kotlin))
    implementation("com.gradle.publish:plugin-publish-plugin:$gradlePublishPluginVersion")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:$dokka")
    implementation("io.github.gradle-nexus:publish-plugin:$nexusPublishPlugin")

    implementation("love.forte.gradle.common:gradle-common-core:$gradleCommon")
    implementation("love.forte.gradle.common:gradle-common-kotlin-multiplatform:$gradleCommon")
    implementation("love.forte.gradle.common:gradle-common-publication:$gradleCommon")
}
