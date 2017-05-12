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

import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.LogMessages;
import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.prove.Prover;
import edu.cmu.ml.proppr.prove.wam.*;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.StatusLogger;
import edu.cmu.ml.proppr.util.SymbolTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

/**
 * Represents a ProPPR's Answer of a goal.
 * <p>
 * Created on 06/05/17.
 *
 * @author Victor Guimarães
 */
public class Answer<P extends ProofGraph> implements Callable<Answer<P>> {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    protected final Query query;
    protected final int id;

    protected final WamProgram program;
    protected final WamPlugin[] plugins;
    protected final Prover<P> prover;
    protected final boolean normalize;

    protected final APROptions aprOptions;
    protected final SymbolTable<Feature> featureTable;
    protected final int numSolutions;
    protected final StatusLogger status;

    protected Map<Query, Double> solutions;

    /**
     * Constructor with the needed parameters.
     *
     * @param query             the {@link Query}
     * @param id                the id
     * @param program           the {@link WamProgram}
     * @param plugins           the {@link WamPlugin}s
     * @param prover            the {@link Prover}
     * @param normalize         if it is to normalizeAnswers
     * @param aprOptions        the {@link APROptions}
     * @param featureTable      the {@link SymbolTable} of {@link Feature}s
     * @param numberOfSolutions the number of solutions to retrieve
     * @param status            the {@link StatusLogger}
     */
    public Answer(Query query, int id, WamProgram program, WamPlugin[] plugins, Prover<P> prover, boolean normalize,
                  APROptions aprOptions,
                  SymbolTable<Feature> featureTable, int numberOfSolutions, StatusLogger status) {
        this.query = query;
        this.id = id;
        this.program = program;
        this.plugins = plugins;
        this.prover = prover;
        this.normalize = normalize;
        this.aprOptions = aprOptions;
        this.featureTable = featureTable;
        this.numSolutions = numberOfSolutions;
        this.status = status;
    }

    @Override
    public Answer<P> call() throws Exception {
        try {
            return findSolutions(program, plugins, prover.copy(), query, normalize, id);
        } catch (LogicProgramException e) {
            throw new LogicProgramException(
                    String.format(ExceptionMessages.ERROR_ANSWERING_QUERY.toString(), query), e);
        }
    }

    /**
     * Finds the solution and stores the results this class fields.
     *
     * @param program   the {@link WamProgram}
     * @param plugins   the {@link WamPlugin}s
     * @param prover    the {@link Prover}
     * @param query     the {@link Query}
     * @param normalize if it is to normalizeAnswers
     * @param id        the id
     * @return this class with the solutions in it.
     * @throws LogicProgramException if a error on the logic program occurs
     */
    public Answer<P> findSolutions(WamProgram program, WamPlugin[] plugins, Prover<P> prover, Query query,
                                   boolean normalize, int id) throws LogicProgramException {
        P pg = prover.makeProofGraph(new InferenceExample(query, null, null), aprOptions, featureTable,
                                     program, plugins);
        logger.trace(LogMessages.ANSWERING_QUERY.toString(), query);
        Map<State, Double> dist = prover.prove(pg, status);
        solutions = new TreeMap<>();
        for (Map.Entry<State, Double> s : dist.entrySet()) {
            if (s.getKey().isCompleted()) {
                Query x = pg.fill(s.getKey());
                solutions.put(x, s.getValue());
                logger.trace(LogMessages.ANSWER_RESULT_WITH_VALUE.toString(), x.toString(), s.getValue());
            } else {
                logger.trace(LogMessages.ANSWER_STATE_WITH_VALUE.toString(), s.toString(), s.getValue());
            }
        }
        if (normalize) {
            solutions = Dictionary.normalize(solutions);
        }
        List<Map.Entry<Query, Double>> solutionDist = Dictionary.sort(solutions);
        logger.trace(LogMessages.NUMBER_OF_QUERY_ANSWERS.toString(), solutionDist.size());

        return this;
    }

    /**
     * Gets the solutions of the query. The solution is a {@link Map} of the queries with their correspondent value.
     *
     * @return the solutions of the query
     */
    public Map<Query, Double> getSolutions() {
        return solutions;
    }

}
