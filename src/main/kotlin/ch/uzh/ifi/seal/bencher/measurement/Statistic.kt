package ch.uzh.ifi.seal.bencher.measurement

interface Statistic<In, Out> {
    val name: String
    fun statistic(values: List<In>): Out
}

object Mean : Statistic<Int, Double> {

    override val name: String
        get() = "mean"

    override fun statistic(values: List<Int>): Double =
        if (values.isEmpty()) {
            0.0
        } else {
            Sum.statistic(values) / values.size
        }
}

object Sum : Statistic<Int, Double> {

    override val name: String
        get() = "sum"

    override fun statistic(values: List<Int>): Double =
        values.fold(0.0) { acc, n -> acc + n }
}
