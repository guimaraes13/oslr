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

package br.ufrj.cos.engine.proppr;

import edu.cmu.ml.proppr.Grounder;
import edu.cmu.ml.proppr.examples.GroundedExample;
import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.examples.InferenceExampleStreamer;
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
import java.util.concurrent.Callable;

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

    public InMemoryGrounder(APROptions apr, Prover<P> p, WamProgram program, WamPlugin... plugins) {
        super(apr, p, program, plugins);
    }

    public InMemoryGrounder(int numberOfThreads, int throttle, APROptions apr, Prover<P> p, WamProgram program,
                            WamPlugin... plugins) {
        super(numberOfThreads, throttle, apr, p, program, plugins);
    }

    //TODO: modify this method to take a Iterable examples from memory, not from file
    //TODO: modify this to save the resultant ground to memory, not to file
    public void groundExamples(File dataFile, File groundedFile, boolean maintainOrder) {
        status.start();
        try {
            if (this.graphKeyFile != null) {
                this.graphKeyWriter = new BufferedWriter(new FileWriter(this.graphKeyFile));
            }
            this.statistics = new GroundingStatistics();
            this.empty = 0;

            Multithreading<InferenceExample, String> m = new Multithreading<>(log, this
                    .status, maintainOrder);

            m.executeJob(this.nthreads, new InferenceExampleStreamer(dataFile).stream(), new
                    Transformer<InferenceExample, String>() {
                        @Override
                        public Callable<String> transformer(InferenceExample in, int id) {
                            return new Ground(in, id);
                        }
                    }, groundedFile, this.throttle);

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

    //TODO: modify this class to save the grounded states
    private class Ground implements Callable<String> {

        InferenceExample inf;
        int id;

        public Ground(InferenceExample in, int id) {
            validateExample(in);
            this.inf = in;
            this.id = id;
        }

        @Override
        public String call() throws Exception {
            P pg = prover.makeProofGraph(inf, apr, featureTable, masterProgram, masterPlugins);
            GroundedExample gx = groundExample(getProver().copy(), pg);
            InferenceExample ix = pg.getExample();
            statistics.updateStatistics(ix, ix.getPosSet().length, ix.getNegSet().length, gx.getPosList().size(), gx
                    .getNegList().size());
            if (gx.getGraph().edgeSize() > 0) {
                if (gx.length() > 0 || includeUnlabeledGraphs) {
                    return (serializeGroundedExample(pg, gx));
                } else {
                    statistics.noPosNeg();
                    //log.warn("No positive or negative solutions for query "+id+":"+pg.getExample().getQuery()
                    // .toString()+"; skipping");
                }
            } else {
                statistics.emptyGraph(); //log.warn("Empty graph for example "+id);
            }
            return null;
        }
    }

}
