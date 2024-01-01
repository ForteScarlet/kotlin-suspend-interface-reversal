# Kotlin Suspend Interface reversal

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
