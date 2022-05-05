package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.analysis.coverage.sta.*
import ch.uzh.ifi.seal.bencher.analysis.finder.BenchmarkFinder

internal object CLIHelper {
    fun walaSCGExecutor(bf: BenchmarkFinder, scg: MixinSC): WalaSCG {
        val epsAssembler: EntrypointsAssembler = if (scg.sep) {
            SingleCGEntrypoints()
        } else {
            MultiCGEntrypoints()
        }

        return WalaSCG(
                algo = scg.walaSCGAlgo,
                entrypoints = CGEntrypoints(
                        mf = bf,
                        ea = epsAssembler,
                        me = BenchmarkWithSetupTearDownEntrypoints()
                ),
                inclusions = scg.cov.inclusions,
                reflectionOptions = scg.reflectionOptions
        )
    }
}
