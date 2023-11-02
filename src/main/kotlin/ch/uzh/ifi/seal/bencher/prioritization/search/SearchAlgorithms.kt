package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.algorithm.Algorithm
import org.uma.jmetal.algorithm.multiobjective.ibea.IBEA
import org.uma.jmetal.algorithm.multiobjective.mocell.MOCellBuilder
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder
import org.uma.jmetal.algorithm.singleobjective.coralreefsoptimization.CoralReefsOptimizationBuilder
import org.uma.jmetal.algorithm.singleobjective.evolutionstrategy.EvolutionStrategyBuilder
import org.uma.jmetal.algorithm.singleobjective.evolutionstrategy.EvolutionStrategyBuilder.EvolutionStrategyVariant
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

sealed interface LocalSearchAlgorithmCreator : SearchAlgorithmCreator

data object HillClimbingCreator : LocalSearchAlgorithmCreator {
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
                options.maxEvaluations,
            )
        )
    }
}

sealed interface EvolutionaryAlgorithmCreator : SearchAlgorithmCreator {

    fun crossoverOperator(probability: Double): CrossoverOperator<PermutationSolution<Int>> =
        PMXCrossover(probability)

    fun mutationOperator(probability: Double, numberOfBenchmarks: Int?): MutationOperator<PermutationSolution<Int>> {
        val p = if (numberOfBenchmarks != null) {
            1.0 / numberOfBenchmarks
        } else {
            probability
        }
        return PermutationSwapMutation(p)
    }

    fun selectionOperator(): SelectionOperator<List<PermutationSolution<Int>>, PermutationSolution<Int>> =
        BinaryTournamentSelection()
}

sealed class GeneticAlgorithmCreator : EvolutionaryAlgorithmCreator {
    override val multiObjective: Boolean = false

    abstract val variant: GeneticAlgorithmBuilder.GeneticAlgorithmVariant

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
            crossoverOperator(options.crossoverProbability),
            mutationOperator(options.mutationProbability, options.numberOfBenchmarks),
        )
            .setVariant(GeneticAlgorithmBuilder.GeneticAlgorithmVariant.GENERATIONAL)
            .setMaxEvaluations(options.maxEvaluations)
            .setPopulationSize(options.populationSize)
            .setSelectionOperator(selectionOperator())
            .setVariant(variant)

        return MultipleSolutionsAlgorithmWrapper(builder.build())
    }
}

data object GenerationalGeneticAlgorithmCreator : GeneticAlgorithmCreator() {
    override val variant: GeneticAlgorithmBuilder.GeneticAlgorithmVariant =
        GeneticAlgorithmBuilder.GeneticAlgorithmVariant.GENERATIONAL
}

data object SteadyStateGeneticAlgorithmCreator : GeneticAlgorithmCreator() {
    override val variant: GeneticAlgorithmBuilder.GeneticAlgorithmVariant =
        GeneticAlgorithmBuilder.GeneticAlgorithmVariant.STEADY_STATE
}

sealed class EvolutionStrategyCreator : EvolutionaryAlgorithmCreator {
    override val multiObjective: Boolean = false

    abstract val variant: EvolutionStrategyVariant

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)
        if (problem.numberOfObjectives() != 1) {
            throw IllegalArgumentException("EvolutionStrategy expects a single objective problem")
        }

        val builder = EvolutionStrategyBuilder(
            problem,
            mutationOperator(options.mutationProbability, options.numberOfBenchmarks),
            variant,
        )
            .setMaxEvaluations(options.maxEvaluations)

        return MultipleSolutionsAlgorithmWrapper(builder.build())
    }
}

data object ElitistEvolutionStrategyCreator : EvolutionStrategyCreator() {
    override val variant: EvolutionStrategyVariant = EvolutionStrategyVariant.ELITIST
}

data object NonElitistEvolutionStrategyCreator : EvolutionStrategyCreator() {
    override val variant: EvolutionStrategyVariant = EvolutionStrategyVariant.NON_ELITIST
}

data object CoralReefsOptimizationCreator : EvolutionaryAlgorithmCreator {
    override val multiObjective: Boolean = false

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions
    ): Algorithm<List<PermutationSolution<Int>>> {
        if (options.numberOfObjectives == 1 || problem.numberOfObjectives() > 1) {
            SearchAlgorithmCreator.checkObjectives(problem, options)
        }

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

        val builder = CoralReefsOptimizationBuilder(
            problem,
            selectionOperator(),
            crossoverOperator(options.crossoverProbability),
            mutationOperator(options.mutationProbability, options.numberOfBenchmarks),
        )
            // hyperparameters according to:
            // 1. Salcedo-Sanz et al. "The Coral Reefs Optimization Algorithm: A Novel Metaheuristic for Efficiently Solving Optimization Problems" (https://doi.org/10.1155/2014/739768)
            // 2. JMetal default implementations
            // 3. in accordance with the other search algorithms
            .setMaxEvaluations(options.maxEvaluations)
            .setComparator(comparator)
            .setRho(0.4)
            .setPd(0.05) // between 0 and 0.1
            .setFbs(0.9)
            .setFbr(0.1)
            .setFa(0.1)
            .setFd(0.1)
            .setAttemptsToSettle(10) // could not find any references
            .setM(16) // similar to MOCell -> M=16*N=16 = 256 ~ 250 (population size of all other algorithms)
            .setN(16) // similar to MOCell -> M=16*N=16 = 256 ~ 250 (population size of all other algorithms)

        return builder.build()
    }
}

sealed interface ArchiveBasedEvolutionaryAlgorithmCreator : EvolutionaryAlgorithmCreator

data object IBEACreator : ArchiveBasedEvolutionaryAlgorithmCreator {
    override val multiObjective: Boolean = true

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)
        return IBEA(
            problem,
            options.populationSize,
            options.archiveSize,
            options.maxEvaluations,
            selectionOperator(),
            crossoverOperator(options.crossoverProbability),
            mutationOperator(options.mutationProbability, options.numberOfBenchmarks),
        )
    }
}

data object MOCellCreator : ArchiveBasedEvolutionaryAlgorithmCreator {
    override val multiObjective: Boolean = true

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)

        val archive = CrowdingDistanceArchive<PermutationSolution<Int>>(options.archiveSize)

        val builder = MOCellBuilder(
            problem,
            crossoverOperator(options.crossoverProbability),
            mutationOperator(options.mutationProbability, options.numberOfBenchmarks),
        )
            .setMaxEvaluations(options.maxEvaluations)
            .setPopulationSize(options.populationSize) // needs a population size whose square root is an integer
            .setSelectionOperator(selectionOperator())
            .setArchive(archive)

        return builder.build()
    }
}

data object NSGAIICreator : EvolutionaryAlgorithmCreator {
    override val multiObjective: Boolean = true

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)

        val builder = NSGAIIBuilder(
            problem,
            crossoverOperator(options.crossoverProbability),
            mutationOperator(options.mutationProbability, options.numberOfBenchmarks),
            options.populationSize,
        )
            .setMaxEvaluations(options.maxEvaluations)
            .setSelectionOperator(selectionOperator())

        return builder.build()
    }
}

data object NSGAIIICreator : EvolutionaryAlgorithmCreator {
    override val multiObjective: Boolean = true

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)

        val builder = NSGAIIIBuilder(problem)
            .setCrossoverOperator(crossoverOperator(options.crossoverProbability))
            .setSelectionOperator(selectionOperator())
            .setPopulationSize(options.populationSize)
            .setMutationOperator(mutationOperator(options.mutationProbability, options.numberOfBenchmarks))
            .setMaxIterations(options.maxIterations)

        return builder.build()
    }
}

data object PAESCreator : ArchiveBasedEvolutionaryAlgorithmCreator {
    override val multiObjective: Boolean = true

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)
        val archive = CrowdingDistanceArchive<PermutationSolution<Int>>(options.archiveSize)
        return org.uma.jmetal.algorithm.multiobjective.paes.PAES(
            problem,
            options.maxEvaluations,
            archive,
            mutationOperator(options.mutationProbability, options.numberOfBenchmarks),
        )
    }
}

data object SPEA2Creator : EvolutionaryAlgorithmCreator {
    override val multiObjective: Boolean = true

    override fun create(
        problem: PermutationProblem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions,
    ): Algorithm<List<PermutationSolution<Int>>> {
        SearchAlgorithmCreator.checkVariables(problem, options)
        val builder = SPEA2Builder(
            problem,
            crossoverOperator(options.crossoverProbability),
            mutationOperator(options.mutationProbability, options.numberOfBenchmarks),
        )
            .setPopulationSize(options.populationSize)
            .setMaxIterations(options.maxIterations)
            .setSelectionOperator(selectionOperator())

        return builder.build()
    }
}

data class SearchAlgorithmOptions(
    val benchmarkIdMap: BenchmarkIdMap = BenchmarkIdMapImpl(listOf()),
    val objectives: List<Objective> = listOf(),
    val populationSize: Int = 250,
    val maxIterations: Int = 100,
    val maxEvaluations: Int = populationSize * maxIterations,
    val archiveSize: Int = populationSize,
    val crossoverProbability: Double = 0.9,
    val mutationProbability:Double = 0.1,
) {
    val numberOfBenchmarks: Int = benchmarkIdMap.size
    val numberOfObjectives: Int = objectives.size
}
