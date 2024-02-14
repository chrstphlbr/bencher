package ch.uzh.ifi.seal.bencher.cli

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.CommandExecutor
import ch.uzh.ifi.seal.bencher.FailingCommandExecutor
import ch.uzh.ifi.seal.bencher.analysis.coverage.SimpleCoverageReader
import ch.uzh.ifi.seal.bencher.execution.JMHCLIArgs
import ch.uzh.ifi.seal.bencher.measurement.CSVPerformanceChangesReader
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizationCommand
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizationType
import picocli.CommandLine
import java.io.File
import java.io.FileInputStream
import java.time.Duration
import java.util.concurrent.Callable


@CommandLine.Command(
        name = CommandNames.prio,
        descriptionHeading = "\nPrioritization and Selection\n\n",
        description = ["Prioritizes and/or selects software microbenchmarks based on different characteristics", ""],
        requiredOptionMarker = '*',
        subcommands = [CommandLine.HelpCommand::class]
)
internal class CommandPrioritize : Callable<CommandExecutor> {
    @CommandLine.Spec
    private lateinit var spec: CommandLine.Model.CommandSpec

    @CommandLine.ParentCommand
    lateinit var parent: CommandMain

    @CommandLine.Option(names = ["-ppv", "--previous-project-version"], description = ["previous project version"])
    var previousVersion: String = ""

    @CommandLine.Option(
            names = ["-cas", "--change-aware-selection"],
            description = ["sets change-awareness of the prioritization by selecting changed benchmarks before unchanged benchmarks and performing prioritization on all covered elements"]
    )
    var changeAwareSelection: Boolean = false

    @CommandLine.Option(
        names = ["-cap", "--change-aware-prioritization"],
        description = ["sets change-awareness of the prioritization by removing covered elements that have not changed and performing prioritization on only changed covered elements"]
    )
    var changeAwarePrioritization: Boolean = false

    @CommandLine.Option(
            names = ["-pb", "--parameterized-benchmarks"],
            description = ["sets whether parameterized benchmarks should be counted once for each parameterization"]
    )
    var parameterizedBenchmarks: Boolean = false

    @CommandLine.Option(
            names = ["-pbr", "--parameterized-benchmarks-reversed"],
            description = ["sets whether parameterized benchmarks are reversed in the prioritization (highest parameter combination first)"]
    )
    var parameterizedBenchmarksReversed: Boolean = false

    @CommandLine.Option(
            names = ["-jmh", "--jmh-cli-parameters"],
            description = ["JMH command-line parameters"],
            converter = [JMHCLIArgsConverter::class]
    )
    var jmhParams: JMHCLIArgs = JMHCLIArgs()


    var v1: File = File("")
        @CommandLine.Option(
                names = ["-v1", "--version-1"],
                description = ["file path to old version\'s JAR"],
                required = true
                //            validateWith = [FileExistsValidator::class, FileIsFileValidator::class],
                //            converter = FileConverter::class
        )
        set(value) {
            val name = "v1"
            FileExistsValidator.validate(spec, name, value)
            FileIsFileValidator.validate(spec, name, value)
            field = value
        }

    var v2: File = File("")
        @CommandLine.Option(
                names = ["-v2", "--version-2"],
                description = ["file path to new version\'s JAR"],
                required = true
//            validateWith = [FileExistsValidator::class, FileIsFileValidator::class],
//            converter = FileConverter::class
        )
        set(value) {
            val name = "v2"
            FileExistsValidator.validate(spec, name, value)
            FileIsFileValidator.validate(spec, name, value)
            field = value
        }

    @CommandLine.Option(
            names = ["-pt", "--prioritization-type"],
            description = ["prioritization type", " Default: \${DEFAULT-VALUE}", " Options: \${COMPLETION-CANDIDATES}"],
            converter = [PrioritizationTypeConverter::class]
    )
    var type: PrioritizationType = PrioritizationType.TOTAL

    @CommandLine.Option(
            names = ["-tb", "--time-budget"],
            description = ["time budget for running benchmarks"]
//            validateWith = [DurationValidator::class],
//            converter = [DurationConverter::class]
    )
    var timeBudget: Duration = Duration.ZERO

    var coverageFile: File? = null
        @CommandLine.Option(
                names = ["-cov", "--coverage-file"],
                description = ["path to coverage file"],
                required = true
//            validateWith = [FileExistsValidator::class, FileIsFileValidator::class],
//            converter = FileConverter::class
        )
        set(value) {
            val name = "coverageFile"
            FileExistsValidator.validate(spec, name, value)
            FileIsFileValidator.validate(spec, name, value)
            field = value
        }

    @CommandLine.Mixin
    val cut = MixinCoverageUnitType()

    @CommandLine.Mixin
    val weights = MixinWeights()

    @CommandLine.Mixin
    val performanceChanges = MixinPerformanceChanges()

    @CommandLine.Mixin
    val javaSettings = MixinJava()

    override fun call(): CommandExecutor {
        val covReader = SimpleCoverageReader(coverageUnitType = cut.coverageUnitType)

        val cov = FileInputStream(coverageFile).use {
            covReader.read(it).getOrElse {
                return FailingCommandExecutor(it)
            }
        }

        val ws = if (weights.file != null) {
            FileInputStream(weights.file)
        } else {
            null
        }

        val pcs = if (performanceChanges.file != null) {
            FileInputStream(performanceChanges.file).use {
                CSVPerformanceChangesReader(hasHeader = true)
                                .read(it)
                    .getOrElse {
                        return FailingCommandExecutor(it)
                    }
            }
        } else {
            null
        }

        return PrioritizationCommand(
            out = parent.out,
            project = parent.project,
            version = parent.version,
            previousVersion = previousVersion,
            pkgPrefixes = parent.packagePrefixes,
            type = type,
            v1Jar = v1.toPath(),
            v2Jar = v2.toPath(),
            javaSettings = javaSettings.javaSettings(),
            cov = cov,
            weights = ws,
            coverageUnitWeightMapper = weights.mapper,
            performanceChanges = pcs,
            changeAwarePrioritization = changeAwarePrioritization,
            changeAwareSelection = changeAwareSelection,
            paramBenchs = parameterizedBenchmarks,
            paramBenchsReversed = parameterizedBenchmarksReversed,
            timeBudget = timeBudget,
            jmhParams = jmhParams.execConfig(),
        )
    }
}
