package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Constants
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGExecutor
import org.apache.commons.csv.CSVFormat
import org.funktionale.either.Either
import java.io.*
import java.nio.file.Path

class CSVPrioritizer(
    file: InputStream,
    cgExecutor: CGExecutor,
    jarFile: Path,
    val hasHeader: Boolean = false,
    val hasParams: Boolean = true,
    val del: Char = ',',
    val charset: String = Constants.defaultCharset
) : ExternalPrioritizer(file, cgExecutor, jarFile) {

    override fun readPriorities(): Either<String, Map<out Method, Priority>> {
        val r = BufferedReader(InputStreamReader(file, charset))
        val format = CSVFormat.DEFAULT.withDelimiter(del)
        val headerFormat = if (hasHeader) {
            format.withHeader()
        } else if (hasParams) {
            format.withHeader(csvClass, csvMethod, csvParams, csvValue)
        } else {
            format.withHeader(csvClass, csvMethod, csvValue)
        }

        try {
            val p = headerFormat.parse(r)
            val methodPrios = p.records.mapNotNull rec@{ rec ->
                val c = rec.get(csvClass) ?: return@rec null
                val m = rec.get(csvMethod) ?: return@rec null
                val vStr = rec.get(csvValue) ?: return@rec null
                val params = if (hasParams) {
                    params(rec.get(csvParams))
                } else {
                    listOf()
                }


                Pair(
                        PlainMethod(
                                clazz = c,
                                name = m,
                                params = params
                        ),
                        Priority(
                                rank = 0,
                                total = 0,
                                value = vStr.toDouble()
                        )
                )
            }.toMap()

            return Either.right(methodPrios)
        } catch (e: IOException) {
            return Either.left("Could not parse CSV file: ${e.message}")
        } catch (e: NumberFormatException) {
            return Either.left("Could not parse value into double: ${e.message}")
        }
    }

    private fun params(s: String?): List<String> =
            if (s == null) {
                listOf()
            } else {
                //TODO: parse list from string representation when available
                listOf()
            }

    companion object {
        val csvClass = "class"
        val csvMethod = "method"
        val csvParams = "params"
        val csvValue = "value"
    }
}
