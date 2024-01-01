import org.jetbrains.dokka.DokkaConfiguration
import java.net.URI

plugins {
    id("org.jetbrains.dokka")
}

tasks.named("dokkaHtml").configure {
    tasks.findByName("kaptKotlin")?.also { kaptKotlinTask ->
        dependsOn(kaptKotlinTask)
    }
}
tasks.named("dokkaHtmlPartial").configure {
    tasks.findByName("kaptKotlin")?.also { kaptKotlinTask ->
        dependsOn(kaptKotlinTask)
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
        version = P.version
        documentedVisibilities.set(
            listOf(
                DokkaConfiguration.Visibility.PUBLIC,
                DokkaConfiguration.Visibility.PROTECTED
            )
        )
        jdkVersion.set(8)
        if (project.file("Module.md").exists()) {
            includes.from("Module.md")
        } else if (project.file("README.md").exists()) {
            includes.from("README.md")
        }


//        sourceLink {
//            localDirectory.set(projectDir.resolve("src"))
//            val relativeTo = projectDir.relativeTo(rootProject.projectDir)
//            remoteUrl.set(URL("${IProject.HOMEPAGE}/tree/v3-dev/$relativeTo/src"))
//            remoteLineSuffix.set("#L")
//        }

        perPackageOption {
            matchingRegex.set(".*internal.*") // will match all .internal packages and sub-packages
            suppress.set(true)
        }

        fun externalDocumentation(docUrl: URI, suffix: String = "package-list") {
            externalDocumentationLink {
                url.set(docUrl.toURL())

                packageListUrl.set(docUrl.resolve("${docUrl.path}/$suffix").toURL())
            }
        }

        // kotlin-coroutines doc
        externalDocumentation(URI("https://kotlinlang.org/api/kotlinx.coroutines"))

        // SLF4J
        externalDocumentation(URI("https://www.slf4j.org/apidocs"))
    }
}
