package ch.uzh.ifi.seal.bencher

import ch.uzh.ifi.seal.bencher.jmh_results.JMHResultTransformer
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.funktionale.either.Either
import java.io.File

// cli options
val optionCommand = "c"
val optionCommandLong = "command"
val optionInFile = "i"
val optionInFileLong = "input"
val optionOutFile = "o"
val optionOutFileLong = "output"

fun main(args: Array<String>) {
    val conf = parseArgs(args)
    if (conf == null) {
        return
    }

    // TODO: change to proper trial, commit, project
    val exec: CommandExecutor = when (conf.command) {
        Command.PARSE_JMH_RESULTS -> JMHResultTransformer(inFile = conf.inFile, outFile = conf.outFile, trial = 1, commit = "", project = conf.project)
        Command.DYNAMIC_CALL_GRAPH -> TODO("dynamic call graph not implemented")
    }

    val err = exec.execute()
    if (err.isDefined()) {
        println("Execution failed with '${err.get()}'")
    }
}

fun parseArgs(args: Array<String>): Config? {
    val o = Options()
    o.addOption(Option.builder(optionCommand)
            .argName(optionCommandLong)
            .hasArg(true)
            .desc("Command")
            .required()
            .build()
    )
    o.addOption(Option.builder(optionInFile)
            .argName(optionInFileLong)
            .hasArg(true)
            .desc("Input file")
            .required()
            .build()
    )
    o.addOption(Option.builder(optionOutFile)
            .argName(optionOutFileLong)
            .hasArg(true)
            .desc("Output file")
            .required()
            .build()
    )
    // TODO: remove when config file is there
    o.addOption(Option.builder("p")
            .argName("project")
            .hasArg(true)
            .desc("Project to be parsed")
            .required()
            .build()
    )

    val cliParser = DefaultParser()
    try {
        val cl = cliParser.parse(o, args)

        // command
        val cmd: Either<Command, String> =
                if (cl.hasOption(optionCommand)) {
            val cStr = cl.getOptionValue(optionCommand)
            val c = Command.fromStr(cStr)
            if (c == null) {
                Either.right("Invalid commmand: ${cStr}")
            } else {
                Either.left(c)
            }
        } else {
            Either.right("No command provided")
        }

        if (cmd.isRight()) {
            println(appendLineBreak(cmd.right().get()))
            printUsage(o)
            return null
        }

        // input file
        val inFile: Either<String, String> = if (cl.hasOption(optionInFile)) {
            // check if inFile is path and file
            val inFile = cl.getOptionValue(optionInFile)
            val f = File(inFile)
            if (f.isFile && f.canRead()) {
                Either.left(inFile)
            } else {
                Either.right("Input file '${inFile}' does not exist")
            }
        } else {
            Either.right("No input file provided")
        }

        if (inFile.isRight()) {
            println(appendLineBreak(inFile.right().get()))
            printUsage(o)
            return null
        }

        // output file
        val outFile: Either<String, String> = if (cl.hasOption(optionOutFile)) {
            // check if outFile is path and file
            val outFile = cl.getOptionValue(optionOutFile)
            val f = File(outFile)
            if (f.isFile) {
                f.delete()
            }
            val created = f.createNewFile()
            if (created) {
                Either.left(outFile)
            } else {
                Either.right("Could not create output file '${outFile}'")
            }
        } else {
            Either.right("No output file provided")
        }

        if (outFile.isRight()) {
            println(appendLineBreak(outFile.right().get()))
            printUsage(o)
            return null
        }


        return Config(
                command = cmd.left().get(),
                inFile = inFile.left().get(),
                outFile = outFile.left().get(),
                project = cl.getOptionValue("p")
        )
    } catch(e: Exception) {
        println(e.message + "\n")
        printUsage(o)
        return null
    }
}

fun appendLineBreak(str: String): String {
    return str + "\n"
}

fun printUsage(o: Options) {
    val hf = HelpFormatter()
    hf.printHelp("bencher", o)
}
