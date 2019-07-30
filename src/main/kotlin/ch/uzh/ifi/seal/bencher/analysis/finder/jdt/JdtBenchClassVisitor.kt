package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.TypeDeclaration

class JdtBenchClassVisitor : ASTVisitor() {
    private lateinit var fullyQualifiedClassName: String

    //private val benchs: MutableSet<Benchmark> = mutableSetOf()
    // TODO setups & tearDowns
    //private lateinit var classExecConfig: Option<ExecutionConfiguration>
    //private val benchExecInfos: MutableMap<Benchmark, ExecutionConfiguration> = mutableMapOf()

    // sub visitor
    private val mvs: MutableList<JdtBenchmarkMethodVisitor> = mutableListOf()

    //fun benchs(): Set<Benchmark> = benchs
    // returns Some iff benchs.size > 0
    //fun classExecInfo(): Option<ExecutionConfiguration> = classExecConfig
    //fun benchExecInfos(): Map<Benchmark, ExecutionConfiguration> = benchExecInfos

    // TODO not public?
    override fun visit(node: TypeDeclaration): Boolean {
        val binding = node.resolveBinding()
        // TODO inner classes correct?
        fullyQualifiedClassName = binding.qualifiedName

        node.methods.forEach {
            visit(it)
        }

        return super.visit(node)
    }

    override fun visit(node: MethodDeclaration): Boolean {
        val mv = JdtBenchmarkMethodVisitor()
        mv.visit(node)
        mvs.add(mv)
        return super.visit(node)
    }
}