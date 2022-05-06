package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.analysis.coverage.sta.*
import ch.uzh.ifi.seal.bencher.analysis.finder.BenchmarkFinder

internal object CLIHelper {
    fun walaSCExecutor(bf: BenchmarkFinder, sc: MixinSC): WalaSC {
        val epsAssembler: EntrypointsAssembler = if (sc.sep) {
            SingleCGEntrypoints()
        } else {
            MultiCGEntrypoints()
        }

        return WalaSC(
                algo = sc.walaSCGAlgo,
                entrypoints = CGEntrypoints(
                        mf = bf,
                        ea = epsAssembler,
                        me = BenchmarkWithSetupTearDownEntrypoints()
                ),
                inclusions = sc.cov.inclusions,
                reflectionOptions = sc.reflectionOptions
        )
    }
}
