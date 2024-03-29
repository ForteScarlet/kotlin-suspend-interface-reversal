package pk1.pk2

import love.forte.suspendreversal.annotations.SuspendReversal
import java.io.IOException
import kotlin.jvm.Throws

@SuspendReversal
interface Foo {
    val age: Int get() = 5

    @JvmSynthetic
    suspend fun run()
    @JvmSynthetic
    suspend fun get(): Int

    val name: String
}


@SuspendReversal
interface Bar<T, out R, in I, V : Number> {
    @JvmSynthetic
    suspend fun run(): V
    @JvmSynthetic
    suspend fun <Q : T> get(value: I): Q
}


@SuspendReversal
abstract class S1 {
    @JvmSynthetic
    abstract suspend fun run()
    @JvmSynthetic
    abstract suspend fun get(): String
}


@SuspendReversal
abstract class S2<T, out R, in I, V : Number>(val name: String) {
    @JvmSynthetic
    @Throws(IOException::class)
    abstract suspend fun run()
    @JvmSynthetic
    abstract suspend fun get(): String
}
