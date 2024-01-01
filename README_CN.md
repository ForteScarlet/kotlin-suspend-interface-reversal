# Kotlin Suspend Interface reversal

[English](README.md) | 中文

基于 KSP，为包含挂起函数的接口或抽象类生成与平台兼容的扩展类型。

## 简介

假设：你有一个如下所示的接口：

```kotlin
interface Foo {
    /** get value */
    suspend fun value(): Int
    
    /** get name */
    val name: String
}
```

现在，你希望此接口由你的用户实现并扩展，但事实上 Java 用户可能很难直接使用它。

此时，你又提供两个额外的接口来适应阻塞和异步API，如下所示：

```kotlin
import java.util.concurrent.CompletableFuture

interface Foo {
    /** get value */
    @JvmSynthetic
    suspend fun value(): Int

    /** get name */
    val name: String
}

/** 提供给 Java */
interface JBlockingFoo : Foo {
    fun valueBlocking(): Int

    @JvmSynthetic
    override suspend fun value(): Int = valueBlocking()
}

/** 提供给 Java */
interface JAsyncFoo : Foo {
    fun valueAsync(): CompletableFuture<out Int>

    @JvmSynthetic
    override suspend fun value(): Int = valueAsync().await()
}
```

通过这种方式，Java 用户可以实现它了：

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

## 使用

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
> 支持 `JVM` 和 `JS` 目标。

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

### 代码应用

添加 `@SuspendReversal` 到接口或抽象类上。

```kotlin
@SuspendReversal
interface Foo {
    val age: Int get() = 5 // 会被跳过/忽略

    @JvmSynthetic
    suspend fun run()      // 会生成对应的阻塞/异步函数
    
    @JvmSynthetic
    suspend fun get(): Int // 会生成对应的阻塞/异步函数

    val name: String       // 会被跳过/忽略
}
```

> [!note]
> 如果 `SuspendReversal.markJvmSynthetic = true`，
> 那么必须 (或者说强烈建议) 在原本的挂起函数上标记 `@JvmSynthetic`。


## 注意事项

1. 处理器将仅处理 **抽象、挂起** 的函数。

2. 在 Java 中，返回值为 `Unit` 的函数被转化为 `CompletableFuture<Void?>`（用于生成的异步函数）

3. 会从源函数上复制 `@kotlin.Throws` 和 `@kotlin.jvm.Throws`.

4. 会**粗略**计算生成的函数是否需要标记继承。

e.g.
```kotlin
interface Foo {
    suspend fun run()
    fun runBlocking() { /*...*/ }
}

// 生成 👇

interface JBlockingFoo : Foo {
    override fun runBlocking() // 将会标记 `override`
    suspend fun run() {
        runBlocking()
    }
}
```

> [!warning]
> 这种判断很粗糙，不是很可靠。
> 例如，它不会判断它是否是 `final`、参数是否具有继承关系，等等。


## 友情链接

[Kotlin suspend transform compiler plugin](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin)
: 用于为Kotlin挂起函数自动生成平台兼容函数的Kotlin编译器插件。


## 开源协议

参考 [LICENSE](LICENSE) .

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
