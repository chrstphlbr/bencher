package ch.uzh.ifi.seal.bencher.cli

import picocli.CommandLine


@CommandLine.Command(
        name = CommandNames.transform,
        descriptionHeading = "\nTransformation Functionality\n\n",
        description = ["Applies transformations on a certain input"],
        subcommands = [
            CommandTransformCSVWeights::class,
            CommandTransformJMHResult::class,
            CommandLine.HelpCommand::class
        ],
        requiredOptionMarker = '*'
)
internal class CommandTransform : Runnable {
    @CommandLine.Spec
    private lateinit var spec: CommandLine.Model.CommandSpec

    @CommandLine.ParentCommand
    lateinit var parent: CommandMain

    override fun run() {
        System.err.println("No COMMAND specified for ${spec.name()}")
        System.err.println()
        spec.commandLine().usage(System.err)
    }
}
