package love.forte.suspendreversal.annotations

/**
 *
 * @author ForteScarlet
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class SuspendReversal(
    val javaBlocking: Boolean = true,
    val javaAsync: Boolean = true,
    val jsAsync: Boolean = true,
    val markJvmSyntheticInOverriddenSuspendFunction: Boolean = true,
    // todo more options..?
)
