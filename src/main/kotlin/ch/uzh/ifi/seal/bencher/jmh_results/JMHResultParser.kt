package ch.uzh.ifi.seal.bencher.jmh_results

import com.beust.klaxon.JsonArray
import com.beust.klaxon.KlaxonException
import com.beust.klaxon.Parser
import org.funktionale.either.Either
import java.io.File
import java.io.FileInputStream

class JMHResultParser(
        inFile: File,
        private val project: String,
        private val commit: String,
        private val instance: String,
        private val trial: Int
) : Iterable<BenchmarkResult> {

    private val json: JsonArray<*>

    init {
        val f = FileInputStream(inFile)

        try {
            val j = Parser().parse(f) ?: throw KlaxonException("Could not parse file '${inFile.absolutePath}'")
            if (j is JsonArray<*>) {
                json = j
            } else {
                throw KlaxonException("Invalid json: no array at root")
            }
        } finally {
            f.close()
        }
    }

    override fun iterator(): Iterator<BenchmarkResult> = JMHResultIterator(json)

    fun parseAll(): Either<String, JMHResult> = Either.right(
            JMHResult(
                    project = project,
                    commit = commit,
                    trial = trial,
                    instance = instance,
                    benchmarks = iterator().asSequence().toList()
            )
    )
}
