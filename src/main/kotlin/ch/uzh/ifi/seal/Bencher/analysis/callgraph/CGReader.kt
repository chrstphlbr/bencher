package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Constants
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.PossibleMethod
import org.funktionale.either.Either
import org.funktionale.option.Option
import java.io.*
import java.nio.file.Path

interface CGReader {
    fun read(input: InputStream): Either<String, CGResult>
}

class SimpleReader(
        val indent: String = SimplePrinter.defaultIndent,
        val charset: String = Constants.defaultCharset
) : CGReader, CGExecutor {

    override fun get(jar: Path): Either<String, CGResult> = read(FileInputStream(jar.toFile()))

    override fun read(input: InputStream): Either<String, CGResult> {
        val r = createReader(input)

        val res = mutableMapOf<Benchmark, Iterable<MethodCall>>()

        lateinit var currentBench: Benchmark
        lateinit var mcs: MutableList<MethodCall>
        var inBench = false

        lines@ for (l in r.lines()) {
            if (l == SimplePrinter.benchStart) {
                if (inBench) {
                    // add previous benchmark calls to res
                    res[currentBench] = mcs
                }
                // initialize empty MethodCall list
                mcs = mcList()
                inBench = false
                continue@lines
            }

            if (!inBench) {
                // first line that indicates benchmark
                val optBench = parseBench(l)
                if (optBench.isEmpty()) {
                    return Either.left("Could not parse into Benchmark: $l")
                }
                currentBench = optBench.get()
                inBench = true
                continue@lines
            }

            val optMethod = parseMethod(l)
            if (optMethod.isEmpty()) {
                return Either.left("Could not parse into Method: $l")
            }

            mcs.add(optMethod.get())
        }

        // add last benchmark
        res[currentBench] = mcs

        return Either.right(CGResult(benchCalls = res))
    }

    private fun createReader(input: InputStream): BufferedReader =
            BufferedReader(InputStreamReader(input, charset))

    private fun mcList(): MutableList<MethodCall> = mutableListOf()

    private fun parseBench(l: String): Option<Benchmark> {
        if (!l.startsWith(benchStart)) {
            // not a benchmark line
            return Option.empty()
        }

        val md = parseMethodDetails(l, benchStart)
        val opmd = plainMethodDetails(md)
        if (opmd.isEmpty()) {
            return Option.empty()
        }
        val pmd = opmd.get()

        // jmh parameters
        val jmhParamStr = md[paramJmhParams] ?: return Option.empty()

        val eJmhParams = parseJmhParam(jmhParamStr)
        if (eJmhParams.isLeft()) {
            return Option.empty()
        }

        return Option.Some(Benchmark(
                clazz = pmd.first,
                name = pmd.second,
                params = pmd.third,
                jmhParams = eJmhParams.right().get()
        ))
    }

    private fun parseMethod(l: String): Option<MethodCall> {
        if (l.isBlank()) {
            return Option.empty()
        }

        val plainStartPos = l.indexOf(plainMethodStart)
        val possibleStartPos = l.indexOf(possibleMethodStart)

        return when {
            plainStartPos >= 0 -> parsePlainMethod(
                    line = l.substring(plainStartPos),
                    level = plainStartPos / indent.length
            )
            possibleStartPos >= 0 -> parsePossibleMethod(
                    line = l.substring(possibleStartPos),
                    level = possibleStartPos / indent.length
            )
            else -> // neither plain nor possible method
                Option.empty()
        }
    }

    private fun parsePlainMethod(line: String, level: Int): Option<MethodCall> {
        val omd = plainMethodDetails(parseMethodDetails(line, plainMethodStart))
        if (omd.isEmpty()) {
            return Option.empty()
        }
        val md = omd.get()

        return Option.Some(MethodCall(
                method = PlainMethod(
                        clazz = md.first,
                        name = md.second,
                        params = md.third
                ),
                level = level
            )
        )
    }

    private fun parsePossibleMethod(line: String, level: Int): Option<MethodCall> {
        val md = parseMethodDetails(line, plainMethodStart)
        val opmd = plainMethodDetails(md)
        if (opmd.isEmpty()) {
            return Option.empty()
        }
        val pmd = opmd.get()

        // nrPossibleTargets
        val nrPossibleTargets = try {
            md[paramNrPossibleTargets]?.toInt() ?: return Option.empty()
        } catch (e: NumberFormatException) {
            return Option.empty()
        }


        // idPossibleTargets
        val idPossibleTargets = try {
            md[paramIdPossibleTargets]?.toInt() ?: return Option.empty()
        } catch (e: NumberFormatException) {
            return Option.empty()
        }

        return Option.Some(MethodCall(
                method = PossibleMethod(
                        clazz = pmd.first,
                        name = pmd.second,
                        params = pmd.third,
                        nrPossibleTargets = nrPossibleTargets,
                        idPossibleTargets = idPossibleTargets
                ),
                level = level
            )
        )
    }

    private fun parseMethodParam(p: String): List<String> = strListParams(p)

    // p is of similar format: "[(str, 1), (str, 2), (str, 3)]"
    private fun parseJmhParam(p: String): Either<String, List<Pair<String, String>>> {
        if (p == emptyList) {
            return Either.right(listOf())
        }
        val splitted = p
                .replace(paramBracesOpen.toString(), "")
                .replace(paramBracesClosed.toString(), "")
                .replace(paramListStart.toString(), "")
                .replace(paramListEnd.toString(), "")
                .split(paramDelimiter)
        val s = splitted.size
        if (s%2 == 1) {
            return Either.left("Could not parse jmhParam ($p) into pairs, because of inequal number of elements ($s)")
        }

        // iterate over half of the splitted list to transform every pair into an actual Pair type
        return Either.right((0 until s step 2).map { i ->
            Pair(splitted[i], splitted[i+1])
        })
    }

    private fun strListParams(p: String): List<String> {
        if (p == emptyList) {
            return emptyList()
        }
        return p.substring(1, p.length - 1).split(paramDelimiter)
    }

    private fun plainMethodDetails(md: Map<String, String>): Option<Triple<String, String, List<String>>> {
        // class name
        val c = md[paramClazz] ?: return Option.empty()

        // method name
        val m = md[paramMethod] ?: return Option.empty()

        // params
        val pStr = md[paramParams] ?: return Option.empty()
        val p = parseMethodParam(pStr)

        return Option.Some(Triple(c, m, p))
    }

    private fun parseMethodDetails(line: String, startKw: String): Map<String, String> {
        // remove start keyword (Benchmark, PlainMethod, PossibleMethod) and trailing ')'
        val paramStr = line.substring(startKw.length, line.length)

        var b = StringBuilder()
        var listLevel = 0
        var bracesLevel = 0
        var currentKey = ""
        val paramMap = mutableMapOf<String, String>()
        paramStr.forEach char@{ c ->
            when (c) {
                ' ' -> if (listLevel == 0) {
                    // param level
                    return@char
                } else {
                    // sub-part of param
                    b.append(c)
                }
                paramAssignOp -> {
                    currentKey = b.toString()
                    b = StringBuilder()
                    listLevel = 0
                }
                paramDelimiterChar -> if (listLevel == 0) {
                    // next param
                    paramMap[currentKey] = b.toString()
                    b = StringBuilder()
                } else {
                    // next sub-part of param
                    b.append(c)
                }
                paramListStart -> {
                    listLevel += 1
                    b.append(c)
                }
                paramListEnd -> {
                    listLevel -= 1
                    b.append(c)
                }
                paramBracesOpen -> {
                    bracesLevel += 1
                    if (bracesLevel > 1) {
                        b.append(c)
                    }
                }
                paramBracesClosed -> {
                    bracesLevel -= 1
                    if (bracesLevel >= 1) {
                        // sub-part of param
                        b.append(c)
                    } else if (bracesLevel == 0) {
                        // last param
                        paramMap[currentKey] = b.toString()
                    }
                }
                else -> b.append(c)
            }
        }

        return paramMap
    }

    companion object {
        private val benchStart = "Benchmark"
        private val plainMethodStart = "PlainMethod"
        private val possibleMethodStart = "PossibleMethod"
        private val paramDelimiter = ", "
        private val paramDelimiterChar = ','
        private val paramBracesOpen = '('
        private val paramBracesClosed = ')'
        private val paramAssignOp = '='
        private val paramListStart = '['
        private val paramListEnd = ']'
        private val paramClazz = "clazz"
        private val paramMethod = "name"
        private val paramParams = "params"
        private val paramJmhParams = "jmhParams"
        private val paramNrPossibleTargets = "nrPossibleTargets"
        private val paramIdPossibleTargets = "idPossibleTargets"
        private val emptyList = "[]"
    }
}
