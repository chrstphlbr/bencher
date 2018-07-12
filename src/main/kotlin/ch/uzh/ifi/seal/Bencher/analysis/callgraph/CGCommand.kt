package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.CommandExecutor
import org.funktionale.option.Option

class CGCommand(
        private val cgExec: CGExecutor<*>,
        private val cgPrinter: CGPrinter
) : CommandExecutor {

    override fun execute(): Option<String> {
        val r = cgExec.get()
        if (r.isLeft()) {
            return r.left().toOption()
        }
        cgPrinter.print(r.right().get())
        return Option.empty()
    }
}
