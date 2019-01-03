package ch.uzh.ifi.seal.bencher

import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.IncludeAll
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.IncludeOnly
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaSCGInclusions
import ch.uzh.ifi.seal.bencher.execution.JMHCLIArgs
import ch.uzh.ifi.seal.bencher.execution.parseJMHCLIParameter
import ch.uzh.ifi.seal.bencher.selection.PrioritizationType
import com.beust.jcommander.*
import com.beust.jcommander.converters.FileConverter
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import java.io.File
import java.time.Duration
import java.time.format.DateTimeParseException


class CommandMain {
    @Parameter(names = ["-p", "--project"], description = "project name", required = true)
    var project: String = ""

    @Parameter(names = ["-pv", "--project-version"], description = "project version")
    var version: String = ""

    @Parameter(names = ["-i", "--instance"], description = "instance ID")
    var instance: String = ""

    @Parameter(names = ["-t", "--trial"], description = "trial ID")
    var trial: Int = 0

    @Parameter(names = ["-out", "--out-file"], description = "output file path", converter = FileConverter::class)
    var out: File? = null

    @Parameter(names = ["-pf", "--package-prefix"], description = "project package prefix")
    var packagePrefix: String = ""

    @Parameter(names = ["--help"], description = "displays CLI usage", help = true)
    var help: Boolean = false
}

class CommandTransform

class CommandTransformJMHResult {
    @Parameter(
            names = ["-f", "--file"],
            description = "file path",
            required = true,
            validateWith = [FileExistsValidator::class, FileIsFileValidator::class],
            converter = FileConverter::class
    )
    lateinit var file: File
}

class CommandTransformCSVWeights {
    @Parameter(
            names = ["-w", "--weights"],
            description = "method-weights file path",
            required = true,
            validateWith = [FileExistsValidator::class, FileIsFileValidator::class],
            converter = FileConverter::class
    )
    var weights: File? = null

    @Parameter(
            names = ["-f", "--file"],
            description = "jar file path",
            required = true,
            validateWith = [FileExistsValidator::class, FileIsFileValidator::class],
            converter = FileConverter::class
    )
    lateinit var jar: File

    @ParametersDelegate
    var scg = ParametersSCG()
}

class ParametersSCG {
    @Parameter(
            names = ["-inc", "--inclusions"],
            description = "WALA package-prefix inclusions",
            converter = WalaSCGInclusionsConverter::class
    )
    var inclusions: WalaSCGInclusions = IncludeAll

    @Parameter(
            names = ["-ro", "--reflection-options"],
            description = "WALA reflection options",
            converter = ReflectionOptionsConverter::class
    )
    var reflectionOptions: AnalysisOptions.ReflectionOptions = AnalysisOptions.ReflectionOptions.FULL

    @Parameter(names = ["-sep", "--single-entry-points"], description = "single entry point for call-graph construction")
    var sep: Boolean = false
}

class CommandSCG {
    @Parameter(
            names = ["-f", "--file"],
            description = "jar file path",
            required = true,
            validateWith = [FileExistsValidator::class],
            converter = FileConverter::class
    )
    lateinit var jar: File

    @ParametersDelegate
    var scg = ParametersSCG()
}

class CommandPrioritize {
    @Parameter(names = ["-ca", "--change-aware"], description = "sets change-awareness of prioritization")
    var changeAware: Boolean = false

    @Parameter(
            names = ["-w", "-weights"],
            description = "method-weights file path",
            validateWith = [FileExistsValidator::class, FileIsFileValidator::class],
            converter = FileConverter::class
    )
    var weights: File? = null

    @Parameter(names = ["-jmh", "--jmh-cli-parameters"], description = "JMH command-line parameters", converter = JMHCLIArgsConverter::class)
    var jmhParams: JMHCLIArgs = JMHCLIArgs()

    @Parameter(
            names = ["-v1", "--version-1"],
            description = "file path to old version\'s JAR",
            required = true,
            validateWith = [FileExistsValidator::class, FileIsFileValidator::class],
            converter = FileConverter::class
    )
    lateinit var v1: File

    @Parameter(
            names = ["-v2", "--version-2"],
            description = "file path to new version\'s JAR",
            required = true,
            validateWith = [FileExistsValidator::class, FileIsFileValidator::class],
            converter = FileConverter::class
    )
    lateinit var v2: File

    @Parameter(
            names = ["-pt", "--prioritization-type"],
            description = "prioritization type",
            converter = PrioritizationTypeConverter::class
    )
    var type: PrioritizationType = PrioritizationType.TOTAL

    @Parameter(
            names = ["-tb", "--time-budget"],
            description = "time budget for running benchmarks",
            validateWith = [DurationValidator::class],
            converter = DurationConverter::class
    )
    var timeBudget: Duration = Duration.ZERO

    @ParametersDelegate
    var scg = ParametersSCG()

    @Parameter(
            names = ["-cgf", "--callgraph-file"],
            description = "path to callgraph file",
            validateWith = [FileExistsValidator::class, FileIsFileValidator::class],
            converter = FileConverter::class
    )
    var callGraphFile: File? = null
}


class ReflectionOptionsConverter : IStringConverter<AnalysisOptions.ReflectionOptions> {
    override fun convert(value: String?): AnalysisOptions.ReflectionOptions {
        if (value == null) {
            return AnalysisOptions.ReflectionOptions.FULL
        }

        return try {
            AnalysisOptions.ReflectionOptions.valueOf(value)
        } catch (e: IllegalArgumentException) {
            AnalysisOptions.ReflectionOptions.FULL
        }
    }
}

class PrioritizationTypeConverter : IStringConverter<PrioritizationType> {
    override fun convert(value: String?): PrioritizationType {
        if (value == null) {
            return PrioritizationType.TOTAL
        }

        return try {
            PrioritizationType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            PrioritizationType.TOTAL
        }
    }
}

class DurationConverter : IStringConverter<Duration> {
    override fun convert(value: String?): Duration {
        if (value == null) {
            return Duration.ZERO
        }

        return Duration.parse(value)
    }
}

class WalaSCGInclusionsConverter : IStringConverter<WalaSCGInclusions> {
    override fun convert(value: String?): WalaSCGInclusions =
            if (value == null || value.isBlank()) {
                IncludeAll
            } else {
                IncludeOnly(value.split(",").toSet())
            }
}

class JMHCLIArgsConverter : IStringConverter<JMHCLIArgs> {
    override fun convert(value: String?): JMHCLIArgs {
        if (value == null) {
            return JMHCLIArgs()
        }
        return parseJMHCLIParameter(value)
    }
}

class FileExistsValidator : IParameterValidator {
    override fun validate(name: String?, value: String?) {
        if (value == null) {
            throw ParameterException("No file path provided for \'$name\'")
        }

        val f = File(value)
        if (!f.exists()) {
            throw ParameterException("File does not exist for '$name': \'$value\'")
        }
    }
}

class FileIsFileValidator : IParameterValidator {
    override fun validate(name: String?, value: String?) {
        if (value == null) {
            throw ParameterException("No file path provided for \'$name\'")
        }

        val f = File(value)
        if (!f.isFile) {
            throw ParameterException("Not a file for '$name': \'$value\'")
        }
    }
}

class DurationValidator : IParameterValidator {
    override fun validate(name: String?, value: String?) {
        if (value == null) {
            throw ParameterException("No duration provided for \'$name\'")
        }

        try {
            Duration.parse(value)
        } catch (e: DateTimeParseException) {
            throw ParameterException(e)
        }
    }
}
