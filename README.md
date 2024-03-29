# Kotlin Suspend Interface reversal

English | [中文](README_CN.md)

Generate platform-compatible extension types for interfaces or abstract classes that contain suspend functions, based on KSP.

## Summary

Suppose: You have an interface like the following:

```kotlin
interface Foo {
    /** get value */
    suspend fun value(): Int
    
    /** get name */
    val name: String
}
```

Now, you want this interface to be implemented by your users, but it's not directly available to Java users!

At this point, you provide two additional interfaces to accommodate blocking and asynchrony, as follows:

```kotlin
import java.util.concurrent.CompletableFuture

interface Foo {
    /** get value */
    @JvmSynthetic
    suspend fun value(): Int

    /** get name */
    val name: String
}

/** Provided to Java */
interface JBlockingFoo : Foo {
    fun valueBlocking(): Int

    @JvmSynthetic
    override suspend fun value(): Int = valueBlocking()
}

/** Provided to Java */
interface JAsyncFoo : Foo {
    fun valueAsync(): CompletableFuture<out Int>

    @JvmSynthetic
    override suspend fun value(): Int = valueAsync().await()
}
```

In this way, Java users can implement it:

```java
public class FooImpl implements JBlockingFoo {
    @Override
    public int valueBlocking() {
        return 1;
    }

    @Override
    public String getName() {
        return "name";
    }
}
```

## Usage

### JVM

```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm") version "$KOTLIN_VERSION"
    id("com.google.devtools.ksp") version "$KSP_VERSION"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
    // annotation
    compileOnly("love.forte.suspend-interface-reversal:annotations:$VERSION")
    // ksp processor
    ksp("love.forte.suspend-interface-reversal:processor:$VERSION")
}
```

### Multiplatform

> [!note] 
> Support `JVM` and `JS`.

```kotlin
plugins {
    id("org.jetbrains.kotlin.multiplatform") version "$KOTLIN_VERSION"
    id("com.google.devtools.ksp") version "$KSP_VERSION"
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        // ...
    }
    js(IR) {
        // ...
    }

    linuxX64()
    macosX64()
    mingwX64()
    // ...
    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
            compileOnly("love.forte.suspend-interface-reversal:annotations:$VERSION")
        }

        jsMain.dependencies {
            implementation("love.forte.suspend-interface-reversal:annotations:$VERSION")
        }
    }
}

// ksp
dependencies {
    add("kspJvm", "love.forte.suspend-interface-reversal:processor:$VERSION") // process JVM
    add("kspJs", "love.forte.suspend-interface-reversal:processor:$VERSION")  // process JS
}
```

### Code

Add `@SuspendReversal` to the interfaces or abstract classes.

```kotlin
@SuspendReversal
interface Foo {
    val age: Int get() = 5 // Will be skipped

    @JvmSynthetic
    suspend fun run()      // Will generated blocking and async function
    
    @JvmSynthetic
    suspend fun get(): Int // Will generated blocking and async function

    val name: String       // Will be skipped
}
```

> [!note]
> If `SuspendReversal.markJvmSynthetic = true`, 
> then `@JvmSynthetic` must (or is strongly recommended) be added to the suspend function.


## Cautions

1. The processor will only handle the **abstract suspend** function.

2. In Java, functions with a return value of `Unit` are treated as `CompletableFuture<Void?>` (for async)

3. Will copy the annotations `@kotlin.Throws` and `@kotlin.jvm.Throws`.

4. Will **roughly** calculate whether the generated function needs to be inherited or not.

e.g.
```kotlin
interface Foo {
    suspend fun run()
    fun runBlocking() { /*...*/ }
}

// Generated 👇

interface JBlockingFoo : Foo {
    override fun runBlocking() // Will be marked `override`
    suspend fun run() {
        runBlocking()
    }
}
```

> [!warning]
> The judgment is very rough and not very reliable.
> For example, it won't determine if it's `final` or not, if the parameters are inherited or not, and so on.


## Useful Links

[Kotlin suspend transform compiler plugin](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin)
: Kotlin compiler plugin for converting suspend functions to platform-compatible functions.


## License

see [LICENSE](LICENSE) .

```text
Copyright (c) 2024 ForteScarlet

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
