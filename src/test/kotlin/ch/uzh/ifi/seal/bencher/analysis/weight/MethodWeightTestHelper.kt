package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper

object MethodWeightTestHelper {

    val coreAmWeight = Pair(JarTestHelper.CoreA.m, 1.0)
    val coreBmWeight = Pair(JarTestHelper.CoreB.m, 2.0)
    val coreCmWeight = Pair(JarTestHelper.CoreC.m, 3.0)
    val coreDmWeight = Pair(JarTestHelper.CoreD.m, 4.0)
    val coreEmn1Weight = Pair(JarTestHelper.CoreE.mn1_1, 5.0)
    val coreEmn2Weight = Pair(JarTestHelper.CoreE.mn2, 6.0)

    val coreAmParams = JarTestHelper.CoreA.m.copy(params = listOf("java.lang.String"))
    val coreBmParams = JarTestHelper.CoreB.m.copy(params = listOf("java.lang.String", "int", "double"))
    val coreCmParams = JarTestHelper.CoreC.m.copy(params = listOf("java.lang.String", "int", "double[]"))
    val coreDmParams = JarTestHelper.CoreD.m.copy(params = listOf("java.lang.String", "java.lang.Integer", "java.lang.Double[]"))
    val coreEmParams = JarTestHelper.CoreE.mn1_1.copy(params = listOf())

    fun csvPrios(del: Char, withParams: Boolean = false): String =
            if (withParams) {
                """
                org.sample.core.CoreA${del}m${del}java.lang.String${del}1
                org.sample.core.CoreB${del}m${del}java.lang.String,int,double${del}2
                org.sample.core.CoreC${del}m${del}java.lang.String,int,double[]${del}3
                org.sample.core.CoreD${del}m${del}java.lang.String,java.lang.Integer,java.lang.Double[]${del}4
                org.sample.core.CoreE${del}mn1${del}${del}5
                """.trimIndent()
            } else {
                """
                org.sample.core.CoreA${del}m${del}1
                org.sample.core.CoreB${del}m${del}2
                org.sample.core.CoreC${del}m${del}3
                org.sample.core.CoreD${del}m${del}4
                org.sample.core.CoreE${del}mn1${del}5
                """.trimIndent()
            }

    fun csvPriosWithHeader(del: Char, withParams: Boolean, firstHeader: String, vararg restHeader: String): String =
            restHeader.fold(firstHeader) { acc, he -> "$acc$del$he" } + "\n${csvPrios(del, withParams)}"

    fun csvLine(m: Method, w: Double) =
            "${m.clazz};${m.name};${m.params.joinToString(separator = ",")};$w"


    val doubleFun: (Double) -> Double = { it * 2 }
    val doubleMapper = ValueMethodWeightMapper(doubleFun)
}
