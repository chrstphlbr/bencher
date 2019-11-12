package ch.uzh.ifi.seal.bencher.analysis.finder.shared

import ch.uzh.ifi.seal.bencher.analysis.finder.jdt.JMHConstants
import org.apache.logging.log4j.LogManager

class StateObjectManager {
    private val log = LogManager.getLogger(StateObjectManager::class.java.canonicalName)

    private val stateObjects = mutableMapOf<String, MutableMap<String, MutableList<String>>>()

    fun all() = stateObjects

    fun add(fqn: String, bfs: List<BenchField>) {
        stateObjects[fqn] = mutableMapOf()

        bfs.filter { it.isParam }.forEach { fv ->
            fv.jmhParams.forEach { (name, values) ->
                stateObjects.getValue(fqn)[name] = values
            }
        }
    }

    fun getBenchmarkJmhParams(benchmarkJmhParams: MutableMap<String, MutableList<String>>, params: List<String>): MutableMap<String, MutableList<String>> {
        // Note: if a jmhParams occurs multiple times only the first occurrences is handled
        val ret = mutableMapOf<String, MutableList<String>>()

        // first add jmhParams in the order of the method arguments
        params.forEach { fqn ->
            val stateObject = stateObjects[fqn]
            if (stateObject == null) {
                if (!fqn.contains(JMHConstants.Package.infra)) {
                    log.warn("The state object '$fqn' was not found (State objects from external dependencies cannot be resolved)")
                }
            } else {
                stateObject.forEach { (name, values) ->
                    if (ret[name] == null) {
                        ret[name] = values
                    }
                }
            }
        }

        // second add the jmhParams of the class where the benchmark is defined
        benchmarkJmhParams.forEach { (name, values) ->
            if (ret[name] == null) {
                ret[name] = values
            }
        }

        return ret
    }

    fun hasStateObjectJmhParam(fqn: String, jmhParam: String): Boolean {
        return if (stateObjects[fqn] == null) {
            false
        } else {
            stateObjects.getValue(fqn)[jmhParam] != null
        }
    }
}