package ch.uzh.ifi.seal.bencher

import arrow.core.Option
import arrow.core.Some
import java.io.InputStream
import java.io.OutputStream

interface CommandExecutor {
    fun execute(): Option<String>
}

class FailingCommandExecutor(val reason: String) : CommandExecutor {
    override fun execute(): Option<String> = Some(reason)
}

abstract class BaseCommandExecutor(
        protected val inStream: InputStream,
        protected val outStream: OutputStream
) : CommandExecutor
