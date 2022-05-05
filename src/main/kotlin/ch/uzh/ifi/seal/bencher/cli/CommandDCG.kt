package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.CommandExecutor
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoverageCommand
import ch.uzh.ifi.seal.bencher.analysis.coverage.SimpleCoveragePrinter
import ch.uzh.ifi.seal.bencher.analysis.coverage.dyn.jacoco.JacocoDC
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(
        name = CommandNames.dc,
        descriptionHeading = "\nCalculate Dynamic Coverages\n\n",
        description = ["Prints dynamic coverages for all benchmarks", ""],
        requiredOptionMarker = '*',
        subcommands = [CommandLine.HelpCommand::class]
)
internal class CommandDCG : Callable<CommandExecutor> {
    @CommandLine.Spec
    private lateinit var spec: CommandLine.Model.CommandSpec

    @CommandLine.ParentCommand
    lateinit var parent: CommandMain

    var jar: File = File("")
        @CommandLine.Option(
                names = ["-f", "--file"],
                description = ["jar file path"],
                required = true
//            validateWith = [FileExistsValidator::class],
//            converter = FileConverter::class
        )
        set(value) {
            val name = "jar file"
            FileExistsValidator.validate(spec, name, value)
            FileIsFileValidator.validate(spec, name, value)
            field = value
        }
    @CommandLine.Option(
            names = ["-cgpb", "-covpb", "--cgs-param-benchs", "--covs-param-benchs"],
            description = ["Create coverages for each parameterized benchmark"]
    )
    var multipleCovsForParameterizedBenchmark: Boolean = false

    @CommandLine.Mixin
    var cov = MixinCoverage()

    override fun call(): CommandExecutor {
        return CoverageCommand(
                covPrinter = SimpleCoveragePrinter(parent.out),
                covExec = JacocoDC(
                        benchmarkFinder = JarBenchFinder(jar = jar.toPath()),
                        oneCoverageForParameterizedBenchmarks = !multipleCovsForParameterizedBenchmark,
                        inclusion = cov.inclusions
                ),
                jar = jar.toPath()
        )
    }
}
