package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGInclusions
import ch.uzh.ifi.seal.bencher.analysis.callgraph.IncludeAll
import ch.uzh.ifi.seal.bencher.analysis.callgraph.IncludeOnly
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.*
import ch.uzh.ifi.seal.bencher.analysis.weight.IdentityMethodWeightMapper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeightMapper
import ch.uzh.ifi.seal.bencher.analysis.weight.log10MethodWeightMapper
import ch.uzh.ifi.seal.bencher.execution.JMHCLIArgs
import ch.uzh.ifi.seal.bencher.execution.parseJMHCLIParameter
import ch.uzh.ifi.seal.bencher.selection.PrioritizationType
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import picocli.CommandLine

internal class PrefixesConverter : CommandLine.ITypeConverter<Set<String>> {
    override fun convert(value: String?): Set<String> =
            if (value == null || value.isBlank()) {
                setOf("")
            } else {
                value.split(",").toSet()
            }
}


internal class ReflectionOptionsConverter : CommandLine.ITypeConverter<AnalysisOptions.ReflectionOptions> {
    override fun convert(value: String?): AnalysisOptions.ReflectionOptions {
        if (value == null) {
            return AnalysisOptions.ReflectionOptions.FULL
        }

        return try {
            AnalysisOptions.ReflectionOptions.valueOf(value)
        } catch (e: IllegalArgumentException) {
            AnalysisOptions.ReflectionOptions.FULL
        }
    }
}

internal class WalaAlgoConverter : CommandLine.ITypeConverter<WalaSCGAlgo> {
    override fun convert(value: String?): WalaSCGAlgo {
        if (value == null) {
            return Wala01CFA()
        }

        return when (value) {
            "RTA" -> WalaRTA()
            "0CFA" -> Wala0CFA()
            "01CFA" -> Wala01CFA()
            "01CFAContainer" -> Wala01CFAContainer()
            "1CFA" -> Wala1CFA()
            else -> Wala01CFA()
        }
    }
}

internal class PrioritizationTypeConverter : CommandLine.ITypeConverter<PrioritizationType> {
    override fun convert(value: String?): PrioritizationType {
        if (value == null) {
            return PrioritizationType.TOTAL
        }

        return try {
            PrioritizationType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            PrioritizationType.TOTAL
        }
    }
}

internal class CGInclusionsConverter : CommandLine.ITypeConverter<CGInclusions> {
    override fun convert(value: String?): CGInclusions =
            if (value == null || value.isBlank()) {
                IncludeAll
            } else {
                IncludeOnly(value.split(",").toSet())
            }
}

internal class JMHCLIArgsConverter : CommandLine.ITypeConverter<JMHCLIArgs> {
    override fun convert(value: String?): JMHCLIArgs {
        if (value == null) {
            return JMHCLIArgs()
        }
        return parseJMHCLIParameter(value)
    }
}

internal class MethodWeightMapperConverter : CommandLine.ITypeConverter<MethodWeightMapper> {
    override fun convert(value: String?): MethodWeightMapper {
        if (value == null) {
            return IdentityMethodWeightMapper
        }
        return when (value) {
            "id" -> IdentityMethodWeightMapper
            "log10" -> log10MethodWeightMapper
            else -> IdentityMethodWeightMapper
        }
    }
}
