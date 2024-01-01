package love.forte.suspendreversal.processor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*

private val CompletableFutureClassName = ClassName("java.util.concurrent", "CompletableFuture")
private val CompletableFutureAwaitMemberName = MemberName("kotlinx.coroutines.future", "await")
private val NullableVoidClassName = ClassName("java.lang", "Void").copy(nullable = true)
private val JvmSyntheticClassName = ClassName("kotlin.jvm", "JvmSynthetic")

internal fun resolveJvm(
    environment: SymbolProcessorEnvironment,
    symbol: AnnotationAndClassDeclaration,
    fileBuilder: FileSpec.Builder,
    typeParameterResolver: TypeParameterResolver,
    superClassName: TypeName,
) {
    val blockingTypeBuilder: TypeSpec.Builder? = generateReversalTypeSpecBuilder(
        typeParameterResolver = typeParameterResolver,
        superClassName = superClassName,
        environment = environment,
        symbol = symbol,
        isEnabled = symbol.nearestAnnotation.javaBlocking,
        classNamePrefix = "JBlocking"
    )

    val asyncTypeBuilder: TypeSpec.Builder? = generateReversalTypeSpecBuilder(
        typeParameterResolver = typeParameterResolver,
        superClassName = superClassName,
        environment = environment,
        symbol = symbol,
        isEnabled = symbol.nearestAnnotation.javaAsync,
        classNamePrefix = "JAsync"
    )

    // all suspend abstract fun
    symbol.declaration.declarations
        .filterIsInstance<KSFunctionDeclaration>()
        .filter { it.isAbstract && Modifier.SUSPEND in it.modifiers }
        .forEach { abstractSuspendFunction ->
            environment.logger.info("abstractSuspendFunction: $abstractSuspendFunction")
            if (blockingTypeBuilder != null) {
                val (blockingFun, overriddenFun) = generateBlockingFunctions(
                    symbol,
                    typeParameterResolver,
                    abstractSuspendFunction
                )
                blockingTypeBuilder.addFunction(blockingFun)
                blockingTypeBuilder.addFunction(overriddenFun)
            }

            if (asyncTypeBuilder != null) {
                val (asyncFun, overriddenFun) = generateAsyncFunctions(
                    symbol,
                    typeParameterResolver,
                    abstractSuspendFunction
                )
                asyncTypeBuilder.addFunction(asyncFun)
                asyncTypeBuilder.addFunction(overriddenFun)
            }
        }

    blockingTypeBuilder?.also { fileBuilder.addType(it.build()) }
    asyncTypeBuilder?.also { fileBuilder.addType(it.build()) }
}


private fun generateBlockingFunctions(
    annotationInfo: AnnotationAndClassDeclaration,
    typeParameterResolver: TypeParameterResolver,
    abstractSuspendFunction: KSFunctionDeclaration
): GeneratedReversalFunctions {
    val funcParameterResolver = abstractSuspendFunction.typeParameters.toTypeParameterResolver(typeParameterResolver)

    val modifiers = abstractSuspendFunction.modifiers.mapNotNull { it.toKModifier() }
    val typeVariables = abstractSuspendFunction.typeParameters.map { it.toTypeVariableName(funcParameterResolver) }
    val extensionReceiver = abstractSuspendFunction.extensionReceiver?.toTypeName(funcParameterResolver)
    val returnType = abstractSuspendFunction.returnType?.toTypeName(funcParameterResolver)

    val parameters = abstractSuspendFunction.parameters.map { param ->
        val modifiers0 = buildSet {
            if (param.isVararg) add(KModifier.VARARG)
        }

        ParameterSpec.builder(
            name = param.name?.asString() ?: "",
            type = param.type.toTypeName(funcParameterResolver),
            modifiers = modifiers0
        ).build()
    }

    val abstractSuspendFunctionName = abstractSuspendFunction.simpleName.asString()
    val abstractBlockingFunction = FunSpec.builder(abstractSuspendFunctionName + "Blocking").apply {
        // doc
        addKdoc("Blocking reversal function for [%N]\n\n @see %N", abstractSuspendFunctionName, abstractSuspendFunctionName)

        val modifiers0 = modifiers.toMutableSet()
        modifiers0.remove(KModifier.SUSPEND)
        modifiers0.add(KModifier.ABSTRACT)

        addModifiers(modifiers0)
        addTypeVariables(typeVariables)
        // receiver
        extensionReceiver?.also { receiver(it) }
        // return
        returnType?.also { returns(it) }
        // parameters
        addParameters(parameters)
        // annotations:
        resolveIncludeAnnotations(this, abstractSuspendFunction)

        clearBody()
    }.build()

    val overriddenSuspendFunction =
        FunSpec.builder(abstractSuspendFunctionName).apply {
            val modifiers0 = modifiers.toMutableSet()
            modifiers0.remove(KModifier.ABSTRACT)
            modifiers0.add(KModifier.OVERRIDE)
            addModifiers(modifiers0)

            addTypeVariables(typeVariables)
            // receiver
            extensionReceiver?.also { receiver(it) }
            // return
            returnType?.also { returns(it) }
            // parameters
            addParameters(parameters)

            // @JvmSynthetic if need
            if (annotationInfo.nearestAnnotation.markJvmSynthetic) {
                addAnnotation(JvmSyntheticClassName)
            }

            // body, call blocking func
            val formatArgs = buildList(parameters.size + 1) {
                add(abstractBlockingFunction)
                addAll(parameters)
            }

            val builder =
                if (returnType == null) StringBuilder("%N(")
                else StringBuilder("return %N(")

            parameters.joinTo(builder) { "%N" }
            builder.append(')')
            addStatement(builder.toString(), args = formatArgs.toTypedArray())
        }.build()

    return GeneratedReversalFunctions(abstractBlockingFunction, overriddenSuspendFunction)
}


private fun generateAsyncFunctions(
    annotationInfo: AnnotationAndClassDeclaration,
    typeParameterResolver: TypeParameterResolver,
    abstractSuspendFunction: KSFunctionDeclaration
): GeneratedReversalFunctions {
    val funcParameterResolver = abstractSuspendFunction.typeParameters.toTypeParameterResolver(typeParameterResolver)

    val modifiers = abstractSuspendFunction.modifiers.mapNotNull { it.toKModifier() }
    val typeVariables = abstractSuspendFunction.typeParameters.map { it.toTypeVariableName(funcParameterResolver) }
    val extensionReceiver = abstractSuspendFunction.extensionReceiver?.toTypeName(funcParameterResolver)
    val returnType = abstractSuspendFunction.returnType?.toTypeName(funcParameterResolver)?.takeIf { it != UNIT }
    val parameters = abstractSuspendFunction.parameters.map { param ->
        val modifiers0 = buildSet {
            if (param.isVararg) add(KModifier.VARARG)
        }

        ParameterSpec.builder(
            name = param.name?.asString() ?: "",
            type = param.type.toTypeName(funcParameterResolver),
            modifiers = modifiers0
        ).build()
    }

    val abstractSuspendFunctionName = abstractSuspendFunction.simpleName.asString()
    val abstractAsyncFunction = FunSpec.builder(abstractSuspendFunctionName + "Async").apply {
        // doc
        addKdoc("Async reversal function for [%N]\n\n @see %N", abstractSuspendFunctionName, abstractSuspendFunctionName)

        val modifiers0 = modifiers.toMutableSet()
        modifiers0.remove(KModifier.SUSPEND)
        modifiers0.add(KModifier.ABSTRACT)


        addModifiers(modifiers0)
        addTypeVariables(typeVariables)
        // receiver
        extensionReceiver?.also { receiver(it) }
        // return Future
        if (returnType == null) {
            returns(CompletableFutureClassName.parameterizedBy(NullableVoidClassName))
        } else {
            returnType.also { returns(CompletableFutureClassName.parameterizedBy(WildcardTypeName.producerOf(it))) }
        }
        // parameters
        addParameters(parameters)
        // annotations:
        resolveIncludeAnnotations(this, abstractSuspendFunction)

        clearBody()
    }.build()

    val overriddenSuspendFunction =
        FunSpec.builder(abstractSuspendFunctionName).apply {
            val modifiers0 = modifiers.toMutableSet()
            modifiers0.remove(KModifier.ABSTRACT)
            modifiers0.add(KModifier.OVERRIDE)
            addModifiers(modifiers0)

            addTypeVariables(typeVariables)
            // receiver
            extensionReceiver?.also { receiver(it) }
            // return
            returnType?.also { returns(it) }
            // parameters
            addParameters(parameters)

            // @JvmSynthetic if need
            if (annotationInfo.nearestAnnotation.markJvmSynthetic) {
                addAnnotation(JvmSyntheticClassName)
            }

            // body, call blocking func
            val formatArgs: List<Any> = buildList(parameters.size + 1) {
                add(abstractAsyncFunction)
                // type vars
                addAll(typeVariables)
                // params
                addAll(parameters)
                add(CompletableFutureAwaitMemberName)
            }

            val builder =
                if (returnType == null) StringBuilder("%N")
                else StringBuilder("return %N")

            if (typeVariables.isNotEmpty()) {
                typeVariables.joinTo(builder, prefix = "<", postfix = ">") { "%T" }
            }

            builder.append("(")
            parameters.joinTo(builder) { "%N" }
            builder.append(").%M()")
            addStatement(builder.toString(), args = formatArgs.toTypedArray())
        }.build()



    return GeneratedReversalFunctions(abstractAsyncFunction, overriddenSuspendFunction)
}
