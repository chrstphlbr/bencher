package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.CommandExecutor
import ch.uzh.ifi.seal.bencher.analysis.coverage.sta.WalaRTA
import ch.uzh.ifi.seal.bencher.analysis.weight.CSVMethodWeightTransformer
import ch.uzh.ifi.seal.bencher.analysis.weight.CSVMethodWeighter
import picocli.CommandLine
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.Callable

@CommandLine.Command(
        name = CommandNames.transformCSVWeights,
        descriptionHeading = "\nTransform Method Weights\n\n",
        description = ["Transforms method weights according to the benchmark's coverages", ""],
        requiredOptionMarker = '*',
        subcommands = [CommandLine.HelpCommand::class]
)
internal class CommandTransformCSVWeights : Callable<CommandExecutor> {
    @CommandLine.Spec
    private lateinit var spec: CommandLine.Model.CommandSpec

    @CommandLine.ParentCommand
    lateinit var parent: CommandTransform

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
    val sc = MixinSC()

    @CommandLine.Mixin
    val weights = MixinWeights()

    override fun call(): CommandExecutor {
        return CSVMethodWeightTransformer(
                jar = jar.toPath(),
                coverageUnitWeighter = CSVMethodWeighter(
                        file = FileInputStream(weights.file),
                        hasParams = true,
                        hasHeader = true
                ),
                coverageUnitWeightMapper = weights.mapper,
                output = parent.parent.out,
                walaSCGAlgo = WalaRTA(),
                coverageInclusions = sc.cov.inclusions,
                reflectionOptions = sc.reflectionOptions,
                packagePrefixes = parent.parent.packagePrefixes
        )
    }
}
