package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.CommandExecutor
import org.funktionale.option.Option

class CGCommand(val cgExec: CGExecutor, val cgPrinter: CGPrinter) : CommandExecutor {

    override fun execute(): Option<String> {
        val r = cgExec.get()
        cgPrinter.print(r)
        return Option.empty()
    }

}