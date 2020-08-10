package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.CommandExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGCommand
import ch.uzh.ifi.seal.bencher.analysis.callgraph.SimpleCGPrinter
import ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn.jacoco.JacocoDC
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(
        name = CommandNames.dcq,
        descriptionHeading = "\nCalculate Dynamic Call-Graphs\n\n",
        description = ["Prints dynamic call-graphs for all benchmarks", ""],
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
            names = ["-cgpb", "--cgs-param-benchs"],
            description = ["Create for each parameterized benchmark an own callgraph"]
    )
    var multipleCGForParameterizedBenchmark: Boolean = false

    @CommandLine.Mixin
    var cg = MixinCG()

    override fun call(): CommandExecutor {
        return CGCommand(
                cgPrinter = SimpleCGPrinter(parent.out),
                cgExec = JacocoDC(
                        benchmarkFinder = JarBenchFinder(jar = jar.toPath()),
                        oneCoverageForParameterizedBenchmarks = !multipleCGForParameterizedBenchmark,
                        inclusion = cg.inclusions
                ),
                jar = jar.toPath()
        )
    }
}
