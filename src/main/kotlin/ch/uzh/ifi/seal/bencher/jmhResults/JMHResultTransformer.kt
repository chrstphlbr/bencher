package ch.uzh.ifi.seal.bencher.jmhResults

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.BaseCommandExecutor
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
        val res = p.parseAll().getOrElse {
            return Option(it)
        }

        val rw = JSONResultPrinter(outStream, repeatHistogramValues = repeatHistogramValues)
        rw.printAll(res)

        return None
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
            return None
        } catch (e: Exception) {
            return Some(e.toString())
        }
    }
}
