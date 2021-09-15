package ch.uzh.ifi.seal.bencher.analysis.callgraph

import arrow.core.None
import arrow.core.Option
import arrow.core.getOrHandle
import ch.uzh.ifi.seal.bencher.CommandExecutor
import java.nio.file.Path

class CGCommand(
        private val cgExec: CGExecutor,
        private val cgPrinter: CGPrinter,
        private val jar: Path
) : CommandExecutor {

    override fun execute(): Option<String> {
        val r = cgExec.get(jar).getOrHandle {
            return Option(it)
        }
        cgPrinter.print(r)
        return None
    }
}
