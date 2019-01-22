package ch.uzh.ifi.seal.bencher.cli

import picocli.CommandLine
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.IllegalArgumentException

@CommandLine.Command(
        name = CommandNames.bencher,
        descriptionHeading = "\nBencher\n\n",
        description = ["Provides functionality for software microbenchmarks", ""],
        mixinStandardHelpOptions = true,
        subcommands = [
            CommandTransform::class,
            CommandSCG::class,
            CommandPrioritize::class,
            CommandLine.HelpCommand::class
        ],
        requiredOptionMarker = '*'
)
internal class CommandMain : Runnable {
    @CommandLine.Spec
    private lateinit var spec: CommandLine.Model.CommandSpec

    @CommandLine.Option(names = ["-p", "--project"], description = ["project name"], required = true)
    var project: String = ""

    @CommandLine.Option(names = ["-pv", "--project-version"], description = ["project version"])
    var version: String = ""

    @CommandLine.Option(names = ["-i", "--instance"], description = ["instance ID"])
    var instance: String = ""

    @CommandLine.Option(names = ["-t", "--trial"], description = ["trial ID"])
    var trial: Int = 0

    @CommandLine.Option(
            names = ["-out", "--out-file"],
            description = ["output file path", " Default: System.out"],
            converter = [OutputConverter::class]
    )
    var out: OutputStream = System.out

    @CommandLine.Option(names = ["-pf", "--package-prefix"], description = ["project package prefix"])
    var packagePrefix: String = ""

    override fun run() {
        System.err.println("No COMMAND specified")
        System.err.println()
        spec.commandLine().usage(System.err)
    }
}

internal class OutputConverter : CommandLine.ITypeConverter<OutputStream> {
    override fun convert(value: String?): OutputStream {
        if (value == null) {
            throw IllegalArgumentException("No output file provided")
        }
        return FileOutputStream(value)
    }
}
