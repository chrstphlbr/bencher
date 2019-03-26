package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.IncludeAll
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.Wala01CFAContainer
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaSCGAlgo
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaSCGInclusions
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import picocli.CommandLine


internal class MixinSCG {
    @CommandLine.Option(
            names = ["-inc", "--inclusions"],
            description = ["WALA package-prefix inclusions"],
            converter = [WalaSCGInclusionsConverter::class]
    )
    var inclusions: WalaSCGInclusions = IncludeAll

    @CommandLine.Option(
            names = ["-ro", "--reflection-options"],
            description = ["WALA reflection options", " Default: \${DEFAULT-VALUE}", " Options: \${COMPLETION-CANDIDATES}"],
            converter = [ReflectionOptionsConverter::class]
    )
    var reflectionOptions: AnalysisOptions.ReflectionOptions = AnalysisOptions.ReflectionOptions.FULL

    @CommandLine.Option(
            names = ["-wa", "--wala-algo"],
            description = ["WALA algorithm", " Default: 01CFAContainer", " Options: RTA, 0CFA, 01CFA, 01CFAContainer, 1CFA"],
            converter = [WalaAlgoConverter::class]
    )
    var walaSCGAlgo: WalaSCGAlgo = Wala01CFAContainer()

    @CommandLine.Option(
            names = ["-sep", "--single-entry-points"],
            description = ["single entry point for call-graph construction"]
    )
    var sep: Boolean = false
}
