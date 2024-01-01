package love.forte.suspendreversal.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 *
 * @author ForteScarlet
 */
class SuspendInterfaceReversalProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        SuspendInterfaceReversalProcessor(environment)
}
