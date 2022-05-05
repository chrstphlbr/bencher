package ch.uzh.ifi.seal.bencher.analysis.coverage.sta

import ch.uzh.ifi.seal.bencher.Method
import com.ibm.wala.ipa.callgraph.Entrypoint
import com.ibm.wala.types.TypeReference

class EntrypointMock(m: Method) : Entrypoint(IMethodMock(m)) {
    override fun getParameterTypes(i: Int): Array<TypeReference> = arrayOf()

    override fun getNumberOfParameters(): Int = 0
}
