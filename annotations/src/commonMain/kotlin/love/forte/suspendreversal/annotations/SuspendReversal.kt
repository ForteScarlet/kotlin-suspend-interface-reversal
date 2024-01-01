package love.forte.suspendreversal.annotations

/**
 *
 * @author ForteScarlet
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class SuspendReversal(
    val jBlocking: Boolean = true,
    val jBlockingClassNamePrefix: String = "JBlocking",
    val jBlockingClassNameSuffix: String = "",
    val jAsync: Boolean = true,
    val jAsyncClassNamePrefix: String = "JAsync",
    val jAsyncClassNameSuffix: String = "",
    val jsAsync: Boolean = true,
    val jsAsyncClassNamePrefix: String = "JsAsync",
    val jsAsyncClassNameSuffix: String = "",
    val markJvmSynthetic: Boolean = true,
    // todo more options..?
) {

    // TODO
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class JBlocking(
        /**
         * 生成函数的基础名称，如果为空则为当前函数名。
         * 最终生成的函数名为 [baseName] + [suffix]。
         */
        val baseName: String = "",

        /**
         * [baseName] 名称基础上追加的名称后缀。
         */
        val suffix: String = "Blocking",
        val asProperty: Boolean = false,
    )

    // TODO
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class JAsync(
        /**
         * 生成函数的基础名称，如果为空则为当前函数名。
         * 最终生成的函数名为 [baseName] + [suffix]。
         */
        val baseName: String = "",

        /**
         * [baseName] 名称基础上追加的名称后缀。
         */
        val suffix: String = "Async",
        val asProperty: Boolean = false,
    )

    // TODO
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class JsAsync(
        /**
         * 生成函数的基础名称，如果为空则为当前函数名。
         * 最终生成的函数名为 [baseName] + [suffix]。
         */
        val baseName: String = "",

        /**
         * [baseName] 名称基础上追加的名称后缀。
         */
        val suffix: String = "Async",
        val asProperty: Boolean = false,

        )

}
