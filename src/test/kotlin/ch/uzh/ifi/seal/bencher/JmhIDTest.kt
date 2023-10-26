package ch.uzh.ifi.seal.bencher

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

class JmhIDTest {

    private lateinit var r: Random

    @BeforeEach
    fun setup() {
        r = Random(System.nanoTime())
    }

    @Test
    fun equalNames() {
        val id1 = JmhID("name1", listOf())
        val id2 = JmhID("name1", listOf())
        Assertions.assertEquals(id1, id2)
        Assertions.assertEquals(id1.hashCode(), id2.hashCode())
    }

    private fun randomName(): String {
        val n = r.nextInt(1, Int.MAX_VALUE)
        return "name$n"
    }

    @Test
    fun differentNames() {
        val id1 = JmhID("name0", listOf())

        repeat(100) {
            val id2Name = randomName()
            val id2 = JmhID(id2Name, listOf())
            Assertions.assertNotEquals(id1, id2)
            Assertions.assertNotEquals(id1.hashCode(), id2.hashCode())
        }
    }

    private fun params(): JmhParameters = listOf(
        Pair("p1", "v1"),
        Pair("p1", "v2"),
        Pair("p1", "v3"),
        Pair("p2", "v1"),
        Pair("p2", "v2"),
        Pair("p2", "v3"),
        Pair("p3", "v1"),
        Pair("p3", "v2"),
        Pair("p3", "v3"),
    )

    @Test
    fun equalParameters() {
        val id1 = JmhID("name1", params())
        val id2 = JmhID("name1", params())
        Assertions.assertEquals(id1, id2)
        Assertions.assertEquals(id1.hashCode(), id2.hashCode())
    }

    private fun randomParams(): JmhParameters {
        val nrParams = r.nextInt(1, 5)
        return (0 until nrParams).flatMap {  p ->
            val nrValues = r.nextInt(1, 5)
            (0 until nrValues).map { v ->
                Pair("p$p", "v$v")
            }
        }
    }

    @Test
    fun differentParams() {
        val id1 = JmhID("name1", params())

        repeat(100) {
            val id2Params = randomParams()
            val id2 = JmhID("name1", id2Params)
            Assertions.assertNotEquals(id1, id2)
            Assertions.assertNotEquals(id1.hashCode(), id2.hashCode())
        }
    }

    private fun randomListParams(): JmhParameters {
        val rand = r.nextInt(0, Int.MAX_VALUE)
        val params = params()
        val nrLists = 7
        return when {
            rand % nrLists == 1 -> params.toMutableList()
            rand % nrLists == 2 -> ArrayList(params)
            rand % nrLists == 3 -> CopyOnWriteArrayList(params)
            rand % nrLists == 4 -> LinkedList(params)
            rand % nrLists == 5 -> {
                val s = Stack<Pair<String, String>>()
                params.forEach { s.push(it) }
                s
            }
            rand % nrLists == 6 -> Vector(params)
            else -> params
        }
    }

    @Test
    fun differentListTypes() {
        repeat(10) {
            val id1 = JmhID("name1", randomListParams())
            val id2 = JmhID("name1", randomListParams())
            Assertions.assertEquals(id1, id2)
            Assertions.assertEquals(id1.hashCode(), id2.hashCode())
        }
    }
}
