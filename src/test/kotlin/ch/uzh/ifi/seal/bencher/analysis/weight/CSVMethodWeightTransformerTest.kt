package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.IncludeOnly
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaRTA
import ch.uzh.ifi.seal.bencher.fileResource
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class CSVMethodWeightTransformerTest {
    private val header = "class;method;params;value"

    private fun transformer(mw: MethodWeights, out: OutputStream): CSVMethodWeightTransformer =
            CSVMethodWeightTransformer(
                    jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath(),
                    methodWeighter = MethodWeighterMock(mw),
                    methodWeightMapper = MethodWeightTestHelper.doubleMapper,
                    output = out,
                    packagePrefixes = setOf("org.sample"),
                    walaSCGAlgo = WalaRTA(),
                    cgInclusions = IncludeOnly(setOf("org.sample")),
                    reflectionOptions = AnalysisOptions.ReflectionOptions.FULL
            )

    @Test
    fun noWeights() {
        val bos = ByteArrayOutputStream()
        val t = transformer(mapOf(), bos)

        val err = t.execute()
        if (err.isDefined()) {
            Assertions.fail<String>("Could not transform: ${err.get()}")
        }

        val out = String(bos.toByteArray())
        val lines = out.split("\n")
        Assertions.assertEquals(2, lines.size)
        // header line
        Assertions.assertEquals(header, lines[0])
        // last empty line
        Assertions.assertEquals("", lines[1])
    }

    @Test
    fun concreteMethods() {
        val ws: MethodWeights = mapOf(
                MethodWeightTestHelper.coreAmWeight,
                MethodWeightTestHelper.coreBmWeight,
                MethodWeightTestHelper.coreCmWeight,
                MethodWeightTestHelper.coreDmWeight
        )
        val bos = ByteArrayOutputStream()
        val t = transformer(ws, bos)

        val err = t.execute()
        if (err.isDefined()) {
            Assertions.fail<String>("Could not transform: ${err.get()}")
        }

        val out = String(bos.toByteArray())
        val lines = out.split("\n")

        Assertions.assertEquals(6, lines.size)
        // header line
        Assertions.assertEquals(header, lines[0])

        val ews: MethodWeights = mapOf(
                Pair(JarTestHelper.CoreA.m, memf(2.0)),
                Pair(JarTestHelper.CoreB.m, memf(3.0)),
                Pair(JarTestHelper.CoreC.m, memf(6.0)),
                Pair(JarTestHelper.CoreD.m, memf(5.0))
        )

        val lm = (1..4).map { lines[it] }
        ews.forEach { m, w ->
            val el = MethodWeightTestHelper.csvLine(m, w)
            if (!lm.contains(el)) {
                Assertions.fail<String>("$el not included")
            }
        }

        // last empty line
        Assertions.assertEquals("", lines[5])
    }

    @Test
    fun interfaceMethods() {
        val ws: MethodWeights = mapOf(Pair(JarTestHelper.CoreI.m, 5.0))
        val bos = ByteArrayOutputStream()
        val t = transformer(ws, bos)

        val err = t.execute()
        if (err.isDefined()) {
            Assertions.fail<String>("Could not transform: ${err.get()}")
        }

        val out = String(bos.toByteArray())
        val lines = out.split("\n")

        Assertions.assertEquals(6, lines.size)
        // header line
        Assertions.assertEquals(header, lines[0])

        val ews: MethodWeights = mapOf(
                // own weight + call from A.m
                Pair(JarTestHelper.CoreA.m, memf(10.0)),
                // own weight + call from A.m
                Pair(JarTestHelper.CoreB.m, memf(10.0)),
                // call from A.m + call from B.m
                Pair(JarTestHelper.CoreC.m, memf(10.0)),
                // own weight + call from A.m
                Pair(JarTestHelper.CoreD.m, memf(10.0))
        )

        val lm = (1..4).map { lines[it] }
        ews.forEach { m, w ->
            val el = MethodWeightTestHelper.csvLine(m, w)
            if (!lm.contains(el)) {
                Assertions.fail<String>("$el not included")
            }
        }

        // last empty line
        Assertions.assertEquals("", lines[5])
    }

    companion object {
        private val memf = MethodWeightTestHelper.doubleFun
    }
}
