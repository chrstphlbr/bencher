package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.CommandExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGCommand
import ch.uzh.ifi.seal.bencher.analysis.callgraph.SimpleCGPrinter
import ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn.JavaCallgraphDCG
import ch.uzh.ifi.seal.bencher.analysis.finder.AsmBenchFinder
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
            val name = "weights"
            FileExistsValidator.validate(spec, name, value)
            FileIsFileValidator.validate(spec, name, value)
            field = value
        }

    @CommandLine.Mixin
    var cg = MixinCG()

    override fun call(): CommandExecutor {
        return CGCommand(
                cgPrinter = SimpleCGPrinter(parent.out),
                cgExec = JavaCallgraphDCG(
                        benchmarkFinder = AsmBenchFinder(jar = jar, pkgPrefix = parent.packagePrefix),
                        inclusion = cg.inclusions
                ),
                jar = jar.toPath()
        )
    }
}
