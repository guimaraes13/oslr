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

import br.ufrj.cos.engine.proppr.ground.Ground;
import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.prove.Prover;
import edu.cmu.ml.proppr.prove.wam.Feature;
import edu.cmu.ml.proppr.prove.wam.ProofGraph;
import edu.cmu.ml.proppr.prove.wam.Query;
import edu.cmu.ml.proppr.prove.wam.WamProgram;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.StatusLogger;
import edu.cmu.ml.proppr.util.SymbolTable;
import edu.cmu.ml.proppr.util.multithreading.Transformer;

import java.util.concurrent.Callable;

/**
 * Transforms an {@link InferenceExample} into a {@link Ground}.
 * <p>
 * Created on 05/05/17.
 *
 * @author Victor Guimarães
 */
public class QueryTransformer<P extends ProofGraph> extends Transformer<Query, Answer<P>> {

    protected WamProgram masterProgram;
    protected WamPlugin[] masterPlugins;
    protected Prover<P> prover;
    protected boolean normalizer;
    protected APROptions aprOptions;
    protected SymbolTable<Feature> featureTable;
    protected int numberOfSolutions;
    protected StatusLogger status;

    /**
     * Constructor with the needed parameters to build the {@link Answer}.
     *
     * @param program           the {@link WamProgram}
     * @param plugins           the {@link WamPlugin}s
     * @param prover            the {@link Prover}
     * @param normalizer        if it is to normalize
     * @param aprOptions        the {@link APROptions}
     * @param featureTable      the {@link SymbolTable} of {@link Feature}s
     * @param numberOfSolutions the number of solutions to retrieve
     * @param status            the {@link StatusLogger}
     */
    public QueryTransformer(WamProgram program, WamPlugin[] plugins, Prover<P> prover, boolean normalizer,
                            APROptions aprOptions,
                            SymbolTable<Feature> featureTable, int numberOfSolutions,
                            StatusLogger status) {
        this.masterProgram = program;
        this.masterPlugins = plugins;
        this.prover = prover;
        this.normalizer = normalizer;
        this.aprOptions = aprOptions;
        this.featureTable = featureTable;
        this.numberOfSolutions = numberOfSolutions;
        this.status = status;
    }

    @Override
    public Callable<Answer<P>> transformer(Query in, int id) {
        return new Answer<>(in, id, masterProgram, masterPlugins, prover, normalizer, aprOptions, featureTable,
                            numberOfSolutions, status);
    }

}
