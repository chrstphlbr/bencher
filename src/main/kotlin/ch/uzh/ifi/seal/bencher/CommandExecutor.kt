package ch.uzh.ifi.seal.bencher

import org.funktionale.option.Option
import java.io.File

interface CommandExecutor {
    fun execute(): Option<String>
}

abstract class BaseCommandExecutor(
        inFilePath: String,
        outFilePath: String
) : CommandExecutor {

    protected val inFile: File
    protected val outFile: File

    init {
        inFile = File(inFilePath)
        outFile = File(outFilePath)
    }
}