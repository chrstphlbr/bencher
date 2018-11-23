package ch.uzh.ifi.seal.bencher.jmh_results

import ch.uzh.ifi.seal.bencher.BaseCommandExecutor
import org.funktionale.option.Option
import java.io.InputStream
import java.io.OutputStream

class JMHResultTransformer(
        inStream: InputStream,
        outStream: OutputStream,
        val project: String,
        val commit: String,
        val instance: String,
        val trial: Int,
        private val repeatHistogramValues: Boolean = false,
        private val async: Boolean = true
) : BaseCommandExecutor(inStream, outStream) {

    override fun execute(): Option<String> =
            if (!async) {
                batch()
            } else {
                stream()
            }

    private fun batch(): Option<String> {
        val p = JMHResultParser(inStream, project, commit, instance, trial)
        val res = p.parseAll()
        if (res.isLeft()) {
            return res.left().toOption()
        }

        val rw = JSONResultPrinter(outStream, repeatHistogramValues = repeatHistogramValues)
        rw.printAll(res.right().get())

        return Option.empty()
    }

    private fun stream(): Option<String> {
        try {
            val p = JMHResultParser(inStream, project, commit, instance, trial)
            val rw = JSONResultPrinter(outStream, repeatHistogramValues = repeatHistogramValues)
            rw.printHeader()
            p.forEach {
                rw.print(project, commit, instance, trial, it)
            }
            rw.done()
            return Option.empty()
        } catch (e: Exception) {
            return Option.Some(e.toString())
        }
    }
}
