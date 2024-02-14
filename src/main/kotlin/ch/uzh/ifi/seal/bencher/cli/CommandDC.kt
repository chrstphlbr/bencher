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
internal class CommandDC : Callable<CommandExecutor> {
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
            names = ["-covpb", "--covs-param-benchs"],
            description = ["Create coverages for each parameterized benchmark"]
    )
    var multipleCovsForParameterizedBenchmark: Boolean = false

    @CommandLine.Mixin
    val cut = MixinCoverageUnitType()

    @CommandLine.Mixin
    val cov = MixinCoverage()

    @CommandLine.Mixin
    val javaSettings = MixinJava()

    override fun call(): CommandExecutor {
        val js = javaSettings.javaSettings()
        return CoverageCommand(
            covPrinter = SimpleCoveragePrinter(
                out = parent.out,
                coverageUnitType = cut.coverageUnitType,
            ),
            covExec = JacocoDC(
                benchmarkFinder = JarBenchFinder(jar = jar.toPath(), javaSettings = js),
                javaSettings = js,
                oneCoverageForParameterizedBenchmarks = !multipleCovsForParameterizedBenchmark,
                coverageUnitType = cut.coverageUnitType,
                inclusion = cov.inclusions,
            ),
            jar = jar.toPath(),
        )
    }
}
