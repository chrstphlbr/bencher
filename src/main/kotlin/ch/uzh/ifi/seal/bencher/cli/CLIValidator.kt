package ch.uzh.ifi.seal.bencher.cli

import picocli.CommandLine
import java.io.File


internal object FileExistsValidator {
    fun validate(spec: CommandLine.Model.CommandSpec, name: String, file: File?) {
        if (file == null) {
            throw CommandLine.ParameterException(spec.commandLine(), "No file path provided for \'$name\'")
        }

        if (!file.exists()) {
            throw CommandLine.ParameterException(spec.commandLine(), "File does not exist for '$name'")
        }
    }
}

internal object FileIsFileValidator {
    fun validate(spec: CommandLine.Model.CommandSpec, name: String, file: File?) {
        if (file == null) {
            throw CommandLine.ParameterException(spec.commandLine(), "No file path provided for \'$name\'")
        }

        if (!file.isFile) {
            throw CommandLine.ParameterException(spec.commandLine(), "Not a file for '$name'")
        }
    }
}

//internal class DurationValidator : IParameterValidator {
//    override fun validate(name: String?, value: String?) {
//        if (value == null) {
//            throw ParameterException("No duration provided for \'$name\'")
//        }
//
//        try {
//            Duration.parse(value)
//        } catch (e: DateTimeParseException) {
//            throw ParameterException(e)
//        }
//    }
//}
