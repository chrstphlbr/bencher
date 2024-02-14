package ch.uzh.ifi.seal.bencher.cli

import ch.uzh.ifi.seal.bencher.JavaSettings
import picocli.CommandLine

internal class MixinJava {
    @CommandLine.Option(
        names = ["--java-home"],
        description = ["sets JAVA_HOME for the underlying java calls"]
    )
    var javaHome: String? = null

    @CommandLine.Option(
        names = ["--jvm-args"],
        description = ["sets the JVM arguments for the underlying java calls"]
    )
    var jvmArgs: String? = null

    fun javaSettings(): JavaSettings = JavaSettings(home = javaHome, jvmArgs = jvmArgs)
}
