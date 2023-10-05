package ch.uzh.ifi.seal.bencher.cli

import org.apache.commons.io.output.NullOutputStream
import picocli.CommandLine
import java.io.FileOutputStream
import java.io.OutputStream

@CommandLine.Command(
        name = CommandNames.bencher,
        descriptionHeading = "\nBencher\n\n",
        description = ["Provides functionality for software microbenchmarks", ""],
        mixinStandardHelpOptions = true,
        subcommands = [
            CommandTransform::class,
            CommandDC::class,
            CommandSC::class,
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

    @CommandLine.Option(
            names = ["-pf", "--package-prefix", "--package-prefixes"],
            description = ["project package prefix"],
            converter = [PrefixesConverter::class]
    )
    var packagePrefixes: Set<String> = setOf("")

    @CommandLine.Option(
            names = ["-e", "--execute"],
            description = ["Execute command (if false, abort after CLI parsing)"],
            arity = "1"
    )
    var execute: Boolean = true

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
        return if (value == "/dev/null") {
            NullOutputStream.INSTANCE
        } else {
            FileOutputStream(value)
        }
    }
}
