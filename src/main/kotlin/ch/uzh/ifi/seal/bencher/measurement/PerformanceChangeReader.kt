package ch.uzh.ifi.seal.bencher.measurement

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.*
import org.apache.commons.csv.CSVFormat
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

interface PerformanceChangesReader {
    fun read(input: InputStream): Either<String, PerformanceChanges>
}

interface MemorizingPerformanceChangesReader : PerformanceChangesReader {
    fun readAndMemorize(input: InputStream, id: String): Either<String, PerformanceChanges>
    fun get(id: String): Either<String, PerformanceChanges>
}

class CSVPerformanceChangesReader(
    private val hasHeader: Boolean = true,
    private val del: Char = ';',
    private val charset: Charset = Constants.defaultCharset,
) : MemorizingPerformanceChangesReader {

    private val memorizedPerformanceChanges = mutableMapOf<String, PerformanceChanges>()

    private fun readToList(input: InputStream): Either<String, List<PerformanceChange>> {
        input.bufferedReader(charset).use { r ->
            val formatBuilder = CSVFormat.Builder.create()
                .setDelimiter(del)
                .setIgnoreEmptyLines(true)
            if (hasHeader) {
                formatBuilder.setHeader()
            } else {
                formatBuilder.setHeader(*Header.fullHeader)
            }

            val headerFormat = formatBuilder.build()

            try {
                val p = headerFormat.parse(r)
                val performanceChanges = p.records.mapNotNull rec@{ rec ->
                    val id = rec.get(Header.ID) ?: return@rec null
                    val nameStr = rec.get(Header.NAME) ?: return@rec null
                    val functionParamsStr = rec.get(Header.FUNCTION_PARAMS) ?: return@rec null
                    val perfParamsStr = rec.get(Header.PERF_PARAMS) ?: return@rec null
                    val v1Str = rec.get(Header.V1) ?: return@rec null
                    val v2Str = rec.get(Header.V2) ?: return@rec null
                    val minStr = rec.get(Header.MIN) ?: return@rec null
                    val maxStr = rec.get(Header.MAX) ?: return@rec null
                    val typeStr = rec.get(Header.TYPE) ?: return@rec null

                    val bench = parseBenchmark(id, nameStr, functionParamsStr, perfParamsStr).getOrElse {
                        return Either.Left("could not parse benchmark for bench '$id': $it")
                    }
                    val v1 = Version.from(v1Str).getOrElse {
                        return Either.Left("could not parse v1 for bench '$id': $it")
                    }
                    val v2 = Version.from(v2Str).getOrElse {
                        return Either.Left("could not parse v2 for bench '$id': $it")
                    }
                    val type = PerformanceChangeType.from(typeStr).getOrElse {
                        return Either.Left("could not parse type for bench '$id': $it")
                    }

                    PerformanceChange(
                        benchmark = bench,
                        v1 = v1,
                        v2 = v2,
                        type = type,
                        min = minStr.toInt(),
                        max = maxStr.toInt(),
                    )
                }

                return Either.Right(performanceChanges)
            } catch (e: IOException) {
                return Either.Left("Could not parse CSV file: ${e.message}")
            } catch (e: NumberFormatException) {
                return Either.Left("Could not parse value into Int: ${e.message}")
            }
        }
    }

    private fun parseBenchmark(id: String, method: String, functionParams: String, perfParams: String): Either<String, Benchmark> {
        // TODO handle groups -> not necessary as long as PerformanceChanges are only matched with dynamic coverage results, because they also consider the group as the method name; only can become a problem if PerformanceChanges are matched with static coverage

        val lastDot = method.indexOfLast { it == '.' }

        val clazz = method.substring(0, lastDot)
        val name = method.substring(lastDot+1, method.length)

        val fps: List<String> = functionParams.split(",")

        val pps = parseJMHParams(perfParams)

        return Either.Right(MF.benchmark(
            clazz = clazz,
            name = name,
            params = fps,
            jmhParams = pps,
        ))
    }

    private fun parseJMHParams(str: String): JmhParameters {
        if (str.isEmpty()) {
            return listOf()
        }

        val pl = mutableListOf<Pair<String, String>>()
        val splEqu = str.split("=")

        var prevKey: String? = null
        for (i in splEqu.indices) {
            val el = splEqu[i]
            if (i == 0) {
                // first element
                prevKey = el
            } else if (i == splEqu.size-1) {
                // last element
                pl.add(Pair(prevKey!!, el))
            } else {
                val lastComma = el.indexOfLast { it == ',' }
                val v = el.substring(0, lastComma)
                pl.add(Pair(prevKey!!, v))
                prevKey = el.substring(lastComma+1)
            }
        }

        return pl
    }

    override fun readAndMemorize(input: InputStream, id: String): Either<String, PerformanceChanges> {
        val pcs = memorizedPerformanceChanges[id]
        return if (pcs == null) {
            readToList(input).map { changes ->
                val npcs = PerformanceChangesImpl(changes)
                memorizedPerformanceChanges[id] = npcs
                npcs
            }
        } else {
            Either.Right(pcs)
        }
    }

    override fun read(input: InputStream): Either<String, PerformanceChanges> =
            readToList(input).map { PerformanceChangesImpl(it) }

    override fun get(id: String): Either<String, PerformanceChanges> {
        val pcs = memorizedPerformanceChanges[id]
        return if (pcs == null) {
            Either.Left("no performance changes read for id '$id'")
        } else {
            Either.Right(pcs)
        }
    }

    companion object {
        private object Header {
            const val ID = "id"
            const val NAME = "name"
            const val FUNCTION_PARAMS = "function_params"
            const val PERF_PARAMS = "perf_params"
            const val V1 = "v1"
            const val V2 = "v2"
            const val MIN = "min"
            const val MAX = "max"
            const val TYPE = "type"

            val fullHeader = arrayOf(ID, NAME, FUNCTION_PARAMS, PERF_PARAMS, V1, V2, MIN, MAX, TYPE)
        }
    }
}
