package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.CommandExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGCommand
import ch.uzh.ifi.seal.bencher.analysis.callgraph.SimpleCGPrinter
import ch.uzh.ifi.seal.bencher.analysis.finder.asm.AsmBenchFinder
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(
        name = CommandNames.scq,
        descriptionHeading = "\nCalculate Static Call-Graphs\n\n",
        description = ["Prints static call-graphs for all benchmarks", ""],
        requiredOptionMarker = '*',
        subcommands = [CommandLine.HelpCommand::class]
)
internal class CommandSCG : Callable<CommandExecutor> {
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

    @CommandLine.Mixin
    var scg = MixinSCG()

    override fun call(): CommandExecutor {
        return CGCommand(
                cgPrinter = SimpleCGPrinter(parent.out),
                cgExec = CLIHelper.walaSCGExecutor(AsmBenchFinder(jar = jar, pkgPrefix = parent.packagePrefix), scg),
                jar = jar.toPath()
        )
    }
}
