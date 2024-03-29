import love.forte.gradle.common.core.Gpg
import love.forte.gradle.common.core.project.setup
import love.forte.gradle.common.core.property.systemProp
import love.forte.gradle.common.publication.configure.multiplatformConfigPublishing

plugins {
    id("org.jetbrains.dokka")
    signing
    `maven-publish`
}

setup(P)

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.encoding = "UTF-8"
}


val (sonatypeUsername, sonatypePassword) = sonatypeUserInfoOrNull

val sonatypeContains = sonatypeUserInfoOrNull != null

val p = project
multiplatformConfigPublishing {
    project = P

    val jarJavadoc by tasks.registering(Jar::class) {
        group = "documentation"
        archiveClassifier.set("javadoc")
    }
    artifact(jarJavadoc)
    isSnapshot = project.version.toString().contains("SNAPSHOT", true)
    releasesRepository = ReleaseRepository
    snapshotRepository = SnapshotRepository
    gpg = Gpg.ofSystemPropOrNull()

    if (systemProp("IS_LOCAL").toBoolean()) {
        logger.info("Is 'IS_LOCAL', mainHost set as null")
        mainHost = null
    }

    publicationsFromMainHost += listOf("wasm", "wasm32", "wasm_js")
    mainHostSupportedTargets += listOf("wasm", "wasm32", "wasm_js")
}


// TODO see https://github.com/gradle-nexus/publish-plugin/issues/208#issuecomment-1465029831
val signingTasks: TaskCollection<Sign> = tasks.withType<Sign>()
tasks.withType<PublishToMavenRepository>().configureEach {
    mustRunAfter(signingTasks)
}
