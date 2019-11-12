package ch.uzh.ifi.seal.bencher.analysis.finder.shared

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class StateObjectManagerTest {
    @Test
    fun noParams() {
        val som = StateObjectManager()
        val jmhParams = mutableMapOf("str1" to mutableListOf("1", "2"))
        val params = listOf<String>()

        val res = som.getBenchmarkJmhParams(jmhParams, params)
        Assertions.assertEquals(jmhParams, res)
    }

    @Test
    fun onlyParams1() {
        val som = StateObjectManager()
        val jmhParams = mutableMapOf<String, MutableList<String>>()
        val params = listOf("org.sample.Test")

        val jmhParamsTest1 = mutableMapOf("str1" to mutableListOf("1", "2"))
        val bfs = listOf(BenchField(true, jmhParamsTest1), BenchField(false, mutableMapOf()))

        som.add("org.sample.Test", bfs)

        val res = som.getBenchmarkJmhParams(jmhParams, params)
        Assertions.assertEquals(jmhParamsTest1, res)
    }

    @Test
    fun onlyParams2() {
        val som = StateObjectManager()
        val jmhParams = mutableMapOf<String, MutableList<String>>()
        val params = listOf("org.sample.Test")

        val jmhParamsTest1 = mutableMapOf("str1" to mutableListOf("1", "2"))
        val jmhParamsTest2 = mutableMapOf("str2" to mutableListOf("3", "4"))
        val bfs = listOf(BenchField(true, jmhParamsTest1), BenchField(true, jmhParamsTest2))

        som.add("org.sample.Test", bfs)

        val res = som.getBenchmarkJmhParams(jmhParams, params)
        Assertions.assertEquals(2, res.size)
        Assertions.assertEquals(mutableListOf("1", "2"), res["str1"])
        Assertions.assertEquals(mutableListOf("3", "4"), res["str2"])
    }

    @Test
    fun onlyParamsOverlapping1() {
        val som = StateObjectManager()
        val jmhParams = mutableMapOf<String, MutableList<String>>()
        val params = listOf("org.sample.TestA", "org.sample.TestB")

        val jmhParamsTest1 = mutableMapOf("str1" to mutableListOf("1", "2"))
        val bfs1 = listOf(BenchField(true, jmhParamsTest1))
        som.add("org.sample.TestA", bfs1)

        val jmhParamsTest2 = mutableMapOf("str1" to mutableListOf("3", "4"))
        val bfs2 = listOf(BenchField(true, jmhParamsTest2))
        som.add("org.sample.TestB", bfs2)

        val res = som.getBenchmarkJmhParams(jmhParams, params)
        Assertions.assertEquals(1, res.size)
        Assertions.assertEquals(mutableListOf("1", "2"), res["str1"])
    }

    @Test
    fun onlyParamsOverlapping2() {
        val som = StateObjectManager()
        val jmhParams = mutableMapOf<String, MutableList<String>>()
        val params = listOf("org.sample.TestB", "org.sample.TestA")

        val jmhParamsTest1 = mutableMapOf("str1" to mutableListOf("1", "2"))
        val bfs1 = listOf(BenchField(true, jmhParamsTest1))
        som.add("org.sample.TestA", bfs1)

        val jmhParamsTest2 = mutableMapOf("str1" to mutableListOf("3", "4"))
        val bfs2 = listOf(BenchField(true, jmhParamsTest2))
        som.add("org.sample.TestB", bfs2)

        val res = som.getBenchmarkJmhParams(jmhParams, params)
        Assertions.assertEquals(1, res.size)
        Assertions.assertEquals(mutableListOf("3", "4"), res["str1"])
    }

    @Test
    fun overlappingParamAndStateObject() {
        val som = StateObjectManager()
        val jmhParams = mutableMapOf("str1" to mutableListOf("1", "2"))
        val params = listOf("org.sample.Test")

        val jmhParamsTest1 = mutableMapOf("str1" to mutableListOf("3", "4"))
        val bfs1 = listOf(BenchField(true, jmhParamsTest1))
        som.add("org.sample.Test", bfs1)

        val res = som.getBenchmarkJmhParams(jmhParams, params)
        Assertions.assertEquals(1, res.size)
        Assertions.assertEquals(mutableListOf("3", "4"), res["str1"])
    }

    @Test
    fun complex1() {
        val som = StateObjectManager()
        val jmhParams = mutableMapOf("str1" to mutableListOf("5", "6"))
        val params = listOf("org.sample.TestA", "org.sample.TestB")

        val jmhParamsTest1 = mutableMapOf("str1" to mutableListOf("1", "2"))
        val bfs1 = listOf(BenchField(true, jmhParamsTest1))
        som.add("org.sample.TestA", bfs1)

        val jmhParamsTest2 = mutableMapOf("str1" to mutableListOf("3", "4"))
        val bfs2 = listOf(BenchField(true, jmhParamsTest2))
        som.add("org.sample.TestB", bfs2)

        val res = som.getBenchmarkJmhParams(jmhParams, params)
        Assertions.assertEquals(1, res.size)
        Assertions.assertEquals(mutableListOf("1", "2"), res["str1"])
    }

    @Test
    fun complex2() {
        val som = StateObjectManager()
        val jmhParams = mutableMapOf("str1" to mutableListOf("5", "6"))
        val params = listOf("org.sample.TestA", "org.sample.TestB")

        val jmhParamsTest1 = mutableMapOf("str2" to mutableListOf("1", "2"))
        val bfs1 = listOf(BenchField(true, jmhParamsTest1))
        som.add("org.sample.TestA", bfs1)

        val jmhParamsTest2 = mutableMapOf("str3" to mutableListOf("3", "4"))
        val bfs2 = listOf(BenchField(true, jmhParamsTest2))
        som.add("org.sample.TestB", bfs2)

        val res = som.getBenchmarkJmhParams(jmhParams, params)
        Assertions.assertEquals(3, res.size)
        Assertions.assertEquals(mutableListOf("5", "6"), res["str1"])
        Assertions.assertEquals(mutableListOf("1", "2"), res["str2"])
        Assertions.assertEquals(mutableListOf("3", "4"), res["str3"])
    }

    @Test
    fun stateObjectHasJmhParamEmpty() {
        val som = StateObjectManager()
        val actual = som.hasStateObjectJmhParam("not.exists", "param")
        val expected = false
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun stateObjectHasJmhParamExist() {
        val jmhParam = "str2"
        val stateObjectClassName = "com.StateObj"

        val som = StateObjectManager()
        val jmhParamsTest = mutableMapOf(jmhParam to mutableListOf("1", "2"))
        val bfs = listOf(BenchField(true, jmhParamsTest))
        som.add(stateObjectClassName, bfs)

        val actual = som.hasStateObjectJmhParam(stateObjectClassName, jmhParam)
        val expected = true
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun stateObjectHasJmhParamNotExist1() {
        val jmhParam = "str2"
        val stateObjectClassName = "com.StateObj"

        val som = StateObjectManager()
        val jmhParamsTest = mutableMapOf(jmhParam to mutableListOf("1", "2"))
        val bfs = listOf(BenchField(true, jmhParamsTest))
        som.add(stateObjectClassName, bfs)

        val actual = som.hasStateObjectJmhParam(stateObjectClassName, jmhParam + "invalid")
        val expected = false
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun stateObjectHasJmhParamNotExist2() {
        val jmhParam = "str2"
        val stateObjectClassName = "com.StateObj"

        val som = StateObjectManager()
        val jmhParamsTest = mutableMapOf(jmhParam to mutableListOf("1", "2"))
        val bfs = listOf(BenchField(true, jmhParamsTest))
        som.add(stateObjectClassName, bfs)

        val actual = som.hasStateObjectJmhParam(stateObjectClassName + "invalid", jmhParam)
        val expected = false
        Assertions.assertEquals(expected, actual)
    }
}