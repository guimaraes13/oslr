/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2017 Victor Guimarães
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

import br.ufrj.cos.util.LogMessages;
import edu.cmu.ml.proppr.Grounder;
import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.prove.Prover;
import edu.cmu.ml.proppr.prove.wam.ProofGraph;
import edu.cmu.ml.proppr.prove.wam.WamProgram;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.ConcurrentSymbolTable;
import edu.cmu.ml.proppr.util.SymbolTable;
import edu.cmu.ml.proppr.util.multithreading.Multithreading;
import edu.cmu.ml.proppr.util.multithreading.Transformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

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
    public static final int EMPTY = 0;

    /**
     * Constructor with the needed parameters for single thread execution.
     *
     * @param apr     the {@link APROptions}
     * @param p       the {@link Prover}
     * @param program the {@link WamProgram}
     * @param plugins the {@link WamPlugin}s
     */
    public InMemoryGrounder(APROptions apr, Prover<P> p,
                            WamProgram program, WamPlugin... plugins) {
        super(apr, p, program, plugins);
    }

    /**
     * Constructor with the needed parameters for multi thread execution.
     *
     * @param numberOfThreads the number of threads
     * @param throttle        the throttle options
     * @param apr             the {@link APROptions}
     * @param p               the {@link Prover}
     * @param program         the {@link WamProgram}
     * @param plugins         the {@link WamPlugin}s
     */
    public InMemoryGrounder(int numberOfThreads, int throttle,
                            APROptions apr, Prover<P> p, WamProgram program,
                            WamPlugin... plugins) {

        super(numberOfThreads, throttle, apr, p, program, plugins);
    }

    /**
     * Ground the examples in the {@link Iterable} and returns a {@link Map} with the {@link Ground}s, saving the
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
            this.statistics = new GroundingStatistics();
            this.featureTable = new ConcurrentSymbolTable<>(ConcurrentSymbolTable.HASHING_STRATEGIES.identity);
            Multithreading<InferenceExample, Ground<P>> multithreading
                    = new Multithreading<>(logger, status, true);

            Transformer<InferenceExample, Ground<P>> transformer
                    = new GroundTransformer<>(prover, apr, featureTable, masterProgram, masterPlugins, statistics,
                                              includeUnlabeledGraphs, status);

            multithreading.executeJob(nthreads, inferenceExampleIterable, transformer, groundCleanup, throttle);
            saveFeaturesToSymbolTable(masterFeatures);
            reportStatistics(EMPTY);
        } catch (Exception e) {
            logger.error(LogMessages.ERROR_GROUNDING_EXAMPLE.toString(), e);
        }
        return groundCleanup.getResultMap();
    }

    /**
     * Saves the discovered features to a {@link SymbolTable}.
     *
     * @param symbolTable the {@link SymbolTable}
     */
    protected void saveFeaturesToSymbolTable(SymbolTable<String> symbolTable) {
        for (int i = 1; i < featureTable.size() + 1; i++) {
            symbolTable.insert(featureTable.getSymbol(i).name);
        }
    }

}
