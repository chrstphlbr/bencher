package ch.uzh.ifi.seal.bencher.measurement

import arrow.core.Either
import arrow.core.getOrHandle
import ch.uzh.ifi.seal.bencher.*
import org.apache.commons.csv.CSVFormat
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

interface PerformanceChangesReader {
    fun read(input: InputStream): Either<String, PerformanceChanges>
}

interface MemorizingPerformanceChangesReader : PerformanceChangesReader {
    val defaultId: String
        get() = "DEFAULT"

    fun readAndMemorize(input: InputStream, id: String): Either<String, PerformanceChanges>
    fun get(id: String): Either<String, PerformanceChanges>
}

class CSVPerformanceChangesReader(
    private val hasHeader: Boolean = true,
    private val del: Char = ';',
    private val charset: String = Constants.defaultCharset
) : MemorizingPerformanceChangesReader {

    private val memorizedPerformanceChanges = mutableMapOf<String, PerformanceChanges>()

    private fun readToList(input: InputStream): Either<String, List<PerformanceChange>> {
        val r = BufferedReader(InputStreamReader(input, charset))
        val format = CSVFormat.DEFAULT
            .withDelimiter(del)
            .withIgnoreEmptyLines()
        val headerFormat = if (hasHeader) {
            format.withHeader()
        } else {
            format.withHeader(*Header.fullHeader)
        }

        try {
            val p = headerFormat.parse(r)
            val performanceChanges = p.records.mapNotNull rec@{ rec ->
                val id = rec.get(Header.id) ?: return@rec null
                val nameStr = rec.get(Header.name) ?: return@rec null
                val functionParamsStr = rec.get(Header.functionParams) ?: return@rec null
                val perfParamsStr = rec.get(Header.perfParams) ?: return@rec null
                val v1Str = rec.get(Header.v1) ?: return@rec null
                val v2Str = rec.get(Header.v2) ?: return@rec null
                val minStr = rec.get(Header.min) ?: return@rec null
                val maxStr = rec.get(Header.max) ?: return@rec null
                val typeStr = rec.get(Header.type) ?: return@rec null

                val bench = parseBenchmark(id, nameStr, functionParamsStr, perfParamsStr).getOrHandle {
                    return Either.Left("could not parse benchmark for bench '$id': $it")
                }
                val v1 = Version.from(v1Str).getOrHandle {
                    return Either.Left("could not parse v1 for bench '$id': $it")
                }
                val v2 = Version.from(v2Str).getOrHandle {
                    return Either.Left("could not parse v2 for bench '$id': $it")
                }
                val type = PerformanceChangeType.from(typeStr).getOrHandle {
                    return Either.Left("could not parse type for bench '$id': $it")
                }

                PerformanceChange(
                    benchmark = bench,
                    v1 = v1,
                    v2 = v2,
                    type = type,
                    min = minStr.toInt(),
                    max = maxStr.toInt()
                )
            }

            return Either.Right(performanceChanges)
        } catch (e: IOException) {
            return Either.Left("Could not parse CSV file: ${e.message}")
        } catch (e: NumberFormatException) {
            return Either.Left("Could not parse value into Int: ${e.message}")
        } finally {
            r.close()
        }
    }

    private fun parseBenchmark(id: String, method: String, functionParams: String, perfParams: String): Either<String, Benchmark> {
        // TODO handle groups

        val lastDot = method.indexOfLast { it == '.' }

        val clazz = method.substring(0, lastDot)
        val name = method.substring(lastDot, method.length)

        val fps: List<String> = functionParams.split(",")

        val pps = parseJMHParams(perfParams)

        return Either.Right(MF.benchmark(
            clazz = clazz,
            name = name,
            params = fps,
            jmhParams = pps
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
            const val id = "id"
            const val name = "name"
            const val functionParams = "function_params"
            const val perfParams = "perf_params"
            const val v1 = "v1"
            const val v2 = "v2"
            const val min = "min"
            const val max = "max"
            const val type = "type"

            val fullHeader = arrayOf(id, name, functionParams, perfParams, v1, v2, min, max, type)
        }
    }
}
