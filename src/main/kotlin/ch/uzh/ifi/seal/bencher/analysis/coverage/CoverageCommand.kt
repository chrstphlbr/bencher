package ch.uzh.ifi.seal.bencher.analysis.coverage

import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.CommandExecutor
import java.nio.file.Path

class CoverageCommand(
    private val covExec: CoverageExecutor,
    private val covPrinter: CoveragePrinter,
    private val jar: Path
) : CommandExecutor {

    override fun execute(): Option<String> {
        val r = covExec.get(jar).getOrElse {
            return Option(it)
        }
        covPrinter.print(r)
        return None
    }
}
