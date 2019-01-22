package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.CommandExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaRTA
import ch.uzh.ifi.seal.bencher.analysis.weight.CSVMethodWeightTransformer
import ch.uzh.ifi.seal.bencher.analysis.weight.CSVMethodWeighter
import picocli.CommandLine
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.Callable

@CommandLine.Command(
        name = CommandNames.transformCSVWeights,
        descriptionHeading = "\nTransform Method Weights\n\n",
        description = ["Transforms method weights according to the benchmark's call-graphs", ""],
        requiredOptionMarker = '*',
        subcommands = [CommandLine.HelpCommand::class]
)
internal class CommandTransformCSVWeights : Callable<CommandExecutor> {
    @CommandLine.Spec
    private lateinit var spec: CommandLine.Model.CommandSpec

    @CommandLine.ParentCommand
    lateinit var parent: CommandTransform


    var weights: File? = null
        @CommandLine.Option(
                names = ["-w", "--weights"],
                description = ["method-weights file path"],
                required = true
//            validateWith = [FileExistsValidator::class, FileIsFileValidator::class],
//            converter = FileConverter::class
        )
        set(value) {
            val name = "weights"
            FileExistsValidator.validate(spec, name, value)
            FileIsFileValidator.validate(spec, name, value)
            field = value
        }


    var jar: File = File("")
        @CommandLine.Option(
                names = ["-f", "--file"],
                description = ["jar file path"],
                required = true
//            validateWith = [FileExistsValidator::class, FileIsFileValidator::class],
//            converter = FileConverter::class
        )
        set(value) {
            val name = "jar"
            FileExistsValidator.validate(spec, name, value)
            FileIsFileValidator.validate(spec, name, value)
            field = value
        }


    @CommandLine.Mixin
    var scg = MixinSCG()

    override fun call(): CommandExecutor {
        return CSVMethodWeightTransformer(
                jar = jar.toPath(),
                methodWeighter = CSVMethodWeighter(
                        file = FileInputStream(weights),
                        hasParams = true,
                        hasHeader = true
                ),
                output = parent.parent.out,
                walaSCGAlgo = WalaRTA(),
                walaSCGInclusions = scg.inclusions,
                reflectionOptions = scg.reflectionOptions,
                packagePrefix = parent.parent.packagePrefix
        )
    }
}
