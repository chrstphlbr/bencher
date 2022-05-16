package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.analysis.weight.CoverageUnitWeightMapper
import ch.uzh.ifi.seal.bencher.analysis.weight.IdentityMethodWeightMapper
import picocli.CommandLine
import java.io.File

internal class MixinWeights {
    @CommandLine.Spec
    private lateinit var spec: CommandLine.Model.CommandSpec

    var file: File? = null
        @CommandLine.Option(
                names = ["-w", "--weights"],
                description = ["coverage unit weights file path"]
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
            names = ["-wm", "--weight-mapper"],
            description = ["coverage unit weight mapper", " Default: id", " Options: id, log10"],
            converter = [MethodWeightMapperConverter::class]
    )
    var mapper: CoverageUnitWeightMapper = IdentityMethodWeightMapper
}
