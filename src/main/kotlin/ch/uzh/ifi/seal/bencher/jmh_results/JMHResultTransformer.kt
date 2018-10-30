package ch.uzh.ifi.seal.bencher.jmh_results

import ch.uzh.ifi.seal.bencher.BaseCommandExecutor
import org.funktionale.option.Option

class JMHResultTransformer(
        inFile: String,
        outFile: String,
        val project: String,
        val commit: String,
        val trial: Int,
        private val repeatHistogramValues: Boolean = false,
        private val async: Boolean = true
) : BaseCommandExecutor(inFile, outFile) {

    override fun execute(): Option<String> =
            if (!async) {
                batch()
            } else {
                stream()
            }

    private fun batch(): Option<String> {
        val p = JMHResultParser(inFile, project, commit, trial)
        val res = p.parseAll()
        if (res.isLeft()) {
            return res.left().toOption()
        }

        val rw = JSONResultPrinter(outFile.outputStream(), repeatHistogramValues = repeatHistogramValues)
        rw.printAll(res.right().get())

        return Option.empty()
    }

    private fun stream(): Option<String> {
        try {
            val p = JMHResultParser(inFile, project, commit, trial)
            val rw = JSONResultPrinter(outFile.outputStream(), repeatHistogramValues = repeatHistogramValues)
            rw.printHeader()
            p.forEach {
                rw.print(project, commit, trial, it)
            }
            rw.done()
            return Option.empty()
        } catch (e: Exception) {
            return Option.Some(e.toString())
        }
    }
}
