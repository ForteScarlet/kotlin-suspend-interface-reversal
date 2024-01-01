import love.forte.gradle.common.core.project.setup

setup(P)

repositories {
    mavenCentral()
}

apply(plugin = "suspend-reversal-nexus-publish")
