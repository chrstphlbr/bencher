package ch.uzh.ifi.seal.bencher.jmhResults

import arrow.core.Either
import com.beust.klaxon.JsonArray
import com.beust.klaxon.KlaxonException
import com.beust.klaxon.Parser
import java.io.InputStream

class JMHResultParser(
        inStream: InputStream,
        private val project: String,
        private val commit: String,
        private val instance: String,
        private val trial: Int
) : Iterable<BenchmarkResult> {

    private val json: JsonArray<*>

    init {
        try {
            val j = Parser.default().parse(inStream)
            if (j is JsonArray<*>) {
                json = j
            } else {
                throw KlaxonException("Invalid json: no array at root")
            }
        } finally {
            inStream.close()
        }
    }

    override fun iterator(): Iterator<BenchmarkResult> = JMHResultIterator(json)

    fun parseAll(): Either<String, JMHResult> = Either.Right(
            JMHResult(
                    project = project,
                    commit = commit,
                    trial = trial,
                    instance = instance,
                    benchmarks = iterator().asSequence().toList()
            )
    )
}
