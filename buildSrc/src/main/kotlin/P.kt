import love.forte.gradle.common.core.project.ProjectDetail
import love.forte.gradle.common.core.project.Version
import love.forte.gradle.common.core.project.minus
import love.forte.gradle.common.core.project.version
import love.forte.gradle.common.core.property.systemProp

@Suppress("MemberVisibilityCanBePrivate")
object P : ProjectDetail() {
    const val GROUP = "love.forte.suspend-interface-reversal"
    const val DESCRIPTION =
        "Generate platform-compatible extension types for interfaces or abstract classes that contain suspend functions, based on KSP."
    const val HOMEPAGE = "https://github.com/ForteScarlet/kotlin-suspend-interface-reversal"

    override val group: String
        get() = GROUP

    override val description: String
        get() = DESCRIPTION

    override val homepage: String
        get() = HOMEPAGE

    override val version: Version = with(version(0, 1, 0)) {
        if (isSnapshot) this - Version.SNAPSHOT else this
    }

    override val developers: List<Developer> = developers {
        developer {
            id = "forte"
            name = "ForteScarlet"
            email = "ForteScarlet@163.com"
            url = "https://github.com/ForteScarlet"
        }
    }

    override val licenses: List<License> = licenses {
        license {
            name = "MIT License"
            url = "https://mit-license.org/"
        }
    }

    override val scm: Scm = scm {
        url = HOMEPAGE
        connection = "scm:git:$HOMEPAGE.git"
        developerConnection = "scm:git:ssh://git@github.com/ForteScarlet/kotlin-suspend-interface-reversal.git"
    }

}

private val isSnapshot: Boolean get() = systemProp("IS_SNAPSHOT").toBoolean()
