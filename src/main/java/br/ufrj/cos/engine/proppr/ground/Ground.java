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
import edu.cmu.ml.proppr.examples.GroundedExample;
import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.prove.Prover;
import edu.cmu.ml.proppr.prove.wam.*;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.StatusLogger;
import edu.cmu.ml.proppr.util.SymbolTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Represents an iterator grounded by ProPPR.
 * <p>
 * Created on 05/05/17.
 *
 * @author Victor Guimarães
 */
public class Ground<P extends ProofGraph> implements Callable<Ground<P>> {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    protected final InferenceExample inferenceExample;
    protected final int id;

    protected final Prover<P> prover;
    protected final APROptions aprOptions;
    protected final SymbolTable<Feature> featureTable;
    protected final WamProgram masterProgram;
    protected final WamPlugin[] masterPlugins;

    protected final Grounder.GroundingStatistics statistics;
    protected final boolean includeUnlabeledGraphs;

    protected final StatusLogger status;

    protected P proofGraph = null;
    protected GroundedExample groundedExample;

    /**
     * Constructor with needed parameters.
     *
     * @param inferenceExample       the {@link InferenceExample}
     * @param id                     the example's id
     * @param prover                 the {@link Prover}
     * @param aprOptions             the {@link APROptions}
     * @param featureTable           the {@link SymbolTable}
     * @param masterProgram          the {@link WamProgram}
     * @param masterPlugins          the {@link WamPlugin}s
     * @param statistics             the {@link Grounder.GroundingStatistics}
     * @param includeUnlabeledGraphs if it is to include unlabeled graph
     * @param status                 the {@link StatusLogger}
     */
    public Ground(InferenceExample inferenceExample, int id, Prover<P> prover, APROptions aprOptions,
                  SymbolTable<Feature> featureTable, WamProgram masterProgram,
                  WamPlugin[] masterPlugins, Grounder.GroundingStatistics statistics, boolean includeUnlabeledGraphs,
                  StatusLogger status) {
        this.inferenceExample = inferenceExample;
        this.id = id;
        this.prover = prover;
        this.aprOptions = aprOptions;
        this.featureTable = featureTable;
        this.masterProgram = masterProgram;
        this.masterPlugins = masterPlugins;
        this.statistics = statistics;
        this.includeUnlabeledGraphs = includeUnlabeledGraphs;
        this.status = status;
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public Ground<P> call() throws Exception {
        proofGraph = prover.makeProofGraph(inferenceExample, aprOptions, featureTable, masterProgram, masterPlugins);
        groundedExample = groundExample(prover.copy());
        InferenceExample ix = proofGraph.getExample();
        statistics.updateStatistics(ix, ix.getPosSet().length, ix.getNegSet().length, groundedExample.getPosList()
                .size(), groundedExample
                                            .getNegList().size());
        if (groundedExample.getGraph().edgeSize() > 0) {
            if (groundedExample.length() > 0 || includeUnlabeledGraphs) {
//                return (proofGraph.serialize(groundedExample));
                return this;
            } else {
                statistics.noPosNeg();
            }
        } else {
            statistics.emptyGraph();
        }
        return null;
    }

    /**
     * Grounds the iterator using the {@link Prover}.
     *
     * @param prover the {@link Prover}
     * @return the {@link GroundedExample}
     * @throws LogicProgramException if an error occurs during the grounding
     */
    public GroundedExample groundExample(Prover<P> prover) throws LogicProgramException {
        logger.trace(LogMessages.GROUNDING_EXAMPLE.toString(), proofGraph.getExample().toString());

        Map<State, Double> ans = prover.prove(proofGraph, status);

        return proofGraph.makeRWExample(ans);
    }

    /**
     * Gets the {@link ProofGraph}.
     *
     * @return the {@link ProofGraph}
     */
    public P getProofGraph() {
        return proofGraph;
    }

    /**
     * Gets the {@link GroundedExample}.
     *
     * @return the {@link GroundedExample}
     */
    public GroundedExample getGroundedExample() {
        return groundedExample;
    }

    @Override
    public String toString() {
        if (proofGraph != null && groundedExample != null) {
            return proofGraph.serialize(groundedExample);
        } else {
            return super.toString();
        }
    }
}
