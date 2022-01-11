package ch.uzh.ifi.seal.bencher.cli

import picocli.CommandLine
import java.io.File

internal class MixinPerformanceChanges {
    @CommandLine.Spec
    private lateinit var spec: CommandLine.Model.CommandSpec

    var file: File? = null
        @CommandLine.Option(
            names = ["-pc", "--performance-changes"],
            description = ["performance changes file path"]
        )
        set(value) {
            val name = "performance changes"
            FileExistsValidator.validate(spec, name, value)
            FileIsFileValidator.validate(spec, name, value)
            field = value
        }
}
