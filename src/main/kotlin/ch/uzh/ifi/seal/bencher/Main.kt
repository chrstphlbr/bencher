package ch.uzh.ifi.seal.bencher

import ch.uzh.ifi.seal.bencher.cli.CommandMain
import picocli.CommandLine


fun main(args: Array<String>) {
    println("Start Bencher")
    printArgs(args)
    val startBencher = System.nanoTime()
    val cmd = CommandLine(CommandMain())
    val parsed = cmd.parseWithHandler(CommandLine.RunLast(), args) ?: return

    if (parsed.size != 1) {
        System.err.println("Invalid number of executors: expected 1, got ${parsed.size}")
        return
    }

    if (parsed[0] !is CommandExecutor) {
        // invalid command, usage printing should have happened in run method
        System.err.println("CLI command did not return CommandExecutor")
        return
    }

    val exec = parsed[0] as CommandExecutor

    println("Start command execution")
    val startCmd = System.nanoTime()
    val err = exec.execute()
    val durCmd = System.nanoTime() - startCmd
    if (err.isDefined()) {
        println("Execution failed with '${err.get()}' in ${durCmd}ns")
    }
    println("Finished command execution in ${durCmd}ns")
    val durBencher = System.nanoTime() - startBencher
    println("Finished Bencher in ${durBencher}ns")
}

private fun printArgs(args: Array<String>) {
    println(
            args.map {
                if (it.contains(" ")) {
                    "\"$it\""
                } else {
                    it
                }
            }
            .joinToString(" ")
    )
}
