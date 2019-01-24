package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.CommandExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CachedCGExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.SimpleReader
import ch.uzh.ifi.seal.bencher.analysis.finder.AsmBenchFinder
import ch.uzh.ifi.seal.bencher.execution.JMHCLIArgs
import ch.uzh.ifi.seal.bencher.selection.PrioritizationCommand
import ch.uzh.ifi.seal.bencher.selection.PrioritizationType
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

    @CommandLine.Option(
            names = ["-ca", "--change-aware"],
            description = ["sets change-awareness of prioritization"]
    )
    var changeAware: Boolean = false

    var weights: File? = null
        @CommandLine.Option(
                names = ["-w", "-weights"],
                description = ["method-weights file path"]
//            validateWith = [FileExistsValidator::class, FileIsFileValidator::class],
//            converter = FileConverter::class
        )
        set(value) {
            val name = "weights"
            FileExistsValidator.validate(spec, name, value)
            FileIsFileValidator.validate(spec, name, value)
            field = value
        }

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

    @CommandLine.Mixin
    var scg = MixinSCG()


    var callGraphFile: File? = null
        @CommandLine.Option(
                names = ["-cgf", "--callgraph-file"],
                description = ["path to callgraph file"]
//            validateWith = [FileExistsValidator::class, FileIsFileValidator::class],
//            converter = FileConverter::class
        )
        set(value) {
            val name = "callGraphFile"
            FileExistsValidator.validate(spec, name, value)
            FileIsFileValidator.validate(spec, name, value)
            field = value
        }


    override fun call(): CommandExecutor {
        val benchFinder = AsmBenchFinder(jar = v2, pkgPrefix = parent.packagePrefix)

        val cgExecutor = CachedCGExecutor(if (callGraphFile == null) {
            CLIHelper.walaSCGExecutor(benchFinder, scg)
        } else {
            SimpleReader()
        })

        val weights = if (weights != null) {
            FileInputStream(weights)
        } else {
            null
        }

        return PrioritizationCommand(
                out = parent.out,
                project = parent.project,
                version = parent.version,
                pkgPrefix = parent.packagePrefix,
                type = type,
                v1 = v1.toPath(),
                v2 = v2.toPath(),
                benchFinder = benchFinder,
                cgExecutor = cgExecutor,
                weights = weights,
                changeAware = changeAware,
                timeBudget = timeBudget,
                jmhParams = jmhParams.execConfig()
        )
    }
}