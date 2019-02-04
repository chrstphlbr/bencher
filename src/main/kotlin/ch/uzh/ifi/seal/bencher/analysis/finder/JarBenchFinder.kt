package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Constants
import ch.uzh.ifi.seal.bencher.runCommand
import org.funktionale.either.Either
import org.funktionale.option.Option
import java.io.File
import java.nio.file.Path
import java.time.Duration

class JarBenchFinder(val jar: Path) : MethodFinder<Benchmark> {

    private val defaultTimeout = Duration.ofMinutes(1)

    private var parsed = false
    private lateinit var benchmarks: List<Benchmark>

    override fun all(): Either<String, List<Benchmark>> {
        if (!parsed) {
            val generated = generateBenchs()
            if (generated.isDefined()) {
                return Either.left("Could not generate benchmarks: ${generated.get()}")
            }
            parsed = true
        }
        return Either.right(benchmarks)
    }

    private fun generateBenchs(): Option<String> {
        // execute benchmark option
        val benchs = benchs(jar.toAbsolutePath())
        if (benchs.isLeft()) {
            return Option.Some(benchs.left().get())
        }

        benchmarks = benchs.right().get()
        return Option.None
    }

    private fun benchs(jarPath: Path): Either<String, List<Benchmark>> {
        val cmd = String.format(jarCmdBenchmarksWithParams, jarPath)
        val benchsParams = executeBenchCmd(cmd)
        if (benchsParams.isRight()) {
            // got benchmarks including parameters
            return benchsParams
        }
        // bench not parsable including params
        return executeBenchCmd(String.format(jarCmdBenchmarks, jarPath))
    }

    private fun executeBenchCmd(cmd: String): Either<String, List<Benchmark>> {
        val (success, out, err) = cmd.runCommand(File(Constants.homeDir), defaultTimeout)
        if (!success) {
            // execution timed out
            return Either.left("Execution '${cmd}' timed out ")
        }

        if (err != null && err.isNotBlank()) {
            return Either.left(err)
        }

        if (out == null) {
            return Either.left("No output from '${cmd}' (and no error)")
        }

        return parseBenchs(out)
    }

    private fun parseBenchs(out: String, cmd: String = ""): Either<String, List<Benchmark>> {
        val lines = out.split("\n")

        if (lines.isEmpty()) {
            return Either.left("No output from '${cmd}' (and no error)")
        }

        if (!lines[0].startsWith(jarCmdFirstLine)) {
            return Either.left("No benchmark out:\n${out}")
        }

        var currentBench: Benchmark? = null
        val benchs = mutableListOf<Benchmark>()
        for (i in 1 until lines.size) {
            val currentLine = lines[i]

            if (currentLine.startsWith(jarCmdParamLine)) {
                // param line
                currentBench = currentBench!!.copy(jmhParams = currentBench.jmhParams + parseJmhParams(currentLine))
            } else {
                // benchmark line

                // add last bench
                if (currentBench != null) {
                    benchs.add(currentBench)
                }

                if (currentLine.isBlank()) {
                    currentBench = null
                    continue
                }

                currentBench = parseBench(currentLine)
            }
        }

        if (currentBench != null) {
            benchs.add(currentBench)
        }

        return Either.right(benchs.toList())
    }

    private fun parseBench(bench: String): Benchmark {
        val clazz = bench.substringBeforeLast(".")
        val method = bench.substringAfterLast(".")
        return Benchmark(
                clazz = clazz,
                name = method,
                params = listOf(),
                jmhParams = listOf()
        )
    }

    private fun parseJmhParams(jmhParam: String): List<Pair<String, String>> {
        if (jmhParam.isBlank()) {
            return listOf()
        }

        val paramName = jmhParam.substringAfter("\"").substringBefore("\"")
        val paramVals = jmhParam.substringAfter("{").substringBefore("}").split(",")
        return paramVals.map { Pair(paramName, it.trim()) }
    }

    companion object {
        val jarCmdBenchmarksWithParams = "java -jar %s -lp"
        val jarCmdBenchmarks = "java -jar %s -l"
        val jarCmdFirstLine = "Benchmarks:"
        val jarCmdParamLine = "  param"
    }
}
