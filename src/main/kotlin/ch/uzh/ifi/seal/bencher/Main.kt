package ch.uzh.ifi.seal.bencher

import ch.uzh.ifi.seal.bencher.cli.CommandMain
import picocli.CommandLine
import java.time.Duration
import java.time.LocalDateTime
import java.util.*


fun main(args: Array<String>) {
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
    val start = System.nanoTime()
    val err = exec.execute()
    val dur = System.nanoTime() - start
    if (err.isDefined()) {
        println("Execution failed with '${err.get()}' in ${dur}ns")
    }
    println("Finished command execution in ${dur}ns")
}
