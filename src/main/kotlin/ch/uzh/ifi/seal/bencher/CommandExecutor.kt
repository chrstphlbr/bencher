package ch.uzh.ifi.seal.bencher

import org.funktionale.option.Option
import java.io.InputStream
import java.io.OutputStream

interface CommandExecutor {
    fun execute(): Option<String>
}

class FailingCommandExecutor(val reason: String) : CommandExecutor {
    override fun execute(): Option<String> = Option.Some(reason)
}

abstract class BaseCommandExecutor(
        protected val inStream: InputStream,
        protected val outStream: OutputStream
) : CommandExecutor
