/*
 * Online Structure Learner by Revision (OSLR) is an online relational
 * learning algorithm that can handle continuous, open-ended
 * streams of relational examples as they arrive. We employ
 * techniques from theory revision to take advantage of the already
 * acquired knowledge as a starting point, find where it should be
 * modified to cope with the new examples, and automatically update it.
 * We rely on the Hoeffding's bound statistical theory to decide if the
 * model must in fact be updated accordingly to the new examples.
 * The system is built upon ProPPR statistical relational language to
 * describe the induced models, aiming at contemplating the uncertainty
 * inherent to real data.
 *
 * Copyright (C) 2017-2018 Victor Guimarães
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package br.ufrj.cos.engine.proppr.ground;

import br.ufrj.cos.engine.proppr.MapCleanup;
import edu.cmu.ml.proppr.Grounder;
import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.prove.Prover;
import edu.cmu.ml.proppr.prove.wam.Feature;
import edu.cmu.ml.proppr.prove.wam.ProofGraph;
import edu.cmu.ml.proppr.prove.wam.WamProgram;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.ConcurrentSymbolTable;
import edu.cmu.ml.proppr.util.StatusLogger;
import edu.cmu.ml.proppr.util.SymbolTable;
import edu.cmu.ml.proppr.util.multithreading.Multithreading;
import edu.cmu.ml.proppr.util.multithreading.Transformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import static br.ufrj.cos.util.log.InferenceLog.ERROR_GROUNDING_EXAMPLE;

/**
 * An in memory version of the ProPPR's {@link Grounder}.
 * <p>
 * Created on 02/05/17.
 *
 * @author Victor Guimarães
 */
public class InMemoryGrounder<P extends ProofGraph> extends Grounder<P> {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * The empty
     */
    @SuppressWarnings("unused")
    public static final int EMPTY = 0;
    /**
     * The percent normalizer to change a number from [0, 1] to [0, 100]%.
     */
    public static final double PERCENT_NORMALIZER = 100.0;

    /**
     * Grounded log message.
     */
    public static final String GROUNDED = "Grounded: {}";
    /**
     * Skipped with no solutions log message.
     */
    public static final String SKIPPED_WITH_NO_SOLUTIONS = "Skipped: {} = {} with no labeled solutions; {} with empty" +
            " graphs";
    /**
     * Covered status log message.
     */
    public static final String COVERED_STATUS = "totalPos: {} totalNeg: {} coveredPos: {} coveredNeg: {}";
    /**
     * Positive examples proveable log message.
     */
    public static final String POSITIVE_EXAMPLES_PROVEABLE = "For positive examples {}/{} proveable [{}%]";
    /**
     * Negative examples proveable log message.
     */
    public static final String NEGATIVE_EXAMPLES_PROVEABLE = "For negative examples {}/{} proveable [{}%]";
    /**
     * Positive examples with fewest positive examples covered log message.
     */
    public static final String EXAMPLE_WITH_FEWEST_POS_EXAMPLES_COVERED = "Example with fewest [{}%] pos examples " +
            "covered: {}";

    /**
     * Constructor with the needed parameters for single thread execution.
     *
     * @param aprOptions the {@link APROptions}
     * @param prover     the {@link Prover}
     * @param program    the {@link WamProgram}
     * @param plugins    the {@link WamPlugin}s
     */
    public InMemoryGrounder(APROptions aprOptions, Prover<P> prover,
                            WamProgram program, WamPlugin... plugins) {
        super(aprOptions, prover, program, plugins);
    }

    /**
     * Constructor with the needed parameters for multi thread execution.
     *
     * @param numberOfThreads the number of threads
     * @param throttle        the throttle options
     * @param aprOptions      the {@link APROptions}
     * @param prover          the {@link Prover}
     * @param program         the {@link WamProgram}
     * @param plugins         the {@link WamPlugin}s
     */
    @SuppressWarnings("SameParameterValue")
    public InMemoryGrounder(int numberOfThreads, int throttle,
                            APROptions aprOptions, Prover<P> prover, WamProgram program,
                            WamPlugin... plugins) {

        super(numberOfThreads, throttle, aprOptions, prover, program, plugins);
    }

    /**
     * Ground the iterator in the {@link Iterable} and returns a {@link Map} with the {@link Ground}s, saving the
     * discovered features in a {@link SymbolTable}.
     *
     * @param inferenceExampleIterable the {@link Iterable}
     * @param masterFeatures           the {@link SymbolTable}
     * @return the {@link Map}
     */
    public Map<Integer, Ground<P>> groundExamples(Iterable<InferenceExample> inferenceExampleIterable,
                                                  SymbolTable<String> masterFeatures) {
        MapCleanup<Ground<P>> groundCleanup = new MapCleanup<>();
        try {
            StatusLogger status = new StatusLogger();
            GroundingStatistics statistics = new GroundingStatistics();
            SymbolTable<Feature> featureTable = new ConcurrentSymbolTable<>(ConcurrentSymbolTable.HASHING_STRATEGIES
                                                                                    .identity);
            Multithreading<InferenceExample, Ground<P>> multithreading
                    = new Multithreading<>(status, true);

            Transformer<InferenceExample, Ground<P>> transformer
                    = new GroundTransformer<>(prover, apr, featureTable, masterProgram, masterPlugins, statistics,
                                              includeUnlabeledGraphs, status);

            multithreading.executeJob(nthreads, inferenceExampleIterable, transformer, groundCleanup, throttle);
            saveFeaturesToSymbolTable(featureTable, masterFeatures);
            reportStatistics(statistics);
        } catch (Exception e) {
            logger.error(ERROR_GROUNDING_EXAMPLE.toString(), e);
        }
        return groundCleanup.getResultMap();
    }

    /**
     * Saves the discovered features to a {@link SymbolTable}.
     *
     * @param source      the source {@link SymbolTable}
     * @param destination the destination {@link SymbolTable}
     */
    protected static void saveFeaturesToSymbolTable(SymbolTable<Feature> source, SymbolTable<String> destination) {
        for (int i = 1; i < source.size() + 1; i++) {
            destination.insert(source.getSymbol(i).name);
        }
    }

    /**
     * Logs the {@link GroundingStatistics}.
     *
     * @param statistics the {@link GroundingStatistics}
     */
    protected void reportStatistics(GroundingStatistics statistics) {
        if (!logger.isTraceEnabled()) { return; }
        int skipped = statistics.noPosNeg + statistics.emptyGraph;
        logger.trace(GROUNDED, statistics.count - skipped);
        logger.trace(SKIPPED_WITH_NO_SOLUTIONS, skipped, statistics
                .noPosNeg, statistics
                             .emptyGraph);
        logger.trace(COVERED_STATUS, statistics.totalPos, statistics
                .totalNeg, statistics.coveredPos, statistics.coveredNeg);
        if (statistics.totalPos > 0) {
            logger.trace(POSITIVE_EXAMPLES_PROVEABLE, statistics.coveredPos, statistics.totalPos,
                         PERCENT_NORMALIZER * statistics.coveredPos / statistics
                                 .totalPos);
        }
        if (statistics.totalNeg > 0) {
            logger.trace(NEGATIVE_EXAMPLES_PROVEABLE, statistics.coveredNeg, statistics.totalNeg,
                         PERCENT_NORMALIZER * statistics.coveredNeg / statistics
                                 .totalNeg);
        }
        if (statistics.worstX != null) {
            logger.trace(EXAMPLE_WITH_FEWEST_POS_EXAMPLES_COVERED, PERCENT_NORMALIZER * statistics
                    .smallestFractionCovered, statistics.worstX.getQuery());
        }
    }

    /**
     * Gets the {@link WamProgram}.
     *
     * @return the {@link WamProgram}
     */
    public WamProgram getProgram() {
        return masterProgram;
    }

    /**
     * Sets the {@link WamProgram}.
     *
     * @param program the {@link WamProgram}
     */
    public void setProgram(WamProgram program) {
        this.masterProgram = program;
    }

}
