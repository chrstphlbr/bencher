package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.IncludeAll
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.IncludeOnly
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaSCGInclusions
import ch.uzh.ifi.seal.bencher.execution.JMHCLIArgs
import ch.uzh.ifi.seal.bencher.execution.parseJMHCLIParameter
import ch.uzh.ifi.seal.bencher.selection.PrioritizationType
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import picocli.CommandLine


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

internal class WalaSCGInclusionsConverter : CommandLine.ITypeConverter<WalaSCGInclusions> {
    override fun convert(value: String?): WalaSCGInclusions =
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
