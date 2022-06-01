package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.CommandExecutor
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoverageCommand
import ch.uzh.ifi.seal.bencher.analysis.coverage.SimpleCoveragePrinter
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitType
import ch.uzh.ifi.seal.bencher.analysis.finder.asm.AsmBenchFinder
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(
        name = CommandNames.sc,
        descriptionHeading = "\nCalculate Static Coverages\n\n",
        description = ["Prints static coverages for all benchmarks", ""],
        requiredOptionMarker = '*',
        subcommands = [CommandLine.HelpCommand::class]
)
internal class CommandSC : Callable<CommandExecutor> {
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
    val sc = MixinSC()

    override fun call(): CommandExecutor {
        return CoverageCommand(
                covPrinter = SimpleCoveragePrinter(
                    out = parent.out,
                    coverageUnitType = CoverageUnitType.METHOD
                ),
                covExec = CLIHelper.walaSCExecutor(AsmBenchFinder(jar = jar, pkgPrefixes = parent.packagePrefixes), sc),
                jar = jar.toPath()
        )
    }
}
