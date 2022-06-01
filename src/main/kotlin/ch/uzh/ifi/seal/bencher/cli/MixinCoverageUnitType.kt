package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitType
import picocli.CommandLine

internal class MixinCoverageUnitType {
    @CommandLine.Option(
        names = ["-cut", "--coverage-unit-type"],
        description = ["Specify the coverage unit type", " Default: \${DEFAULT-VALUE}", " Options: \${COMPLETION-CANDIDATES}"],
        converter = [CoverageUnitTypeConverter::class]
    )
    var coverageUnitType: CoverageUnitType = CoverageUnitType.METHOD
}