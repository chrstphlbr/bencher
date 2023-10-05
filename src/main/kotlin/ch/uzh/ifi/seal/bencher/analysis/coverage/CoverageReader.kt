package ch.uzh.ifi.seal.bencher.analysis.coverage

import arrow.core.*
import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

interface CoverageReader {
    fun read(input: InputStream): Either<String, Coverages>
}

class SimpleCoverageReader(
    private val coverageUnitType: CoverageUnitType,
    val charset: Charset = Constants.defaultCharset
) : CoverageReader {

    override fun read(input: InputStream): Either<String, Coverages> {
        val res = mutableMapOf<Method, Coverage>()

        lateinit var currentBench: Benchmark
        lateinit var mcs: MutableSet<CoverageUnitResult>
        var inBench = false

        createReader(input).use { r ->
            lines@ for (l in r.lines()) {
                if (l == C.covStart) {
                    if (inBench) {
                        // add previous benchmark calls to res
                        res[currentBench] = Coverage(
                            of = currentBench,
                            unitResults = mcs
                        )
                    }
                    // initialize empty MethodCall set
                    mcs = mcSet()
                    inBench = false
                    continue@lines
                }

                if (!inBench) {
                    // first line that indicates benchmark
                    val bench = parseBench(l) ?: return Either.Left("Could not parse into Benchmark: $l")
                    currentBench = bench
                    inBench = true
                    continue@lines
                }

                val mc = parseCoverageUnitResult(currentBench.toPlainMethod(), l)
                    .getOrElse {
                        return Either.Left("Could not parse into CoverageUnitResult: $l (reason: $it)")
                    }
                    ?: // no parsing error but nothing to add
                    continue@lines

                mcs.add(mc)
            }
        }

        return try {
            // add last benchmark
            res[currentBench] = Coverage(
                of = currentBench,
                unitResults = mcs
            )

            Either.Right(Coverages(coverages = res))
        } catch (e: UninitializedPropertyAccessException) {
            // empty file
            Either.Left("Empty coverages file")
        }
    }

    private fun createReader(input: InputStream): BufferedReader =
            BufferedReader(InputStreamReader(input, charset))

    private fun mcSet(): MutableSet<CoverageUnitResult> = mutableSetOf()

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

    private fun parseCoverageUnitResult(of: Method, l: String): Either<String, CoverageUnitResult?> {
        if (l.isBlank()) {
            return Either.Left("line is blank")
        }

        val covElements = l.split(C.coverageLineDelimiter)
        if (covElements.size != 3) {
            return Either.Left("expected 3 elements but got ${covElements.size}")
        }

        val firstElement = covElements[0]

        val unit = when {
            firstElement.startsWith(C.methodStart) -> {
                if (coverageUnitType == CoverageUnitType.LINE) {
                    return Either.Right(null)
                }
                parseCoverageUnitMethod(firstElement, C.methodStart)
            }

            firstElement.startsWith(C.lineStart) -> {
                if (coverageUnitType == CoverageUnitType.METHOD) {
                    return Either.Right(null)
                }
                parseCoverageUnitLine(firstElement, C.lineStart)
            }

            else -> return Either.Left("unknown line start")
        } ?: return Either.Left("could not parse line")

        val prob = covElements[1].toDoubleOrNull() ?: return Either.Left("could not parse probability into Double")
        val level = covElements[2].toIntOrNull() ?: return Either.Left("could not parse level into Int")

        return Either.Right(
            if (prob == 1.0) {
                CUF.covered(
                        of = of,
                        unit = unit,
                        level = level
                )
            } else {
                CUF.possiblyCovered(
                        of = of,
                        unit = unit,
                        level = level,
                        probability = prob
                )
            }
        )
    }

    private fun parseCoverageUnitMethod(line: String, startKw: String): CoverageUnitMethod? {
        val m = parseMethod(line, startKw) ?: return null
        return CoverageUnitMethod(m)
    }

    private fun parseMethod(line: String, startKw: String): Method? {
        val md = parseMethodDetails(line, startKw)

        val pmd = plainMethodDetails(md).getOrElse {
            return null
        }

        return if (md.containsKey(C.paramJmhParams)) {
            // jmh parameters
            val jmhParamStr = md[C.paramJmhParams] ?: return null

            val jmhParams = parseJmhParam(jmhParamStr).getOrElse {
                return null
            }

            MF.benchmark(
                    clazz = pmd.first,
                    name = pmd.second,
                    params = pmd.third,
                    jmhParams = jmhParams
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
            return Either.Right(listOf())
        }
        val splitted = p
                .replace(C.paramBracesOpen.toString(), "")
                .replace(C.paramBracesClosed.toString(), "")
                .replace(C.paramListStart.toString(), "")
                .replace(C.paramListEnd.toString(), "")
                .split(C.paramDelimiter)
        val s = splitted.size
        if (s % 2 == 1) {
            return Either.Left("Could not parse jmhParam ($p) into pairs, because of inequal number of elements ($s)")
        }

        // iterate over half of the splitted list to transform every pair into an actual Pair type
        return Either.Right((0 until s step 2).map { i ->
            Pair(splitted[i], splitted[i + 1])
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
        val c = md[C.paramClazz] ?: return None

        // method name
        val m = md[C.paramMethod] ?: return None

        // params
        val pStr = md[C.paramParams] ?: return None
        val p = parseMethodParam(pStr)

        return Some(Triple(c, m, p))
    }

    private fun parseMethodDetails(line: String, startKw: String): Map<String, String> {
        // remove start keyword (Benchmark, Method) and trailing ')'
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

    private fun parseCoverageUnitLine(line: String, startKw: String): CoverageUnitLine? {
        val lineDetails = parseLineDetails(line, startKw)

        val file = lineDetails[C.paramFile] ?: return null

        val number = try {
            val numberStr = lineDetails[C.paramNumber] ?: return null
            numberStr.toInt()
        } catch (e: NumberFormatException) {
            return null
        }

        val mi = nullableInt(lineDetails[C.paramMissedInstructions]).getOrElse { return null }

        val ci = nullableInt(lineDetails[C.paramCoveredInstructions]).getOrElse { return null }

        val mb = nullableInt(lineDetails[C.paramMissedBranches]).getOrElse { return null }

        val cb = nullableInt(lineDetails[C.paramCoveredBranches]).getOrElse { return null }

        val cul = CoverageUnitLine(
            line = LF.line(file = file, number = number),
            missedInstructions = mi,
            coveredInstructions = ci,
            missedBranches = mb,
            coveredBranches = cb
        )

        return cul
    }

    private fun parseLineDetails(line: String, startKw: String): Map<String, String> {
        // remove keyword ("Line") and parenthesis ('(' and ')')
        val paramStr = line.substring(startKw.length + 1, line.length - 1)

        var b = StringBuilder()
        var currentKey = ""
        val paramMap = mutableMapOf<String, String>()

        paramStr.forEach char@{ c ->
            when (c) {
                ' ' -> return@char
                C.paramAssignOp -> {
                    // done with key
                    currentKey = b.toString()
                    b = StringBuilder()
                }
                C.paramDelimiterChar -> {
                    // next param
                    paramMap[currentKey] = b.toString()
                    b = StringBuilder()
                }
                else -> b.append(c)
            }
        }

        // add last element
        if (currentKey != "") {
            paramMap[currentKey] = b.toString()
        }

        return paramMap
    }

    private fun nullableInt(str: String?): Option<Int?> {
        if (str == null) {
            return None
        }

        if (str == C.valueNull) {
            return Some(null)
        }

        return try {
            Some(str.toInt())
        } catch (e: NumberFormatException) {
            None
        }
    }

    companion object {
        private val C = CoveragePrinterReaderConstants
    }
}
