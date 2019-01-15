package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.CommandExecutor
import ch.uzh.ifi.seal.bencher.analysis.JMHVersionExtractor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.change.JarChangeFinder
import ch.uzh.ifi.seal.bencher.analysis.finder.BenchmarkFinder
import ch.uzh.ifi.seal.bencher.analysis.weight.CGMethodWeighter
import ch.uzh.ifi.seal.bencher.analysis.weight.CSVMethodWeighter
import ch.uzh.ifi.seal.bencher.execution.*
import org.funktionale.either.Either
import org.funktionale.option.Option
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import java.time.Duration

enum class PrioritizationType {
    DEFAULT, RANDOM, TOTAL, ADDITIONAL
}

class PrioritizationCommand(
        private val out: OutputStream,
        private val project: String,
        private val version: String,
        private val pkgPrefix: String,
        private val v1: Path,
        private val v2: Path,
        private val benchFinder: BenchmarkFinder,
        private val cgExecutor: CGExecutor,
        private val weights: InputStream? = null,
        private val type: PrioritizationType,
        private val changeAware: Boolean = false,
        private val timeBudget: Duration = Duration.ZERO,
        private val jmhParams: ExecutionConfiguration = unsetExecConfig

) : CommandExecutor {
    override fun execute(): Option<String> {
        val ebs = benchFinder.all()
        if (ebs.isLeft()) {
            return Option.Some(ebs.left().get())
        }
        val benchs = ebs.right().get()


        val ep: Either<String, Prioritizer> = when (type) {
            PrioritizationType.DEFAULT -> unweightedPrioritizer(DefaultPrioritizer(v2), cgExecutor)
            PrioritizationType.RANDOM -> unweightedPrioritizer(RandomPrioritizer(), cgExecutor)
            PrioritizationType.TOTAL -> weightedPrioritizer(type, cgExecutor, weights)
            PrioritizationType.ADDITIONAL -> weightedPrioritizer(type, cgExecutor, weights)
        }

        if (ep.isLeft()) {
            return Option.Some(ep.left().get())
        }
        val prioritizer = ep.right().get()

        val epbs = prioritizer.prioritize(benchs)
        if (epbs.isLeft()) {
            return Option.Some(epbs.left().get())
        }
        val prioritizedBenchs = epbs.right().get()

        // check whether benchmarks hava a certain time budget for execution
        val benchsInBudget: List<PrioritizedMethod<Benchmark>> = if (timeBudget != Duration.ZERO) {
            val ets = temporalSelector()
            if (ets.isLeft()) {
                return Option.Some(ets.left().get())
            }
            val sel = ets.right().get()

            val esbs = sel.select(prioritizedBenchs.map { it.method })
            if (esbs.isLeft()) {
                return Option.Some(esbs.left().get())
            }
            val selectedBenchmarks = esbs.right().get()

            // potentially O(nˆ2)
            prioritizedBenchs.filter { selectedBenchmarks.contains(it.method) }
        } else {
            prioritizedBenchs
        }

        val p = CSVPrioPrinter(out = out)
        p.print(benchsInBudget)

        return Option.empty()
    }

    private fun temporalSelector(): Either<String, Selector> {
        // configurations
        val ve = JMHVersionExtractor(jar = v2.toFile())
        val ev = ve.getVersion()
        if (ev.isLeft()) {
            return Either.left(ev.left().get())
        }
        // used JMH version
        val v = ev.right().get()

        // deafult execution configuration
        val dec = defaultExecConfig(v)

        val ebei = benchFinder.benchmarkExecutionInfos()
        if (ebei.isLeft()) {
            return Either.left(ebei.left().get())
        }
        val bei = ebei.right().get()

        val ecei = benchFinder.classExecutionInfos()
        if (ecei.isLeft()) {
            return Either.left(ecei.left().get())
        }
        val cei = ecei.right().get()

        val configurator = OverridingConfigBasedConfigurator(
                overridingExecConfig = jmhParams,
                benchExecConfigs = bei,
                classExecConfigs = cei,
                defaultExecConfig = dec
        )

        return Either.right(
                GreedyTemporalSelector(
                        budget = timeBudget,
                        timePredictor = ConfigExecTimePredictor(configurator = configurator)
                )
        )
    }

    private fun unweightedPrioritizer(p: Prioritizer, cgExecutor: CGExecutor): Either<String, Prioritizer> =
            if (changeAware) {
                val ecg = cg(cgExecutor)
                if (ecg.isLeft()) {
                    Either.left(ecg.left().get())
                } else {
                    val cgResult = ecg.right().get()
                    changeAwarePrioritizer(p, cgResult)
                }
            } else {
                Either.right(p)
            }

    private fun weightedPrioritizer(type: PrioritizationType, cgExecutor: CGExecutor, weights: InputStream?): Either<String, Prioritizer> {
        val ecg = cg(cgExecutor)
        if (ecg.isLeft()) {
            return Either.left(ecg.left().get())
        }
        val cgResult = ecg.right().get()

        val weighter = if (weights != null) {
            CSVMethodWeighter(file = weights)
        } else {
            CGMethodWeighter(cg = cgResult)
        }

        val ews = weighter.weights()
        if (ews.isLeft()) {
            return Either.left(ews.left().get())
        }
        val ws = ews.right().get()

        val prioritizer: Prioritizer = when (type) {
            PrioritizationType.TOTAL -> TotalPrioritizer(cgResult = cgResult, methodWeights = ws)
            PrioritizationType.ADDITIONAL -> AdditionalPrioritizer(cgResult = cgResult, methodWeights = ws)
            else -> return Either.left("Invalid prioritizer '$type': not prioritizable")
        }

        return changeAwarePrioritizer(prioritizer, cgResult)
    }

    private fun cg(cgExecutor: CGExecutor): Either<String, CGResult> {
        val ecgr = cgExecutor.get(v2)
        if (ecgr.isLeft()) {
            return Either.left(ecgr.left().get())
        }
        return Either.right(ecgr.right().get())
    }

    private fun changeAwarePrioritizer(prioritizer: Prioritizer, cgResult: CGResult): Either<String, Prioritizer> {
        val cf = JarChangeFinder(pkgPrefix = pkgPrefix)
        val ec = cf.changes(v1.toFile(), v2.toFile())
        if (ec.isLeft()) {
            return Either.left(ec.left().get())
        }
        val sap = SelectionAwarePrioritizer(prioritizer = prioritizer, selector = FullChangeSelector(cgResult = cgResult, changes = ec.right().get()))
        return Either.right(sap)
    }
}