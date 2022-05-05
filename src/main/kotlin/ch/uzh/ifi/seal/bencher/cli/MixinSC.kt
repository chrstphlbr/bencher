package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.analysis.coverage.sta.Wala01CFA
import ch.uzh.ifi.seal.bencher.analysis.coverage.sta.WalaSCGAlgo
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import picocli.CommandLine


internal class MixinSC {
    @CommandLine.Mixin
    val cov = MixinCoverage()

    @CommandLine.Option(
            names = ["-ro", "--reflection-options"],
            description = ["WALA reflection options", " Default: \${DEFAULT-VALUE}", " Options: \${COMPLETION-CANDIDATES}"],
            converter = [ReflectionOptionsConverter::class]
    )
    var reflectionOptions: AnalysisOptions.ReflectionOptions = AnalysisOptions.ReflectionOptions.FULL

    @CommandLine.Option(
            names = ["-wa", "--wala-algo"],
            description = ["WALA algorithm", " Default: 01CFA", " Options: RTA, 0CFA, 01CFA, 01CFAContainer, 1CFA"],
            converter = [WalaAlgoConverter::class]
    )
    var walaSCGAlgo: WalaSCGAlgo = Wala01CFA()

    @CommandLine.Option(
            names = ["-sep", "--single-entry-points"],
            description = ["single entry point for call-graph construction"]
    )
    var sep: Boolean = false
}
