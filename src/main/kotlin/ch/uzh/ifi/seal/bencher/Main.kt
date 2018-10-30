package ch.uzh.ifi.seal.bencher

import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGCommand
import ch.uzh.ifi.seal.bencher.analysis.callgraph.SimplePrinter
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.*
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import ch.uzh.ifi.seal.bencher.jmh_results.JMHResultTransformer
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.funktionale.either.Either
import java.io.File
import java.io.FileOutputStream

// cli options
val optionCommand = "c"
val optionCommandLong = "command"
val optionInFile = "i"
val optionInFileLong = "input"
val optionOutFile = "o"
val optionOutFileLong = "output"

val optionProject = "p"
val optionProjectLong = "project"
val optionProjectVersion = "pv"
val optionProjectVersionLong = "project_version"
val optionTrial = "t"
val optionTrialLong = "trial"


fun main(args: Array<String>) {
    val conf = parseArgs(args) ?: return

    val inFile = File(conf.inFile)
    if (!inFile.exists()) {
        println("Input file does not exist")
        return
    }
    if (!inFile.isFile) {
        println("Input file is a directory")
        return
    }
    val inPath = inFile.toPath()

    // TODO: change to proper trial, commit, project
    val exec: CommandExecutor = when (conf.command) {
        Command.PARSE_JMH_RESULTS -> JMHResultTransformer(inFile = conf.inFile, outFile = conf.outFile, trial = conf.trial, commit = conf.projectVersion, project = conf.project)
        Command.DYNAMIC_CALL_GRAPH -> TODO("dynamic call graph not implemented")
        Command.STATIC_CALL_GRAPH ->
            CGCommand(
                    cgPrinter = SimplePrinter(FileOutputStream(conf.outFile)),
                    cgExec = WalaSCG(
                            algo = WalaRTA(),
                            entrypoints = CGEntrypoints(
                                    mf = JarBenchFinder(inPath),
                                    // TODO: add to cmd param whether to use Single or Multi CGEntryPoints
                                    ea = MultiCGEntrypoints(),
                                    me = BenchmarkWithSetupTearDownEntrypoints()
                            ),
                            inclusions = inclusions(conf.project),
                            // TODO: add to cmd param which reflection option to use
                            reflectionOptions = AnalysisOptions.ReflectionOptions.ONE_FLOW_TO_CASTS_APPLICATION_GET_METHOD
                    ),
                    jar = inPath
            )
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
    o.addOption(Option.builder(optionProject)
            .argName(optionProjectLong)
            .hasArg(true)
            .desc("Project to be parsed")
            .required()
            .build()
    )
    o.addOption(Option.builder(optionProjectVersion)
            .argName(optionProjectVersionLong)
            .hasArg(true)
            .desc("Version of the project")
            .required()
            .build()
    )
    o.addOption(Option.builder(optionTrial)
            .argName(optionTrialLong)
            .hasArg(true)
            .desc("Trial the results belong to")
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

        val trial: Either<Int, String> = try {
            val opVal = cl.getOptionValue(optionTrial)
            Either.left(if (opVal.isNullOrBlank()) {
                0
            } else {
                opVal.toInt()
            })
        } catch (e: NumberFormatException) {
            Either.right("Could not parse trial: ${e.message}")
        }

        if (trial.isRight()) {
            println(appendLineBreak(trial.right().get()))
            printUsage(o)
            return null
        }

        val project = cl.getOptionValue(optionProject) ?: ""
        val projectVersion = cl.getOptionValue(optionProjectVersion) ?: ""

        return Config(
                command = cmd.left().get(),
                inFile = inFile.left().get(),
                outFile = outFile.left().get(),
                project = project,
                projectVersion = projectVersion,
                trial = trial.left().get()
        )
    } catch(e: Exception) {
        println(e.message + "\n")
        printUsage(o)
        return null
    }
}

fun inclusions(str: String): WalaSCGInclusions =
        if (str.isBlank()) {
            IncludeAll
        } else {
            IncludeOnly(str.split(",").toSet())
        }

fun appendLineBreak(str: String): String =
    str + "\n"

fun printUsage(o: Options) {
    val hf = HelpFormatter()
    hf.printHelp("bencher", o)
}
