package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.algorithm.Algorithm
import org.uma.jmetal.algorithm.multiobjective.ibea.IBEA
import org.uma.jmetal.algorithm.multiobjective.mocell.MOCellBuilder
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder
import org.uma.jmetal.operator.crossover.CrossoverOperator
import org.uma.jmetal.operator.crossover.impl.PMXCrossover
import org.uma.jmetal.operator.mutation.MutationOperator
import org.uma.jmetal.operator.mutation.impl.PermutationSwapMutation
import org.uma.jmetal.operator.selection.SelectionOperator
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection
import org.uma.jmetal.problem.permutationproblem.PermutationProblem
import org.uma.jmetal.solution.permutationsolution.PermutationSolution
import org.uma.jmetal.util.aggregationfunction.impl.WeightedSum
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive
import org.uma.jmetal.util.comparator.ObjectiveComparator

interface SearchAlgorithmCreator {
    val multiObjective: Boolean

    fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>>

    companion object {
        fun checkVariables(
            problem: PermutationProblem<PermutationSolution<Int>>,
            options: SearchAlgorithmOptions,
        ) {
            if (problem.numberOfVariables() != options.numberOfBenchmarks) {
                throw IllegalArgumentException("problem.numberOfVariables (${problem.numberOfVariables()}) != options.numberOfBenchmarks (${options.numberOfBenchmarks})")
            }
        }

        fun checkObjectives(
            problem: PermutationProblem<PermutationSolution<Int>>,
            options: SearchAlgorithmOptions,
        ) {
            if (problem.numberOfObjectives() != options.objectives.size) {
                throw IllegalArgumentException("problem.numberOfObjectives (${problem.numberOfObjectives()}) != options.objectives.size (${options.objectives.size})")
            }
        }
    }
}

data class RestartSearchAlgorithmCreator(
    private val creator: SearchAlgorithmCreator,
    private val restarts: Int,
) : SearchAlgorithmCreator {
    override val multiObjective: Boolean = creator.multiObjective

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions
    ): Algorithm<List<PermutationSolution<Int>>> {
        val algorithms = (0 until restarts).map { creator.create(problem, options) }
        return MultipleAlgorithmWrapper(algorithms)
    }
}

data object GreedyCreator : SearchAlgorithmCreator {
    override val multiObjective: Boolean = false

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)
        if (options.numberOfObjectives == 1) {
            SearchAlgorithmCreator.checkObjectives(problem, options)
        }

        val aggregation = if (options.numberOfObjectives > 1) {
            Aggregation(
                function = WeightedSum(true),
                weights = options.objectives.indices.map { 1.0 / options.numberOfObjectives }.toDoubleArray(),
                objectives = options.objectives,
            )
        } else {
            null
        }

        return MultipleSolutionsAlgorithmWrapper(
            Greedy(
                problem,
                options.benchmarkIdMap,
                options.objectives,
                aggregation,
            )
        )
    }
}

data object HillClimbingCreator : SearchAlgorithmCreator {
    override val multiObjective: Boolean = false

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)
        if (options.numberOfObjectives == 1 || problem.numberOfObjectives() > 1) {
            SearchAlgorithmCreator.checkObjectives(problem, options)
        }

        val initial = problem.evaluate(problem.createSolution())

        val comparator = if (problem.numberOfObjectives() > 1) {
            val aggregation = Aggregation(
                function = WeightedSum(false),
                weights = options.objectives.indices.map { 1.0 / options.objectives.size }.toDoubleArray(),
                objectives = null,
            )

            AggregateObjectiveComparator<PermutationSolution<Int>>(aggregation)
        } else {
            ObjectiveComparator(0);
        }

        return MultipleSolutionsAlgorithmWrapper(
            HillClimbing(
                initial,
                problem,
                comparator,
                PermutationNeighborhood(),
                25000, // same as for the EvolutionaryAlgorithmCreators
            )
        )
    }
}

sealed class EvolutionaryAlgorithmCreator(
    private val crossoverProbability: Double = 0.9,
    protected val populationSize: Int = 250,
    protected val maxIterations: Int = 100,
    protected val maxEvaluations: Int = populationSize * maxIterations,
) : SearchAlgorithmCreator {

    private val defaultMutationProbability = 0.1

    protected fun crossoverOperator(): CrossoverOperator<PermutationSolution<Int>> =
        PMXCrossover(crossoverProbability)

    protected fun mutationOperator(numberOfBenchmarks: Int?): MutationOperator<PermutationSolution<Int>> {
        val probability = if (numberOfBenchmarks != null) {
            1.0 / numberOfBenchmarks
        } else {
            defaultMutationProbability
        }
        return PermutationSwapMutation(probability)
    }

    protected fun selectionOperator(): SelectionOperator<List<PermutationSolution<Int>>, PermutationSolution<Int>> =
        BinaryTournamentSelection()
}

sealed class ArchiveBasedEvolutionaryAlgorithmCreator(
    protected val archiveSize: Int = 500,  // 2 * population size (250)
) : EvolutionaryAlgorithmCreator()

data object GeneticAlgorithmCreator : EvolutionaryAlgorithmCreator() {
    override val multiObjective: Boolean = false

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)
        if (problem.numberOfObjectives() != 1) {
            throw IllegalArgumentException("GeneticAlgorithm expects a single objective problem")
        }

        val builder = GeneticAlgorithmBuilder(
            problem,
            crossoverOperator(),
            mutationOperator(options.numberOfBenchmarks),
        )
            .setVariant(GeneticAlgorithmBuilder.GeneticAlgorithmVariant.GENERATIONAL)
            .setMaxEvaluations(maxEvaluations)
            .setPopulationSize(populationSize)
            .setSelectionOperator(selectionOperator())

        return MultipleSolutionsAlgorithmWrapper(builder.build())
    }
}

data object IBEACreator : ArchiveBasedEvolutionaryAlgorithmCreator() {
    override val multiObjective: Boolean = true

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)
        return IBEA(
            problem,
            populationSize,
            archiveSize,
            maxEvaluations,
            selectionOperator(),
            crossoverOperator(),
            mutationOperator(options.numberOfBenchmarks),
        )
    }
}

data object MOCellCreator : EvolutionaryAlgorithmCreator() {
    override val multiObjective: Boolean = true

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)

        val builder = MOCellBuilder(
            problem,
            crossoverOperator(),
            mutationOperator(options.numberOfBenchmarks),
        )
            .setMaxEvaluations(maxEvaluations)
            .setPopulationSize(256) // needs a population size whose square root is an integer -> 16*16 = 256 ~ 250 (population size of all other algorithms)
            .setSelectionOperator(selectionOperator())

        return builder.build()
    }
}

data object NSGAIICreator : EvolutionaryAlgorithmCreator() {
    override val multiObjective: Boolean = true

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)

        val builder = NSGAIIBuilder(
            problem,
            crossoverOperator(),
            mutationOperator(options.numberOfBenchmarks),
            populationSize,
        )
            .setMaxEvaluations(maxEvaluations)
            .setSelectionOperator(selectionOperator())

        return builder.build()
    }
}

data object NSGAIIICreator : EvolutionaryAlgorithmCreator() {
    override val multiObjective: Boolean = true

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)

        val builder = NSGAIIIBuilder(problem)
            .setCrossoverOperator(crossoverOperator())
            .setSelectionOperator(selectionOperator())
            .setPopulationSize(populationSize)
            .setMutationOperator(mutationOperator(options.numberOfBenchmarks))
            .setMaxIterations(maxIterations)

        return builder.build()
    }
}

data object PAESCreator : ArchiveBasedEvolutionaryAlgorithmCreator() {
    override val multiObjective: Boolean = true

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)
        val archive = CrowdingDistanceArchive<PermutationSolution<Int>>(archiveSize)
        return org.uma.jmetal.algorithm.multiobjective.paes.PAES(
            problem,
            maxEvaluations,
            archive,
            mutationOperator(options.numberOfBenchmarks),
        )
    }
}

data object SPEA2Creator : EvolutionaryAlgorithmCreator() {
    override val multiObjective: Boolean = true

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)
        val builder = SPEA2Builder(
            problem,
            crossoverOperator(),
            mutationOperator(options.numberOfBenchmarks),
        )
            .setPopulationSize(populationSize)
            .setMaxIterations(maxIterations)
            .setSelectionOperator(selectionOperator())

        return builder.build()
    }
}

data class SearchAlgorithmOptions(
    val benchmarkIdMap: BenchmarkIdMap,
    val objectives: List<Objective>,
) {
    val numberOfBenchmarks: Int = benchmarkIdMap.size
    val numberOfObjectives: Int = objectives.size
}
