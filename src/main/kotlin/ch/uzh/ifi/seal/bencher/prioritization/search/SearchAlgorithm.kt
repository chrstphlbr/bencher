package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.algorithm.Algorithm
import org.uma.jmetal.algorithm.multiobjective.ibea.IBEA
import org.uma.jmetal.algorithm.multiobjective.mocell.MOCellBuilder
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder
import org.uma.jmetal.operator.crossover.CrossoverOperator
import org.uma.jmetal.operator.crossover.impl.PMXCrossover
import org.uma.jmetal.operator.mutation.MutationOperator
import org.uma.jmetal.operator.mutation.impl.PermutationSwapMutation
import org.uma.jmetal.operator.selection.SelectionOperator
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection
import org.uma.jmetal.problem.Problem
import org.uma.jmetal.solution.permutationsolution.PermutationSolution
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive

interface SearchAlgorithmCreator {
    fun create(
        problem: Problem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions
    ): Algorithm<List<PermutationSolution<Int>>>
}

sealed class SearchAlgorithm(
    private val crossoverProbability: Double = 0.9,
    protected val populationSize: Int = 250,
    protected val maxIterations: Int = 100,
    protected val maxEvaluations: Int = populationSize * maxIterations
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

abstract class ArchiveBasedSearchAlgorithm(
    protected val archiveSize: Int = 500  // 2 * population size (250)
) : SearchAlgorithm()

class IBEA : ArchiveBasedSearchAlgorithm() {
    override fun create(
        problem: Problem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions
    ): Algorithm<List<PermutationSolution<Int>>> = IBEA(
        problem,
        populationSize,
        archiveSize,
        maxEvaluations,
        selectionOperator(),
        crossoverOperator(),
        mutationOperator(options.numberOfBenchmarks)
    )
}

class MOCell : SearchAlgorithm() {
    override fun create(
        problem: Problem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions
    ): Algorithm<List<PermutationSolution<Int>>> {
        val builder = MOCellBuilder(
            problem,
            crossoverOperator(),
            mutationOperator(options.numberOfBenchmarks)
        )
            .setMaxEvaluations(maxEvaluations)
            .setPopulationSize(populationSize)
            .setSelectionOperator(selectionOperator())

        return builder.build()
    }
}

class NSGAII : SearchAlgorithm() {
    override fun create(
        problem: Problem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions
    ): Algorithm<List<PermutationSolution<Int>>> {
        val builder = NSGAIIBuilder(
            problem,
            crossoverOperator(),
            mutationOperator(options.numberOfBenchmarks),
            populationSize
        )
            .setMaxEvaluations(maxEvaluations)
            .setSelectionOperator(selectionOperator())

        return builder.build()
    }
}

class NSGAIII : SearchAlgorithm() {
    override fun create(
        problem: Problem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions
    ): Algorithm<List<PermutationSolution<Int>>> {
        val builder = NSGAIIIBuilder(problem)
            .setCrossoverOperator(crossoverOperator())
            .setSelectionOperator(selectionOperator())
            .setPopulationSize(populationSize)
            .setMutationOperator(mutationOperator(options.numberOfBenchmarks))
            .setMaxIterations(maxIterations)

        return builder.build()
    }
}

class PAES : ArchiveBasedSearchAlgorithm() {
    override fun create(
        problem: Problem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions
    ): Algorithm<List<PermutationSolution<Int>>> {
        val archive = CrowdingDistanceArchive<PermutationSolution<Int>>(archiveSize)
        return org.uma.jmetal.algorithm.multiobjective.paes.PAES(
            problem,
            maxEvaluations,
            archive,
            mutationOperator(options.numberOfBenchmarks)
        )
    }
}

class SPEA2 : SearchAlgorithm() {
    override fun create(
        problem: Problem<PermutationSolution<Int>>,
        options: SearchAlgorithmOptions
    ): Algorithm<List<PermutationSolution<Int>>> {
        val builder = SPEA2Builder(
            problem,
            crossoverOperator(),
            mutationOperator(options.numberOfBenchmarks)
        )
            .setPopulationSize(populationSize)
            .setMaxIterations(maxIterations)
            .setSelectionOperator(selectionOperator())

        return builder.build()
    }
}

data class SearchAlgorithmOptions(
    val numberOfBenchmarks: Int
)
