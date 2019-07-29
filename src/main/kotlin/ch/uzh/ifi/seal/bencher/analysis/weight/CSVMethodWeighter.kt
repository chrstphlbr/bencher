package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.Constants
import ch.uzh.ifi.seal.bencher.MF
import org.apache.commons.csv.CSVFormat
import org.funktionale.either.Either
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class CSVMethodWeighter(
        private val file: InputStream,
        val hasHeader: Boolean = false,
        val hasParams: Boolean = true,
        val del: Char = ';',
        val charset: String = Constants.defaultCharset
) : MethodWeighter {

    private val read = mutableMapOf<MethodWeightMapper, MethodWeights>()

    override fun weights(mapper: MethodWeightMapper): Either<String, MethodWeights> {
        val w = read[mapper]
        return if (w == null) {
            val eWeights = read()
            if (eWeights.isRight()) {
                val weights = mapper.map(eWeights.right().get())
                read[mapper] = weights
                Either.right(weights)
            } else {
                eWeights
            }
        } else {
            Either.right(w)
        }
    }


    private fun read(): Either<String, MethodWeights> {
        val r = BufferedReader(InputStreamReader(file, charset))
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

            return Either.right(methodPrios)
        } catch (e: IOException) {
            return Either.left("Could not parse CSV file: ${e.message}")
        } catch (e: NumberFormatException) {
            return Either.left("Could not parse value into double: ${e.message}")
        }
    }

    private fun params(s: String?): List<String> =
            if (s == null || s.isBlank()) {
                listOf()
            } else {
                s.split(",")
            }
}
