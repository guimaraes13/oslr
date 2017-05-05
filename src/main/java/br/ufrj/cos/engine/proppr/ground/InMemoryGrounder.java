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

import edu.cmu.ml.proppr.Grounder;
import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.prove.Prover;
import edu.cmu.ml.proppr.prove.wam.ProofGraph;
import edu.cmu.ml.proppr.prove.wam.WamProgram;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.multithreading.Multithreading;
import edu.cmu.ml.proppr.util.multithreading.Transformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    protected int empty;

    protected MapCleanup<Ground<P>> groundCleanup;

    protected Iterable<InferenceExample> inferenceExampleIterable;

    /**
     * Constructor with the needed parameters for single thread execution.
     *
     * @param inferenceExampleIterable the {@link Iterable} of {@link InferenceExample}
     * @param apr                      the {@link APROptions}
     * @param p                        the {@link Prover}
     * @param program                  the {@link WamProgram}
     * @param plugins                  the {@link WamPlugin}s
     */
    public InMemoryGrounder(Iterable<InferenceExample> inferenceExampleIterable, APROptions apr, Prover<P> p,
                            WamProgram program, WamPlugin... plugins) {
        super(apr, p, program, plugins);
        groundCleanup = new MapCleanup<>();
        this.inferenceExampleIterable = inferenceExampleIterable;
    }

    /**
     * Constructor with the needed parameters for multi thread execution.
     *
     * @param inferenceExampleIterable the {@link Iterable} of {@link InferenceExample}
     * @param numberOfThreads          the number of threads
     * @param throttle                 the throttle options
     * @param apr                      the {@link APROptions}
     * @param p                        the {@link Prover}
     * @param program                  the {@link WamProgram}
     * @param plugins                  the {@link WamPlugin}s
     */
    public InMemoryGrounder(Iterable<InferenceExample> inferenceExampleIterable, int numberOfThreads, int throttle,
                            APROptions apr, Prover<P> p, WamProgram program,
                            WamPlugin... plugins) {

        super(numberOfThreads, throttle, apr, p, program, plugins);
        groundCleanup = new MapCleanup<>();
        this.inferenceExampleIterable = inferenceExampleIterable;
    }

    public void groundExamples(File dataFile, File groundedFile, boolean maintainOrder) {
        status.start();
        try {
            if (this.graphKeyFile != null) {
                this.graphKeyWriter = new BufferedWriter(new FileWriter(this.graphKeyFile));
            }
            this.statistics = new GroundingStatistics();
            this.empty = 0;

            Multithreading<InferenceExample, Ground<P>> multithreading = new Multithreading<>(log, this.status,
                                                                                              maintainOrder);
            Transformer<InferenceExample, Ground<P>> transformer = new GroundTransformer<>(prover, apr,
                                                                                           featureTable,
                                                                                           masterProgram,
                                                                                           masterPlugins, statistics,
                                                                                           includeUnlabeledGraphs,
                                                                                           status);
            multithreading.executeJob(nthreads, inferenceExampleIterable, transformer, groundCleanup, throttle);

            reportStatistics(empty);

            File indexFile = new File(groundedFile.getParent(), groundedFile.getName() + FEATURE_INDEX_EXTENSION);
            serializeFeatures(indexFile, featureTable);

            if (this.graphKeyFile != null) {
                this.graphKeyWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the {@link Ground} examples.
     *
     * @return the {@link Ground} examples
     */
    public Map<Integer, Ground<P>> getGroundExamples() {
        return groundCleanup.getResultMap();
    }

}
