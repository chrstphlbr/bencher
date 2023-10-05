package ch.uzh.ifi.seal.bencher.analysis.weight

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Constants
import ch.uzh.ifi.seal.bencher.MF
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitMethod
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
) : CoverageUnitWeighter {

    private val read = mutableMapOf<CoverageUnitWeightMapper, CoverageUnitWeights>()

    override fun weights(mapper: CoverageUnitWeightMapper): Either<String, CoverageUnitWeights> {
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


    private fun read(): Either<String, CoverageUnitWeights> {
        file.use {
            it.bufferedReader(charset).use { r ->
                val formatBuilder = CSVFormat.Builder.create()
                    .setDelimiter(del)
                if (hasHeader) {
                    formatBuilder.setHeader()
                } else if (hasParams) {
                    formatBuilder.setHeader(
                        CSVMethodWeightConstants.clazz,
                        CSVMethodWeightConstants.method,
                        CSVMethodWeightConstants.params,
                        CSVMethodWeightConstants.value
                    )
                } else {
                    formatBuilder.setHeader(
                        CSVMethodWeightConstants.clazz,
                        CSVMethodWeightConstants.method,
                        CSVMethodWeightConstants.value
                    )
                }

                val headerFormat = formatBuilder.build()

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
                            CoverageUnitMethod(
                                MF.plainMethod(
                                    clazz = c,
                                    name = m,
                                    params = params
                                )
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
    }

    private fun params(s: String?): List<String> =
            if (s == null || s.isBlank()) {
                listOf()
            } else {
                s.split(",")
            }
}
