package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.descriptorToParamList
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor

class AsmBenchClassVisitor(api: Int, cv: ClassVisitor?, private val className: String) : ClassVisitor(api, cv) {
    private val benchs: MutableSet<Benchmark> = mutableSetOf()

    // sub visitor
    private val mvs: MutableList<AsmBenchMethodVisitor> = mutableListOf()
    private val fvs: MutableList<AsmBenchFieldVisitor> = mutableListOf()

    fun benchs(): Set<Benchmark> = benchs

    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<String>?): MethodVisitor? {
        val mv = cv?.visitMethod(access, name, descriptor, signature, exceptions)

        val jmhMv = AsmBenchMethodVisitor(
                api = api,
                mv = mv,
                name = name,
                descriptor = descriptor
        )
        mvs.add(jmhMv)
        return jmhMv
    }

    override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor? {
        val fv = AsmBenchFieldVisitor(
                api = api,
                fv = cv?.visitField(access, name, descriptor, signature, value),
                name = name
        )

        fvs.add(fv)

        return fv
    }

    override fun visitEnd() {
        cv?.visitEnd()

        val jmhParams: List<Pair<String, String>> =
                fvs.filter { it.isParam() }.flatMap { fv ->
                    fv.params().flatMap { (name, values) ->
                        values.map { value ->
                            Pair(name, value)
                        }
                    }
                }

        mvs.forEach { m ->
            if (m.isBench()) {
                val oParams = descriptorToParamList(m.descriptor)
                val params: List<String> =  if (!oParams.isEmpty()) {
                    oParams.get()
                } else {
                    listOf()
                }
                val bench = Benchmark(
                        clazz = className,
                        name = m.name,
                        params = params,
                        jmhParams = jmhParams
                )

                benchs.add(bench)
            }
        }
    }
}
