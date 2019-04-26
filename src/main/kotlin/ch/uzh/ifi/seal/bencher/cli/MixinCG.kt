package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGInclusions
import ch.uzh.ifi.seal.bencher.analysis.callgraph.IncludeAll
import picocli.CommandLine

internal class MixinCG {
    @CommandLine.Option(
            names = ["-inc", "--inclusions"],
            description = ["call graph inclusions: defined as package prefixes"],
            converter = [CGInclusionsConverter::class]
    )
    var inclusions: CGInclusions = IncludeAll
}
