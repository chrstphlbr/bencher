package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import com.ibm.wala.ipa.callgraph.AnalysisCache
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import com.ibm.wala.ipa.callgraph.AnalysisScope
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ipa.callgraph.impl.Util
import com.ibm.wala.ipa.cha.IClassHierarchy
import com.ibm.wala.util.NullProgressMonitor


interface WalaSCGAlgo {
    fun cg(opt: AnalysisOptions, scope: AnalysisScope, cache: AnalysisCache, ch: IClassHierarchy): CallGraph
}

class WalaRTA : WalaSCGAlgo {
    override fun cg(opt: AnalysisOptions, scope: AnalysisScope, cache: AnalysisCache, ch: IClassHierarchy): CallGraph {
        val rtaBuilder = Util.makeRTABuilder(opt, cache, ch, scope)
        return rtaBuilder.makeCallGraph(opt, NullProgressMonitor())
    }
}
