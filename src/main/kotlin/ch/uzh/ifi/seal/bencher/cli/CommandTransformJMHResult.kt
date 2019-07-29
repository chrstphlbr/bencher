package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.CommandExecutor
import ch.uzh.ifi.seal.bencher.jmhResults.JMHResultTransformer
import picocli.CommandLine
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.Callable

@CommandLine.Command(
        name = CommandNames.transformJMHResult,
        descriptionHeading = "\nTransform JMH Result\n\n",
        description = ["Transforms JMH results to a generic CSV", ""],
        requiredOptionMarker = '*',
        subcommands = [CommandLine.HelpCommand::class]
)
internal class CommandTransformJMHResult : Callable<CommandExecutor> {
    @CommandLine.Spec
    private lateinit var spec: CommandLine.Model.CommandSpec

    @CommandLine.ParentCommand
    lateinit var parent: CommandTransform


    var file: File = File("")
        @CommandLine.Option(
                names = ["-f", "--file"],
                description = ["file path"],
                required = true
//            validateWith = [FileExistsValidator::class, FileIsFileValidator::class],
//            converter = FileConverter::class
        )
        set(value) {
            val name = "file"
            FileExistsValidator.validate(spec, name, value)
            FileIsFileValidator.validate(spec, name, value)
            field = value
        }

    override fun call(): CommandExecutor {
        return JMHResultTransformer(
                inStream = FileInputStream(file),
                outStream = parent.parent.out,
                instance = parent.parent.instance,
                trial = parent.parent.trial,
                commit = parent.parent.version,
                project = parent.parent.project
        )
    }
}
