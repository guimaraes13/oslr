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

package br.ufrj.cos.engine.proppr.query.answerer;

import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.FileIOUtils;
import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.prove.FeatureDictWeighter;
import edu.cmu.ml.proppr.prove.Prover;
import edu.cmu.ml.proppr.prove.wam.*;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.StatusLogger;
import edu.cmu.ml.proppr.util.SymbolTable;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.Callable;

import static br.ufrj.cos.util.log.InferenceLog.*;

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
    @SuppressWarnings("unused")
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

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public Answer<P> call() throws Exception {
        try {
            return findSolutions(program, plugins, prover.copy(), query, normalize, id);
        } catch (LogicProgramException e) {
            throw new LogicProgramException(
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_ANSWERING_QUERY.toString(), query), e);
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
    @SuppressWarnings("MethodWithTooManyParameters")
    public Answer<P> findSolutions(WamProgram program, WamPlugin[] plugins, Prover<P> prover, Query query,
                                   boolean normalize, @SuppressWarnings("unused") int id) throws LogicProgramException {
        P pg = prover.makeProofGraph(new InferenceExample(query, null, null), aprOptions, featureTable,
                                     program, plugins);
        logger.trace(ANSWERING_QUERY.toString(), query);
        Map<State, Double> dist = prove(prover, pg);
        if (dist == null) {
            logger.info("Null proof!");
            return null;
        }
        if (aprOptions.traceDepth != 0) { logProveGraph(pg, prover.getWeighter(), aprOptions.traceDepth); }
        solutions = new TreeMap<>();
        for (Map.Entry<State, Double> s : dist.entrySet()) {
            if (s.getKey().isCompleted()) {
                Query x = pg.fill(s.getKey());
                solutions.put(x, s.getValue());
                logger.trace(ANSWER_RESULT_WITH_VALUE.toString(), x.toString(), s.getValue());
            } else {
                logger.trace(ANSWER_STATE_WITH_VALUE.toString(), s.toString(), s.getValue());
            }
        }
        if (normalize) {
            solutions = Dictionary.normalize(solutions);
        }
        List<Map.Entry<Query, Double>> solutionDist = Dictionary.sort(solutions);
        logger.trace(NUMBER_OF_QUERY_ANSWERS.toString(), solutionDist.size());

        return this;
    }

    /**
     * Tries to prove the examples and make the {@link ProofGraph}.
     *
     * @param prover the {@link Prover}
     * @param pg     the initial {@link ProofGraph}
     * @return the graph in a {@link Map}
     */
    protected Map<State, Double> prove(Prover<P> prover, P pg) {
        try {
            return prover.prove(pg, status);
        } catch (LogicProgramException ignored) {
            logger.trace(ERROR_PROVING_GOAL.toString(), Arrays.deepToString(query.getRhs()));
        }
        return null;
    }

    private void logProveGraph(P pg, FeatureDictWeighter weighter, int traceDepth) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Graph:\n");
            stringBuilder.append("graph graphname {\n");
            Queue<State> queue = new ArrayDeque<>();
            Map<Integer, Integer> distanceToRoot = new HashMap<>();
            State startState = pg.getStartState();
            queue.add(startState);
            distanceToRoot.put(pg.getId(startState), 0);
            Set<String> addedLinks = new HashSet<>();
            while (!queue.isEmpty()) {
                final State state = queue.poll();
                final int distance = distanceToRoot.get(pg.getId(state)) + 1;
                final String fillState = pg.fill(state).toString();
                if (traceDepth > 0 && distance > traceDepth) {
                    stringBuilder.append(StringUtils.repeat("\t", distance));
                    stringBuilder.append("\"").append(fillState).append("\"");
                    stringBuilder.append(" -- ");
                    stringBuilder.append("\"...").append(fillState).append("...\"").append(";").append("\n");
                }
                for (State child : pg.getOutState(state, weighter)) {
                    final String fillChild = pg.fill(child).toString();
                    final String link = fillState + fillChild;
                    if (fillState.equals(fillChild) || addedLinks.contains(link)) { continue; }
                    try {
                        final StringBuilder subStringBuilder = new StringBuilder();
                        subStringBuilder.append(StringUtils.repeat("\t", distance));
                        subStringBuilder.append("\"").append(fillState).append("\"");
                        subStringBuilder.append(" -- ");
                        subStringBuilder.append("\"").append(fillChild).append("\"").append(";").append("\n");
                        stringBuilder.append(subStringBuilder);
                        addedLinks.add(link);
                        if (!distanceToRoot.containsKey(pg.getId(child))) {
                            queue.add(child);
                            distanceToRoot.put(pg.getId(child), distance);
                        }
                    } catch (Exception ignored) {
                        throw ignored;
                    }
                }
            }
            stringBuilder.append("}\n");
            logger.info(stringBuilder.toString());
        } catch (Exception e) {
            logger.error("Error logging proof graph, reason:\t", e);
        }
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
