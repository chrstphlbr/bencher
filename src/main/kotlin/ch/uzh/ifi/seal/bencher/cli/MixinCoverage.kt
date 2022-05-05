package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.analysis.coverage.CoverageInclusions
import ch.uzh.ifi.seal.bencher.analysis.coverage.IncludeAll
import picocli.CommandLine

internal class MixinCoverage {
    @CommandLine.Option(
            names = ["-inc", "--inclusions"],
            description = ["coverage inclusions: defined as package prefixes"],
            converter = [CoverageInclusionsConverter::class]
    )
    var inclusions: CoverageInclusions = IncludeAll
}
