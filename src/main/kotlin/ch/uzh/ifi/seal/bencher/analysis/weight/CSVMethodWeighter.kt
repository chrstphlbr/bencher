package ch.uzh.ifi.seal.bencher.analysis.weight

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Constants
import ch.uzh.ifi.seal.bencher.MF
import org.apache.commons.csv.CSVFormat
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

class CSVMethodWeighter(
        private val file: InputStream,
        val hasHeader: Boolean = false,
        val hasParams: Boolean = true,
        val del: Char = ';',
        val charset: Charset = Constants.defaultCharset
) : MethodWeighter {

    private val read = mutableMapOf<MethodWeightMapper, MethodWeights>()

    override fun weights(mapper: MethodWeightMapper): Either<String, MethodWeights> {
        val w = read[mapper]
        if (w != null) {
            return Either.Right(w)
        }
        return read().map {
            val weights = mapper.map(it)
            read[mapper] = weights
            weights
        }
    }


    private fun read(): Either<String, MethodWeights> {
        file.bufferedReader(charset).use { r ->
            val format = CSVFormat.DEFAULT.withDelimiter(del)
            val headerFormat = if (hasHeader) {
                format.withHeader()
            } else if (hasParams) {
                format.withHeader(CSVMethodWeightConstants.clazz, CSVMethodWeightConstants.method, CSVMethodWeightConstants.params, CSVMethodWeightConstants.value)
            } else {
                format.withHeader(CSVMethodWeightConstants.clazz, CSVMethodWeightConstants.method, CSVMethodWeightConstants.value)
            }

            try {
                val p = headerFormat.parse(r)
                val methodPrios = p.records.mapNotNull rec@{ rec ->
                    val c = rec.get(CSVMethodWeightConstants.clazz) ?: return@rec null
                    val m = rec.get(CSVMethodWeightConstants.method) ?: return@rec null
                    val vStr = rec.get(CSVMethodWeightConstants.value) ?: return@rec null
                    val params = if (hasParams) {
                        params(rec.get(CSVMethodWeightConstants.params))
                    } else {
                        listOf()
                    }

                    Pair(
                        MF.plainMethod(
                            clazz = c,
                            name = m,
                            params = params
                        ),
                        vStr.toDouble()
                    )
                }.toMap()

                return Either.Right(methodPrios)
            } catch (e: IOException) {
                return Either.Left("Could not parse CSV file: ${e.message}")
            } catch (e: NumberFormatException) {
                return Either.Left("Could not parse value into double: ${e.message}")
            }
        }
    }

    private fun params(s: String?): List<String> =
            if (s == null || s.isBlank()) {
                listOf()
            } else {
                s.split(",")
            }
}
