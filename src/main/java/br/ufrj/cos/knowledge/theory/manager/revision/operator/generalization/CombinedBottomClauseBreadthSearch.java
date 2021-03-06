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

package br.ufrj.cos.knowledge.theory.manager.revision.operator.generalization;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.AsyncTheoryEvaluator;
import br.ufrj.cos.logic.EquivalentHornClause;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.Literal;
import br.ufrj.cos.logic.Predicate;
import br.ufrj.cos.util.HornClauseUtils;
import br.ufrj.cos.util.LanguageUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static br.ufrj.cos.util.log.InferenceLog.BIGGEST_GAP_THRESHOLD;
import static br.ufrj.cos.util.log.InferenceLog.EVALUATION_THEORIES_OF_SIZE;
import static br.ufrj.cos.util.log.PreRevisionLog.RULE_APPENDED_TO_THEORY;
import static br.ufrj.cos.util.log.RevisionLog.*;

/**
 * Class to create a set of rules from a set of examples by doing a breadth search on the space of the combined bottom
 * clause from all the examples.
 * <p>
 * It appends a literal at the time until create all the possible clauses of size {@link #maximumDepthSize}, then it
 * evaluate those clauses and split them in the biggest evaluation gap (1). Finally it returns the best evaluated
 * clauses.
 * <p>
 * The biggest gap is the biggest difference between the evaluation of the i-th clause and the i+1-th clause.
 * <p>
 * Created on 18/08/17.
 *
 * @author Victor Guimarães
 */
public class CombinedBottomClauseBreadthSearch extends CombinedBottomClauseBoundedRule {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * The default maximum depth of the tree.
     */
    public static final int DEFAULT_MAXIMUM_DEPTH_SIZE = 2;
    /**
     * The minimum depth of the tree.
     */
    public static final int MINIMUM_DEPTH_SIZE = 1;

    /**
     * The maximum depth of the tree.
     */
    @SuppressWarnings("CanBeFinal")
    public int maximumDepthSize = DEFAULT_MAXIMUM_DEPTH_SIZE;

    /**
     * If {@code true} goes strait to the maximum size, if {@code false}, goes size by size, looking for
     * improvements, until reach any stop criteria.
     * <p>
     * The stop criteria are: <br>
     * 1) The maximum size; <br>
     * 2) No improvements over {@link #maximumSideWayMovements} iterations; <br>
     * 3) No more possibilities to test.
     */
    @SuppressWarnings("CanBeFinal")
    public boolean strictToMaximumSize = true;

    /**
     * Performs the operation for a all the examples combined.
     *
     * @param targets the examples
     * @param theory  the theory
     */
    @Override
    protected void performOperationForExamples(Collection<? extends Example> targets, Theory theory) {
        Map<Predicate, List<Example>> examplesByPredicate =
                targets.stream().collect(Collectors.groupingBy(e -> e.getGoalQuery().getPredicate()));
        logger.info(FOUND_PREDICATES.toString(), examplesByPredicate.keySet().size());
        Predicate predicate;
        List<Example> examples;
        for (Map.Entry<Predicate, List<Example>> entry : examplesByPredicate.entrySet()) {
            try {
                predicate = entry.getKey();
                examples = entry.getValue();
                logger.info(BUILDING_CLAUSE_FROM_PREDICATE_EXAMPLES.toString(), predicate, examples.size());
                HornClause bottomClause = buildCombinedBottomClause(predicate, examples);
                logger.info(BOTTOM_CLAUSE_SIZE.toString(), bottomClause.getBody().size());
                buildRuleFromBottomClause(targets, bottomClause, theory);
            } catch (IllegalAccessException | InstantiationException e) {
                logger.trace(ERROR_REVISING_EXAMPLE, e);
            }
        }
    }

    /**
     * Builds a {@link HornClause} from a bottom clause, based on the Guimarães and Paes rule creation algorithm.
     *
     * @param evaluationExamples the evaluation examples
     * @param bottomClause       the bottom clause to generate the rule
     * @param theory             the theory to append the rules
     */
    protected void buildRuleFromBottomClause(Collection<? extends Example> evaluationExamples, HornClause bottomClause,
                                             Theory theory) {
        List<Literal> candidateLiterals
                = bottomClause.getBody().stream().filter(l -> !l.isNegated())
                .sorted(Comparator.comparing(l -> l.getPredicate().toString()))
                .collect(Collectors.toList());
        final List<HornClause> hornClauses = refineRules(candidateLiterals, evaluationExamples, bottomClause);

        for (HornClause clause : hornClauses) {
            HornClause featureClause = featureGenerator.createFeatureForRule(clause, evaluationExamples);
            if (theory.add(featureClause)) {
                logger.info(RULE_APPENDED_TO_THEORY.toString(), featureClause);
            }
        }
    }

    /**
     * Refines the set of clauses.
     *
     * @param candidateLiterals  the candidate literals
     * @param evaluationExamples the evaluation examples
     * @param bottomClause       the initial bottom clause
     * @return the best set of clauses
     */
    @SuppressWarnings({"OverlyLongMethod", "OverlyComplexMethod"})
    protected List<HornClause> refineRules(List<Literal> candidateLiterals,
                                           Collection<? extends Example> evaluationExamples,
                                           HornClause bottomClause) {
        Queue<EquivalentHornClause> queue = new ArrayDeque<>();
        queue.add(new EquivalentHornClause(bottomClause.getHead()));
        final int size = getSize(bottomClause);
        int i = 1;
        int sideWayMovements = 0;
        double bestEvaluation = 0;
        double currentEvaluation;
        List<HornClause> bestClauses = Collections.emptyList();
        List<HornClause> currentClauses;
        while (!isToStopBySideWayMovements(sideWayMovements) && !queue.isEmpty() && i < size) {
            currentClauses = getNextClauses(candidateLiterals, evaluationExamples, queue, i);
            if (currentClauses.isEmpty()) { break; }
            logger.trace(CANDIDATE_CLAUSES.toString(), LanguageUtils.iterableToString(currentClauses));
            currentEvaluation = evaluateRules(evaluationExamples, currentClauses);
            logger.debug(CANDIDATE_EVALUATION.toString(), currentEvaluation);
            if (theoryMetric.difference(currentEvaluation, bestEvaluation) > improvementThreshold) {
                logger.debug(ACCEPTING_NEW_BEST_REFINED_CANDIDATE.toString(),
                             LanguageUtils.iterableToString(currentClauses));
                bestEvaluation = currentEvaluation;
                bestClauses = currentClauses;
                sideWayMovements = 0;
            } else {
                logger.debug(MAKING_SIDE_MOVEMENT_FOR_CANDIDATE.toString(),
                             LanguageUtils.iterableToString(currentClauses));
                sideWayMovements++;
                if (theoryMetric.difference(currentEvaluation, bestEvaluation) >= 0.0 && !generic) {
                    bestClauses = currentClauses;
                }
            }
            if (strictToMaximumSize) { break; }
            i++;
        }

        return bestClauses;
    }

    private int getSize(HornClause bottomClause) {
        return (maximumDepthSize < MINIMUM_DEPTH_SIZE ? bottomClause.getBody().size() : maximumDepthSize) + 1;
    }

    /**
     * Gets the next iteration of clauses.
     *
     * @param candidateLiterals  the candidate literals
     * @param evaluationExamples the evaluation examples
     * @param queue              the queue of clauses
     * @param i                  the size of this iteration
     * @return the next iteration of clauses
     */
    protected List<HornClause> getNextClauses(List<Literal> candidateLiterals,
                                              Collection<? extends Example> evaluationExamples,
                                              Queue<EquivalentHornClause> queue, int i) {
        Map<AsyncTheoryEvaluator<EquivalentHornClause>, Double> evaluationMap;
        List<AsyncTheoryEvaluator<EquivalentHornClause>> equivalentHornClauses;
        if (strictToMaximumSize) {
            for (int j = i; j < maximumDepthSize + 1; j++) {
                logger.debug(FIND_CLAUSES_OF_SIZE.toString(), j);
                HornClauseUtils.appendAllCandidatesToQueue(queue, candidateLiterals);
            }
        } else {
            logger.debug(FIND_CLAUSES_OF_SIZE.toString(), i);
            HornClauseUtils.appendAllCandidatesToQueue(queue, candidateLiterals);
        }
        if (queue.isEmpty()) { return Collections.emptyList(); }
        logger.debug(EVALUATION_THEORIES_OF_SIZE.toString(), queue.size(), i);
        evaluationMap = new HashMap<>();
        multithreading.getBestClausesFromCandidates(queue, evaluationExamples, evaluationMap);
        equivalentHornClauses = evaluationMap.entrySet().stream()
                .sorted(Comparator.comparing(e -> -e.getValue(), theoryMetric))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        final int biggestGap = HornClauseUtils.findBiggestGap(equivalentHornClauses,
                                                              AsyncTheoryEvaluator::getEvaluation);
        logger.debug(BIGGEST_GAP_THRESHOLD.toString(), equivalentHornClauses.get(biggestGap - 1).getEvaluation());
        equivalentHornClauses = equivalentHornClauses.subList(0, biggestGap);

        return equivalentHornClauses.stream()
                .map(AsyncTheoryEvaluator::getHornClause).collect(Collectors.toList());
    }

    /**
     * Evaluates the collections of clauses.
     *
     * @param evaluationExamples the examples
     * @param clauses            the clauses
     * @return the evaluation
     */
    protected double evaluateRules(Collection<? extends Example> evaluationExamples,
                                   Collection<? extends HornClause> clauses) {
        //IMPROVE: run this function in another thread with timeout
        return learningSystem.getTheoryEvaluator().evaluateTheoryAppendingClauses(theoryMetric,
                                                                                  evaluationExamples, clauses);
    }

}