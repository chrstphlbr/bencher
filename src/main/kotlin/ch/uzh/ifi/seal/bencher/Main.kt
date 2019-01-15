package ch.uzh.ifi.seal.bencher

import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGCommand
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CachedCGExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.SimplePrinter
import ch.uzh.ifi.seal.bencher.analysis.callgraph.SimpleReader
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.*
import ch.uzh.ifi.seal.bencher.analysis.finder.AsmBenchFinder
import ch.uzh.ifi.seal.bencher.analysis.finder.BenchmarkFinder
import ch.uzh.ifi.seal.bencher.analysis.weight.CSVMethodWeightTransformer
import ch.uzh.ifi.seal.bencher.analysis.weight.CSVMethodWeighter
import ch.uzh.ifi.seal.bencher.jmh_results.JMHResultTransformer
import ch.uzh.ifi.seal.bencher.selection.PrioritizationCommand
import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import java.io.*

// cli commands
val cmdTransform = "trans"
val cmdTransformJMHResult = "jmhResult"
val cmdTransformCSVWeights = "csvWeights"
val cmdSCG = "scg"
val cmdPrio = "prio"


fun main(args: Array<String>) {
    val cmd = CommandMain()
    val scg = CommandSCG()
    val prio = CommandPrioritize()
    // transform commands
    val transform = CommandTransform()
    val transformJMHResult = CommandTransformJMHResult()
    val transformCSVWeights = CommandTransformCSVWeights()

    val scgCmd = JCommander.newBuilder()
            .addObject(scg)
            .build()

    val prioCmd = JCommander.newBuilder()
            .addObject(prio)
            .build()

    val transCmd = JCommander.newBuilder()
            .addCommand(cmdTransformJMHResult, transformJMHResult)
            .addCommand(cmdTransformCSVWeights, transformCSVWeights)
            .build()

    val jc = JCommander.newBuilder()
            .programName("bencher")
            .addObject(cmd)
            .addCommand(cmdTransform, transCmd)
            .addCommand(cmdSCG, scgCmd)
            .addCommand(cmdPrio, prioCmd)
            .build()

    try {
        jc.parse(*args)
    } catch (e: ParameterException) {
        println(e.message)
        println()
        e.usage()
        return
    }

    if (cmd.help) {
        jc.usage()
        return
    }

    val out = if (cmd.out != null) {
        // fine here, because cmd.out won't change
        val outFile = cmd.out as File
        try {
            FileOutputStream(outFile)
        } catch (e: FileNotFoundException) {
            println("Could not create output file \'${outFile.absolutePath}\'")
            return
        }
    } else {
        System.out
    }

    val exec: CommandExecutor = when (jc.parsedCommand) {
        cmdTransform -> when (transCmd.parsedCommand) {
            cmdTransformJMHResult -> createCommandTransformJMHResult(cmd, transformJMHResult, out)
            cmdTransformCSVWeights -> createCommandTransformCSVWeights(cmd, transformCSVWeights, out)
            else -> {
                println("Invalid transform command: ${transCmd.parsedCommand}")
                return
            }
        }
        cmdSCG -> createCommandSCG(cmd, scg, out)
        cmdPrio -> createCommandPrio(cmd, prio, out)
        else -> {
            println("Invalid command: ${jc.parsedCommand}")
            return
        }
    }

    val err = exec.execute()
    if (err.isDefined()) {
        println("Execution failed with '${err.get()}'")
    }
}


fun createCommandTransformJMHResult(cmd: CommandMain, cmdTransform: CommandTransformJMHResult, out: OutputStream): CommandExecutor =
        JMHResultTransformer(
                inStream = FileInputStream(cmdTransform.file),
                outStream = out,
                instance = cmd.instance,
                trial = cmd.trial,
                commit = cmd.version,
                project = cmd.project
        )

fun createCommandTransformCSVWeights(cmd: CommandMain, cmdTransform: CommandTransformCSVWeights, out: OutputStream): CommandExecutor =
        CSVMethodWeightTransformer(
                jar = cmdTransform.jar.toPath(),
                methodWeighter = CSVMethodWeighter(
                        file = FileInputStream(cmdTransform.weights),
                        hasParams = true,
                        hasHeader = true
                ),
                output = out,
                walaSCGAlgo = WalaRTA(),
                walaSCGInclusions = cmdTransform.scg.inclusions,
                reflectionOptions = cmdTransform.scg.reflectionOptions,
                packagePrefix = cmd.packagePrefix
)

fun createCommandSCG(cmd: CommandMain, cmdSCG: CommandSCG, out: OutputStream): CommandExecutor =
        CGCommand(
            cgPrinter = SimplePrinter(out),
            cgExec = walaSCGExecutor(AsmBenchFinder(jar = cmdSCG.jar, pkgPrefix = cmd.packagePrefix), cmdSCG.scg),
            jar = cmdSCG.jar.toPath()
        )

fun walaSCGExecutor(bf: BenchmarkFinder, scg: ParametersSCG): WalaSCG {
    val epsAssembler: EntrypointsAssembler = if (scg.sep) {
        SingleCGEntrypoints()
    } else {
        MultiCGEntrypoints()
    }

    return WalaSCG(
            algo = WalaRTA(),
            entrypoints = CGEntrypoints(
                    mf = bf,
                    ea = epsAssembler,
                    me = BenchmarkWithSetupTearDownEntrypoints()
            ),
            inclusions = scg.inclusions,
            reflectionOptions = scg.reflectionOptions
    )
}

fun createCommandPrio(cmd: CommandMain, cmdPrio: CommandPrioritize, out: OutputStream): CommandExecutor {
    val benchFinder = AsmBenchFinder(jar = cmdPrio.v2, pkgPrefix = cmd.packagePrefix)

    val cgExecutor = CachedCGExecutor(if (cmdPrio.callGraphFile == null) {
        walaSCGExecutor(benchFinder, cmdPrio.scg)
    } else {
        SimpleReader()
    })

    val weights = if (cmdPrio.weights != null) {
        FileInputStream(cmdPrio.weights)
    } else {
        null
    }

    return PrioritizationCommand(
            out = out,
            project = cmd.project,
            version = cmd.version,
            pkgPrefix = cmd.packagePrefix,
            type = cmdPrio.type,
            v1 = cmdPrio.v1.toPath(),
            v2 = cmdPrio.v2.toPath(),
            benchFinder = benchFinder,
            cgExecutor = cgExecutor,
            weights = weights,
            changeAware = cmdPrio.changeAware,
            timeBudget = cmdPrio.timeBudget,
            jmhParams = cmdPrio.jmhParams.execConfig()
    )
}
