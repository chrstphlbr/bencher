package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.CommandExecutor
import org.funktionale.option.Option
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import java.time.Duration

enum class PrioritizationType {
    DEFAULT, RANDOM, TOTAL, ADDITIONAL
}

class PrioritizationCommand(
        private val out: OutputStream,
        private val project: String,
        private val version: String,
        private val v1: Path,
        private val v2: Path,
        private val type: PrioritizationType,
        private val changeAware: Boolean = false,
        private val timeBudget: Duration = Duration.ZERO,
        private val jmhParams: String = "",
        private val weights: InputStream = ByteArrayInputStream(byteArrayOf())

) : CommandExecutor {
    override fun execute(): Option<String> {
        return Option.Some("not implemented")
    }
}