package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.CommandExecutor
import org.funktionale.option.Option

class CGCommand(val cgExec: CGExecutor, val cgPrinter: CGPrinter) : CommandExecutor {

    override fun execute(): Option<String> {
        val r = cgExec.get()
        if (r.isRight()) {
            return r.right().toOption()
        }
        cgPrinter.print(r.left().get())
        return Option.empty()
    }

}