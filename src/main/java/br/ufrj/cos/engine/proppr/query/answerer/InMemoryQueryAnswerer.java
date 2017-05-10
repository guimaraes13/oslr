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

package br.ufrj.cos.engine.proppr.query.answerer;

import br.ufrj.cos.engine.proppr.MapCleanup;
import edu.cmu.ml.proppr.QueryAnswerer;
import edu.cmu.ml.proppr.prove.Prover;
import edu.cmu.ml.proppr.prove.wam.Feature;
import edu.cmu.ml.proppr.prove.wam.ProofGraph;
import edu.cmu.ml.proppr.prove.wam.Query;
import edu.cmu.ml.proppr.prove.wam.WamProgram;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.SymbolTable;
import edu.cmu.ml.proppr.util.multithreading.Multithreading;
import edu.cmu.ml.proppr.util.multithreading.Transformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * An in memory version of the ProPPR's {@link QueryAnswerer}.
 * <p>
 * Created on 06/05/17.
 *
 * @author Victor Guimarães
 */
public class InMemoryQueryAnswerer<P extends ProofGraph> extends QueryAnswerer<P> {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * Constructor with the needed parameters for multi thread execution.
     *
     * @param aprOptions        the {@link APROptions}
     * @param program           the {@link WamProgram}
     * @param plugins           the {@link WamPlugin}s
     * @param prover            the {@link Prover}
     * @param normalize         if it is to normalize
     * @param numberOfThreads   the number of threads this class is allowed to use
     * @param numberOfSolutions the number of top solutions to retrieve
     */
    @SuppressWarnings("SameParameterValue")
    public InMemoryQueryAnswerer(APROptions aprOptions, WamProgram program,
                                 WamPlugin[] plugins,
                                 Prover<P> prover, boolean normalize, int numberOfThreads, int numberOfSolutions) {
        super(aprOptions, program, plugins, prover, normalize, numberOfThreads, numberOfSolutions);
    }

    /**
     * Finds the solutions to the given {@link Query} queries. It puts the {@link Answer} into a {@link Map} where
     * the key is the index of the {@link Query} on the {@link Iterable}.
     *
     * @param queries the {@link Query}
     * @return the {@link Map} of {@link Answer}s of the queries
     */
    public Map<Integer, Answer<P>> findSolutions(Iterable<Query> queries) {
        Multithreading<Query, Answer<P>> multithreading = new Multithreading<>(logger, status, true);
        MapCleanup<Answer<P>> answerCleanup = new MapCleanup<>();
        Transformer<Query, Answer<P>> transformer = new QueryTransformer<>(program, plugins, prover, normalize, apr,
                                                                           featureTable, numSolutions, status);
        multithreading.executeJob(
                this.nthreads,
                queries,
                transformer,
                answerCleanup,
                Multithreading.DEFAULT_THROTTLE);

        return answerCleanup.getResultMap();
    }

    /**
     * Gets the {@link WamProgram}.
     *
     * @return the {@link WamProgram}
     */
    public WamProgram getProgram() {
        return program;
    }

    /**
     * Sets the {@link WamProgram}.
     *
     * @param program the {@link WamProgram}
     */
    public void setProgram(WamProgram program) {
        this.program = program;
    }

    /**
     * Gets the features table.
     *
     * @return the features table
     */
    public SymbolTable<Feature> getFeatureTable() {
        return featureTable;
    }

    /**
     * Sets the features table.
     *
     * @param featureTable the features table
     */
    public void setFeatureTable(SymbolTable<Feature> featureTable) {
        this.featureTable = featureTable;
    }

}
