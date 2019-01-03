package ch.uzh.ifi.seal.bencher

import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGCommand
import ch.uzh.ifi.seal.bencher.analysis.callgraph.SimplePrinter
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.*
import ch.uzh.ifi.seal.bencher.analysis.finder.AsmBenchFinder
import ch.uzh.ifi.seal.bencher.jmh_results.JMHResultTransformer
import ch.uzh.ifi.seal.bencher.selection.PrioritizationCommand
import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import java.io.*

// cli commands
val cmdParse = "parse"
val cmdSCG = "scg"
val cmdPrio = "prio"


fun main(args: Array<String>) {
    val cmd = CommandMain()
    val parse = CommandParse()
    val scg = CommandSCG()
    val prio = CommandPrioritize()

    val jc = JCommander.newBuilder()
            .programName("bencher")
            .addObject(cmd)
            .addCommand(cmdParse, parse)
            .addCommand(cmdSCG, scg)
            .addCommand(cmdPrio, prio)
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
        cmdParse -> createCommandParse(cmd, parse, out)
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


fun createCommandParse(cmd: CommandMain, cmdParse: CommandParse, out: OutputStream): CommandExecutor =
        JMHResultTransformer(
                inStream = FileInputStream(cmdParse.file),
                outStream = out,
                instance = cmd.instance,
                trial = cmd.trial,
                commit = cmd.version,
                project = cmd.project
        )

fun createCommandSCG(cmd: CommandMain, cmdSCG: CommandSCG, out: OutputStream): CommandExecutor {
    val epsAssembler: EntrypointsAssembler = if (cmdSCG.scg.sep) {
        SingleCGEntrypoints()
    } else {
        MultiCGEntrypoints()
    }

    return CGCommand(
            cgPrinter = SimplePrinter(out),
            cgExec = WalaSCG(
                    algo = WalaRTA(),
                    entrypoints = CGEntrypoints(
                            mf = AsmBenchFinder(jar = cmdSCG.jar, pkgPrefix = cmd.packagePrefix),
                            ea = epsAssembler,
                            me = BenchmarkWithSetupTearDownEntrypoints()
                    ),
                    inclusions = cmdSCG.scg.inclusions,
                    reflectionOptions = cmdSCG.scg.reflectionOptions
            ),
            jar = cmdSCG.jar.toPath()
    )
}

fun createCommandPrio(cmd: CommandMain, cmdPrio: CommandPrioritize, out: OutputStream): CommandExecutor {
    val ws = if (cmdPrio.weights != null) {
        FileInputStream(cmdPrio.weights)
    } else {
        ByteArrayInputStream(byteArrayOf())
    }

    return PrioritizationCommand(
            out = out,
            project = cmd.project,
            version = cmd.version,
            type = cmdPrio.type,
            v1 = cmdPrio.v1.toPath(),
            v2 = cmdPrio.v2.toPath(),
            changeAware = cmdPrio.changeAware,
            timeBudget = cmdPrio.timeBudget,
            jmhParams = cmdPrio.jmhParams,
            weights = ws
    )
}
