package ch.uzh.ifi.seal.bencher.jmhResults

import com.beust.klaxon.JsonArray
import com.beust.klaxon.KlaxonException
import com.beust.klaxon.Parser
import org.funktionale.either.Either
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
