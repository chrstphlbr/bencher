package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.CommandExecutor
import org.funktionale.option.Option

class CGCommand(val cgExec: CGExecutor, val cgPrinter: CGPrinter) : CommandExecutor {
    override fun execute(): Option<String> {
        val r = cgExec.get()
        if (r.isLeft()) {
            return r.left().toOption()
        }
        cgPrinter.print(r.right().get())
        return Option.empty()
    }
}
