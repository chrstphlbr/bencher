package ch.uzh.ifi.seal.bencher

import ch.uzh.ifi.seal.bencher.cli.CommandMain
import picocli.CommandLine
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    println("Start Bencher")
    printArgs(args)
    val startBencher = System.nanoTime()

    val cmdMain = CommandMain()
    val cmd = CommandLine(cmdMain)

    val exec = executeAndGetResult(cmd, args)

    if (cmdMain.execute) {
        println("Start command execution")
        val startCmd = System.nanoTime()
        val err = exec.execute()
        val durCmd = System.nanoTime() - startCmd
        err.map {
            println("Execution failed with '$it' in ${durCmd}ns")
        }
        println("Finished command execution in ${durCmd}ns")
    } else {
        println("do not execute")
    }

    val durBencher = System.nanoTime() - startBencher
    println("Finished Bencher in ${durBencher}ns")
}

fun executeAndGetResult(cmd: CommandLine, args: Array<String>): CommandExecutor {
    val exitCode = cmd.execute(*args)
    if (exitCode != 0) {
        System.err.println("command execution returned error $exitCode")
        exitProcess(exitCode)
    }

    val topExecutor = cmd.getExecutionResult<CommandExecutor>()
    if (topExecutor != null) {
        return topExecutor
    }

    var parseResult = cmd.parseResult
    while (parseResult.subcommand() != null) {
        parseResult = parseResult.subcommand()
    }

    val subcmd = parseResult.commandSpec().commandLine()

    if (subcmd == null) {
        System.err.println("could not retrieve command execution result")
        exitProcess(1)
    }

    return subcmd.getExecutionResult()
}

private fun printArgs(args: Array<String>) {
    println(
        args.joinToString(" ") {
            if (it.contains(" ")) {
                "\"$it\""
            } else {
                it
            }
        }
    )
}
