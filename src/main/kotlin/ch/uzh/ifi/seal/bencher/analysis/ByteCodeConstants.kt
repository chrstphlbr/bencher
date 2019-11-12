package ch.uzh.ifi.seal.bencher.analysis

object ByteCodeConstants {
    const val boolean = 'Z'
    const val byte = 'B'
    const val char = 'C'
    const val double = 'D'
    const val float = 'F'
    const val int = 'I'
    const val long = 'J'
    const val short = 'S'
    const val void = 'V'

    const val objectType = 'L'
    const val arrayType = '['

    val primitives = setOf(byte, char, double, float, int, long, short, boolean, void)

    // initializers "method" name
    const val constructor = "<init>"
    const val staticInit = "<clinit>"
}
