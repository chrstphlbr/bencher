package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.*
import org.funktionale.either.Either
import org.funktionale.option.Option
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

interface CGReader {
    fun read(input: InputStream): Either<String, CGResult>
}

class SimpleCGReader(
        val charset: String = Constants.defaultCharset
) : CGReader {

    override fun read(input: InputStream): Either<String, CGResult> {
        val r = createReader(input)

        val res = mutableMapOf<Method, CG>()

        lateinit var currentBench: Benchmark
        lateinit var mcs: MutableSet<MethodCall>
        var inBench = false

        lines@ for (l in r.lines()) {
            if (l == C.cgStart) {
                if (inBench) {
                    // add previous benchmark calls to res
                    res[currentBench] = CG(
                            start = currentBench,
                            edges = mcs
                    )
                }
                // initialize empty MethodCall set
                mcs = mcSet()
                inBench = false
                continue@lines
            }

            if (!inBench) {
                // first line that indicates benchmark
                val bench = parseBench(l) ?: return Either.left("Could not parse into Benchmark: $l")
                currentBench = bench
                inBench = true
                continue@lines
            }

            val mc = parseMethodCall(l) ?: return Either.left("Could not parse into Method: $l")

            mcs.add(mc)
        }

        // add last benchmark
        res[currentBench] = CG(
                start = currentBench,
                edges = mcs
        )

        return Either.right(CGResult(calls = res))
    }

    private fun createReader(input: InputStream): BufferedReader =
            BufferedReader(InputStreamReader(input, charset))

    private fun mcSet(): MutableSet<MethodCall> = mutableSetOf()

    private fun parseBench(l: String): Benchmark? {
        if (!l.startsWith(C.benchStart)) {
            // not a benchmark line
            return null
        }

        val m = parseMethod(l, C.benchStart)

        return if (m is Benchmark) {
            m
        } else {
            null
        }
    }

    private fun parseMethodCall(l: String): MethodCall? {
        if (l.isBlank()) {
            return null
        }

        val mcElements = l.split(C.edgeLineDelimiter)
        if (mcElements.size != 4) {
            return null
        }

        val from = parseMethod(mcElements[0], C.methodStart) ?: return null
        val to = parseMethod(mcElements[3], C.methodStart) ?: return null
        val id = try {
            Integer.parseInt(mcElements[1])
        } catch (e: NumberFormatException) {
            return null
        }
        val nr = try {
            Integer.parseInt(mcElements[2])
        } catch (e: NumberFormatException) {
            return null
        }

        return MCF.methodCall(
                from = from,
                to = to,
                idPossibleTargets = id,
                nrPossibleTargets = nr
        )
    }

    private fun parseMethod(line: String, startKw: String): Method? {
        val md = parseMethodDetails(line, startKw)
        val opmd = plainMethodDetails(md)
        if (opmd.isEmpty()) {
            return null
        }
        val pmd = opmd.get()

        return if (md.containsKey(C.paramJmhParams)) {
            // jmh parameters
            val jmhParamStr = md[C.paramJmhParams] ?: return null

            val eJmhParams = parseJmhParam(jmhParamStr)
            if (eJmhParams.isLeft()) {
                return null
            }

            MF.benchmark(
                    clazz = pmd.first,
                    name = pmd.second,
                    params = pmd.third,
                    jmhParams = eJmhParams.right().get()
            )
        } else {
            MF.plainMethod(
                    clazz = pmd.first,
                    name = pmd.second,
                    params = pmd.third
            )
        }
    }

    private fun parseMethodParam(p: String): List<String> = strListParams(p)

    // p is of similar format: "[(str, 1), (str, 2), (str, 3)]"
    private fun parseJmhParam(p: String): Either<String, List<Pair<String, String>>> {
        if (p == C.emptyList) {
            return Either.right(listOf())
        }
        val splitted = p
                .replace(C.paramBracesOpen.toString(), "")
                .replace(C.paramBracesClosed.toString(), "")
                .replace(C.paramListStart.toString(), "")
                .replace(C.paramListEnd.toString(), "")
                .split(C.paramDelimiter)
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
        if (p == C.emptyList) {
            return emptyList()
        }
        return p.substring(1, p.length - 1).split(C.paramDelimiter)
    }

    private fun plainMethodDetails(md: Map<String, String>): Option<Triple<String, String, List<String>>> {
        // class name
        val c = md[C.paramClazz] ?: return Option.empty()

        // method name
        val m = md[C.paramMethod] ?: return Option.empty()

        // params
        val pStr = md[C.paramParams] ?: return Option.empty()
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
                C.paramAssignOp -> {
                    currentKey = b.toString()
                    b = StringBuilder()
                    listLevel = 0
                }
                C.paramDelimiterChar -> if (listLevel == 0) {
                    // next param
                    paramMap[currentKey] = b.toString()
                    b = StringBuilder()
                } else {
                    // next sub-part of param
                    b.append(c)
                }
                C.paramListStart -> {
                    listLevel += 1
                    b.append(c)
                }
                C.paramListEnd -> {
                    listLevel -= 1
                    b.append(c)
                }
                C.paramBracesOpen -> {
                    bracesLevel += 1
                    if (bracesLevel > 1) {
                        b.append(c)
                    }
                }
                C.paramBracesClosed -> {
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
        private val C = CGPrinterReaderConstants
    }
}
