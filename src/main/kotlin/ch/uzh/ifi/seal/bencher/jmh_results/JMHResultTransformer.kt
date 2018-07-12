package ch.uzh.ifi.seal.bencher.jmh_results

import ch.uzh.ifi.seal.bencher.BaseCommandExecutor
import org.funktionale.option.Option

class JMHResultTransformer(
        inFile: String,
        outFile: String,
        val project: String,
        val commit: String,
        val trial: Int
) : BaseCommandExecutor(inFile, outFile) {

    override fun execute(): Option<String> {
        val p = JMHResultParser(inFile, project, commit, trial)
        val res = p.parse()
        if (res.isLeft()) {
            return res.left().toOption()
        }

        val rw = JSONResultPrinter(outFile.outputStream())
        rw.print(res.right().get())

        return Option.empty()
    }
}
