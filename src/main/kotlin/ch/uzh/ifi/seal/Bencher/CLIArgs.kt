package ch.uzh.ifi.seal.Bencher


data class Config(val command: Command,
                  val inFile: String,
                  val outFile: String,
                  val project: String)

enum class Command(val strRep: String) {
    PARSE_JMH_RESULTS("parse_jmh_results");

    companion object {
        private val map = Command.values().associateBy(Command::strRep);
        fun fromStr(type: String) = map[type]
    }

    override fun toString(): String {
        return strRep
    }
}
