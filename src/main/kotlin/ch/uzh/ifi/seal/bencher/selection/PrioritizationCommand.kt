package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.CommandExecutor
import ch.uzh.ifi.seal.bencher.analysis.JMHVersionExtractor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.change.JarChangeFinder
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import ch.uzh.ifi.seal.bencher.analysis.finder.asm.AsmBenchFinder
import ch.uzh.ifi.seal.bencher.analysis.weight.CGMethodWeighter
import ch.uzh.ifi.seal.bencher.analysis.weight.CSVMethodWeighter
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeightMapper
import ch.uzh.ifi.seal.bencher.execution.*
import ch.uzh.ifi.seal.bencher.parameterizedBenchmarks
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
        private val pkgPrefixes: Set<String>,
        private val v1: Path,
        private val v2: Path,
        private val cg: CGResult,
        private val weights: InputStream? = null,
        private val methodWeightMapper: MethodWeightMapper,
        private val type: PrioritizationType,
        private val paramBenchs: Boolean = true,
        private val paramBenchsReversed: Boolean = true,
        private val changeAware: Boolean = false,
        private val timeBudget: Duration = Duration.ZERO,
        private val jmhParams: ExecutionConfiguration = unsetExecConfig

) : CommandExecutor {

    private val asmBenchFinder = AsmBenchFinder(jar = v2.toFile(), pkgPrefixes = pkgPrefixes)
    private val jarBenchFinder = JarBenchFinder(jar = v2)

    override fun execute(): Option<String> {
        val ebs = jarBenchFinder.all()
        if (ebs.isLeft()) {
            return Option.Some(ebs.left().get())
        }
        val bs = ebs.right().get()
        // make every parameterized benchmark a unique benchmark in the list
        val benchs: List<Benchmark> = if (paramBenchs) {
            bs.parameterizedBenchmarks(paramBenchsReversed)
        } else {
            bs
        }

        val ep: Either<String, Prioritizer> = when (type) {
            PrioritizationType.DEFAULT -> unweightedPrioritizer(DefaultPrioritizer(v2), cg)
            PrioritizationType.RANDOM -> unweightedPrioritizer(RandomPrioritizer(), cg)
            PrioritizationType.TOTAL -> weightedPrioritizer(type, cg, weights, methodWeightMapper)
            PrioritizationType.ADDITIONAL -> weightedPrioritizer(type, cg, weights, methodWeightMapper)
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

        val ebei = asmBenchFinder.benchmarkExecutionInfos()
        if (ebei.isLeft()) {
            return Either.left(ebei.left().get())
        }
        val bei = ebei.right().get()

        val ecei = asmBenchFinder.classExecutionInfos()
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

    private fun unweightedPrioritizer(p: Prioritizer, cg: CGResult): Either<String, Prioritizer> =
            if (changeAware) {
                changeAwarePrioritizer(p, cg)
            } else {
                Either.right(p)
            }

    private fun weightedPrioritizer(type: PrioritizationType, cg: CGResult, weights: InputStream?, methodWeightMapper: MethodWeightMapper): Either<String, Prioritizer> {
        val weighter = if (weights != null) {
            CSVMethodWeighter(file = weights, hasHeader = true)
        } else {
            CGMethodWeighter(cg = cg)
        }

        val ews = weighter.weights()
        if (ews.isLeft()) {
            return Either.left(ews.left().get())
        }
        val ws = ews.right().get()

        val prioritizer: Prioritizer = when (type) {
            PrioritizationType.TOTAL -> TotalPrioritizer(cgResult = cg, methodWeights = ws, methodWeightMapper = methodWeightMapper)
            PrioritizationType.ADDITIONAL -> AdditionalPrioritizer(cgResult = cg, methodWeights = ws, methodWeightMapper = methodWeightMapper)
            else -> return Either.left("Invalid prioritizer '$type': not prioritizable")
        }

        return changeAwarePrioritizer(prioritizer, cg)
    }

    private fun changeAwarePrioritizer(prioritizer: Prioritizer, cgResult: CGResult): Either<String, Prioritizer> {
        val cf = JarChangeFinder(pkgPrefixes = pkgPrefixes)
        val ec = cf.changes(v1.toFile(), v2.toFile())
        if (ec.isLeft()) {
            return Either.left(ec.left().get())
        }
        val sap = SelectionAwarePrioritizer(prioritizer = prioritizer, selector = FullChangeSelector(cgResult = cgResult, changes = ec.right().get()))
        return Either.right(sap)
    }
}