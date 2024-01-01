import utils.systemProperty

data class SonatypeUserInfo(val sonatypeUsername: String, val sonatypePassword: String)

private val _sonatypeUserInfo: SonatypeUserInfo? by lazy {
    val sonatypeUsername: String? = systemProperty("OSSRH_USER")
    val sonatypePassword: String? = systemProperty("OSSRH_PASSWORD")
    
    if (sonatypeUsername != null && sonatypePassword != null) {
        SonatypeUserInfo(sonatypeUsername, sonatypePassword)
    } else {
        null
    }
}

val sonatypeUserInfo: SonatypeUserInfo get() = _sonatypeUserInfo!!
val sonatypeUserInfoOrNull: SonatypeUserInfo? get() = _sonatypeUserInfo

operator fun SonatypeUserInfo?.component1(): String? = this?.sonatypeUsername
operator fun SonatypeUserInfo?.component2(): String? = this?.sonatypePassword

fun isPublishConfigurable(): Boolean {
    return sonatypeUserInfoOrNull != null
}
