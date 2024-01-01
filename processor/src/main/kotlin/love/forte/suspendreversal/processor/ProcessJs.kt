package love.forte.suspendreversal.processor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*


private val JsPromiseClassName = ClassName("kotlin.js", "Promise")
private val JsPromiseAwaitMemberName = MemberName("kotlinx.coroutines", "await")
private val NullableUnitClassName = UNIT.copy(nullable = true)

internal fun resolveJs(
    environment: SymbolProcessorEnvironment,
    symbol: AnnotationAndClassDeclaration,
    fileBuilder: FileSpec.Builder,
    typeParameterResolver: TypeParameterResolver,
    superClassName: TypeName,
) {
    val asyncTypeBuilder: TypeSpec.Builder? = generateReversalTypeSpecBuilder(
        typeParameterResolver = typeParameterResolver,
        superClassName = superClassName,
        environment = environment,
        symbol = symbol,
        isEnabled = symbol.nearestAnnotation.jsAsync,
        classNamePrefix = "JsAsync"
    )

    // all suspend abstract fun
    symbol.declaration.declarations
        .filterIsInstance<KSFunctionDeclaration>()
        .filter { it.isAbstract && Modifier.SUSPEND in it.modifiers }
        .forEach { abstractSuspendFunction ->
            environment.logger.info("abstractSuspendFunction: $abstractSuspendFunction")
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

    asyncTypeBuilder?.also { fileBuilder.addType(it.build()) }
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
        addKdoc("Async reversal function for [%N]", abstractSuspendFunctionName)

        val modifiers0 = modifiers.toMutableSet()
        modifiers0.remove(KModifier.SUSPEND)
        modifiers0.add(KModifier.ABSTRACT)

        addModifiers(modifiers0)
        addTypeVariables(typeVariables)
        // receiver
        extensionReceiver?.also { receiver(it) }
        // return Future
        if (returnType == null) {
            returns(JsPromiseClassName.parameterizedBy(NullableUnitClassName))
        } else {
            returnType.also { returns(JsPromiseClassName.parameterizedBy(it)) }
        }
        // parameters
        addParameters(parameters)
        // annotations:
        //resolveIncludeAnnotations(this, abstractSuspendFunction)

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

            // body, call blocking func
            val formatArgs: List<Any> = buildList(parameters.size + 1) {
                add(abstractAsyncFunction)
                // type vars
                addAll(typeVariables)
                // params
                addAll(parameters)
                add(JsPromiseAwaitMemberName)
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
