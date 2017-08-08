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

package br.ufrj.cos.knowledge.theory.manager.revision.operator.generalization;

import br.ufrj.cos.knowledge.KnowledgeException;
import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.AsyncTheoryEvaluator;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.logic.*;
import br.ufrj.cos.util.*;
import br.ufrj.cos.util.multithreading.ClauseSubstitutionAsyncTransformer;
import br.ufrj.cos.util.multithreading.MultithreadingEvaluation;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static br.ufrj.cos.util.log.InferenceLog.EVALUATION_INITIAL_THEORIES;
import static br.ufrj.cos.util.log.PreRevisionLog.RULE_APPENDED_TO_THEORY;
import static br.ufrj.cos.util.log.RevisionLog.*;

/**
 * Operator that implements Guimarães and Paes rule creation algorithm.
 * <p>
 * V. Guimarães and A. Paes,
 * Looking at the Bottom and the Top: A Hybrid Logical Relational Learning System Based on Answer Sets,
 * 2015 Brazilian Conference on Intelligent Systems (BRACIS), Natal, 2015, pp. 240-245.
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 * @see <a href="http://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=7424026&isnumber=7423894">Looking at the
 * Bottom and the Top: A Hybrid Logical Relational Learning System Based on Answer Sets</a>
 */
@SuppressWarnings("CanBeFinal")
public class BottomClauseBoundedRule extends GeneralizationRevisionOperator {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * Represents a constant for no maximum side way movements.
     */
    public static final int NO_MAXIMUM_SIDE_WAY_MOVEMENTS = -1;

    /**
     * The default value for {@link #improvementThreshold}.
     */
    public static final double DEFAULT_IMPROVEMENT_THRESHOLD = 0.0;

    /**
     * The class name of the variable generator.
     */
    public String variableGeneratorClassName = "br.ufrj.cos.util.VariableGenerator";

    /**
     * The class to use as the variable generator.
     */
    public Class<? extends VariableGenerator> variableGeneratorClass = VariableGenerator.class;

    /**
     * Represents the maximum depth on the transitivity of the relevant concept. A {@link Atom} is relevant to the
     * example if
     * it shares (or transitively) a {@link Term} with it.
     * <p>
     * If it is 0, it means that only the {@link Atom}s which actually share a {@link Term} if the
     * example will be considered.
     * <p>
     * If it is 1, it means that only the {@link Atom}s which actually share a {@link Term} if the
     * example will be considered, and the {@link Atom}s which share a {@link Term}s if those ones.
     * <p>
     * And so on. If it is NO_MAXIMUM_DEPTH, it means that there is no limit
     * on the transitivity.
     *
     * @see br.ufrj.cos.core.LearningSystem
     */
    public int relevantsDepth = 0;

    /**
     * Flag to specify if the rule must be refined or not.
     */
    public boolean refine = false;

    /**
     * Represents the maximum side way movements, i.e. the number of {@link Literal} that will be added to the body
     * of the {@link HornClause} without improving the metric.
     * <p>
     * If the metric improves by adding a {@link Literal} to the body, it not counts as a side way movements.
     * <p>
     * If it is {@link #NO_MAXIMUM_SIDE_WAY_MOVEMENTS}, it means there is no maximum side way
     * movements, it will be limited by the size of the bottom clause.
     */
    public int maximumSideWayMovements = NO_MAXIMUM_SIDE_WAY_MOVEMENTS;

    /**
     * The minimal necessary difference, between and current {@link HornClause} evaluation and a new candidate one,
     * to be considered as improvement. If the threshold is not met, it is considered a side way movement.
     * <p>
     * Use a threshold of 0 and {@link #maximumSideWayMovements} of {@link #NO_MAXIMUM_SIDE_WAY_MOVEMENTS} to allow the
     * search to test all possible {@link HornClause}s.
     * <p>
     * Use a threshold of {@code e} and {@link #maximumSideWayMovements} of 0 to stop as soon as a {@link HornClause}
     * does
     * not improves more than {@code e}.
     */
    public double improvementThreshold = DEFAULT_IMPROVEMENT_THRESHOLD;

    /**
     * Flag to specify which {@link HornClause} will be returned in case of a tie in the evaluation metric.
     * <p>
     * If it is {@code true}, the most generic one will be returned (i.e. the smallest).
     * <p>
     * If it is {@code false}, the most specific one will be returned (i.e. the largest).
     */
    public boolean generic = true;

    /**
     * The maximum amount of time, in seconds, allowed to the evaluation of the {@link Theory}.
     * <p>
     * By default, is 300 seconds (i.e. 5 minutes).
     */
    public int evaluationTimeout = MultithreadingEvaluation.DEFAULT_EVALUATION_TIMEOUT;

    /**
     * The maximum number of threads this class is allowed to create.
     */
    public int numberOfThreads = MultithreadingEvaluation.DEFAULT_NUMBER_OF_THREADS;

    protected MultithreadingEvaluation<Map.Entry<HornClause, Map<Term, Term>>> multithreading;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        if (theoryMetric == null) {
            throw new InitializationException(
                    ExceptionMessages.errorFieldsSet(this, TheoryMetric.class.getSimpleName()));
        }
        try {
            variableGeneratorClass = (Class<? extends VariableGenerator>) Class.forName(variableGeneratorClassName);
        } catch (ClassNotFoundException e) {
            throw new InitializationException(
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_GETTING_VARIABLE_GENERATOR_CLASS.toString(),
                                                 variableGeneratorClassName), e);
        }
        multithreading = new MultithreadingEvaluation(learningSystem, theoryMetric, evaluationTimeout,
                                                      new ClauseSubstitutionAsyncTransformer());
        multithreading.numberOfThreads = numberOfThreads;
    }

    @Override
    public Theory performOperation(Collection<? extends Example> targets) throws TheoryRevisionException {
        try {
            Theory theory = learningSystem.getTheory().copy();
            for (Example example : targets) {
                performOperationForExample(example, theory, targets);
            }
            return theory;
        } catch (KnowledgeException e) {
            throw new TheoryRevisionException(ExceptionMessages.ERROR_DURING_THEORY_COPY.toString(), e);
        }
    }

    @Override
    public void theoryRevisionAccepted(Theory revised) {

    }

    /**
     * Performs the operation for a single example
     *
     * @param example            the example
     * @param theory             the theory
     * @param evaluationExamples the evaluation examples
     */
    protected void performOperationForExample(Example example, Theory theory,
                                              Collection<? extends Example> evaluationExamples) {
        HornClause newRule;
        try {
            if (!example.isPositive() || isCovered(example, theory)) {
                if (example.isPositive()) {
                    logger.trace(SKIPPING_COVERED_EXAMPLE.toString(), example);
                }
                return;
            }
            logger.info(BUILDING_CLAUSE_FROM_EXAMPLE.toString(), example);
            newRule = buildRuleForExample(evaluationExamples, example);
            if (theory.add(newRule)) {
                logger.debug(RULE_APPENDED_TO_THEORY.toString(), newRule);
            }
        } catch (TheoryRevisionException e) {
            logger.debug(e.getMessage());
        }
    }

    /**
     * Checks if the {@link Example} has been already covered by given theory.
     *
     * @param example the {@link Example}
     * @param theory  the theory
     * @return {@code true} if it has, {@code false} otherwise
     */
    protected boolean isCovered(Example example, Theory theory) {
        Map<Example, Map<Atom, Double>> inferred = learningSystem.inferExamples(theory, example.getGroundedQuery());
        Set<Atom> grounds = inferred.values().stream().flatMap(e -> e.keySet().stream()).collect(Collectors.toSet());
        for (AtomExample ground : example.getGroundedQuery()) {
            if (ground.isPositive() && !grounds.contains(ground.getAtom())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Builds a {@link HornClause} from a target example, based on the Guimarães and Paes rule creation algorithm.
     *
     * @param evaluationExamples the evaluation examples
     * @param example            the target example
     * @return a {@link HornClause}
     * @throws TheoryRevisionException in an error occurs during the revision
     */
    protected HornClause buildRuleForExample(Collection<? extends Example> evaluationExamples,
                                             Example example) throws TheoryRevisionException {
        try {
            logger.debug(BUILDING_THE_BOTTOM_CLAUSE.toString(), example);
            HornClause bottomClause = buildBottomClause(example);

            logger.debug(FIND_MINIMAL_SAFE_CLAUSES);
            Map<HornClause, Map<Term, Term>> candidateClauses = HornClauseUtils.buildMinimalSafeRule(bottomClause);

            logger.debug(EVALUATION_INITIAL_THEORIES.toString(), candidateClauses.size());
            AsyncTheoryEvaluator bestClause = multithreading.getBestClausesFromCandidates(candidateClauses.entrySet(),
                                                                                          evaluationExamples);
            if (bestClause == null) {
                logger.debug(ERROR_EVALUATING_MINIMAL_CLAUSES);
                return null;
            }

            if (refine) {
                logger.debug(REFINING_RULE_FROM_EXAMPLE.toString(), example);
                bestClause = refineRule(bestClause, bottomClause.getBody(), bestClause.getSubstitutionMap(),
                                        evaluationExamples);
            }
            return bestClause.getHornClause();
        } catch (Exception e) {
            throw new TheoryRevisionException(ERROR_REVISING_THEORY.toString(), e);
        }
    }

    /**
     * Builds the bottom clause based on the target {@link Example}
     *
     * @param target the target {@link Example}
     * @return the bottom clause
     * @throws IllegalAccessException if an error occurs when instantiating a new object by reflection
     * @throws InstantiationException if an error occurs when instantiating a new object by reflection
     */
    protected HornClause buildBottomClause(Example target) throws InstantiationException, IllegalAccessException {
        Set<Atom> relevants = learningSystem.relevantsBreadthFirstSearch(target.getPositiveTerms(), relevantsDepth,
                                                                         !refine);
        Map<Term, Term> variableMap = target.getVariableMap();

        return toVariableHornClauseForm(target, relevants, variableMap);
    }

    /**
     * Refines the rule. It starts from the best minimal candidate and adds a {@link Literal} at time into its body. At
     * each time, getting the best possible {@link HornClause}. It finishes when one of the following criteria is met:
     * <p>
     * 1) The addition of another {@link Literal} does not improves the {@link HornClause} in
     * {@link #maximumSideWayMovements} times;
     * <br>
     * 2) There is no more possible addition to make;
     * <p>
     * After it finishes, it return the best {@link HornClause} found, based on the {@link #generic} criteria.
     *
     * @param initialClause       the initial minimal candidate clause
     * @param candidateLiterals   the candidate literals
     * @param initialSubstitution the initial substitution map
     * @param evaluationExamples  the evaluation examples
     * @return a {@link AsyncTheoryEvaluator} containing the best {@link HornClause} found
     */
    @SuppressWarnings("OverlyLongMethod")
    protected AsyncTheoryEvaluator refineRule(AsyncTheoryEvaluator initialClause, Set<Literal> candidateLiterals,
                                              Map<Term, Term> initialSubstitution,
                                              Collection<? extends Example> evaluationExamples) {
        Set<Literal> candidates = candidateLiterals;
        Map<Term, Term> substitutionMap = initialSubstitution;
        AsyncTheoryEvaluator bestClause = initialClause;
        AsyncTheoryEvaluator currentClause = initialClause;
        int sideWayMovements = 0;
        while (!isToStopBySideWayMovements(sideWayMovements) && !candidates.isEmpty()) {
            candidates = HornClauseUtils.unifyCandidates(candidates, substitutionMap);
            candidates.removeAll(currentClause.getHornClause().getBody());
            currentClause = specifyRule(currentClause.getHornClause(), candidates, evaluationExamples);
            if (currentClause == null) { break; }
            substitutionMap = currentClause.getSubstitutionMap();
            if (substitutionMap == null) { substitutionMap = new HashMap<>(); }
            if (theoryMetric.difference(currentClause.getEvaluation(), bestClause.getEvaluation()) >
                    improvementThreshold) {
                bestClause = currentClause;
                sideWayMovements = 0;
            } else {
                sideWayMovements++;
                if (theoryMetric.difference(currentClause.getEvaluation(), bestClause.getEvaluation()) >= 0.0 &&
                        !generic) {
                    bestClause = currentClause;
                }
            }
        }
        return bestClause;
    }

    /**
     * Creates a variable version of the {@link HornClause} with the variable target {@link Example} in the head
     *
     * @param target      the target {@link Example}
     * @param body        the body of the rule
     * @param variableMap the variableMap, useful when wants to collapse more than on {@link Term} into the same
     *                    {@link Variable}
     * @return the variable {@link HornClause}
     * @throws IllegalAccessException if an error occurs when instantiating a new list of {@link Term}s
     * @throws InstantiationException if an error occurs when instantiating a new list of {@link Term}s
     */
    @SuppressWarnings("OverlyCoupledMethod")
    protected HornClause toVariableHornClauseForm(Example target, Collection<? extends Atom> body,
                                                  Map<Term, Term> variableMap) throws IllegalAccessException,
            InstantiationException {
        VariableGenerator variableGenerator = variableGeneratorClass.newInstance();
        //Maps the Term::getName to each Variable in the values of variableMap and makes a Set of it
        variableGenerator.setUsedNames(variableMap.values().stream().map(Term::getName).collect(Collectors.toSet()));
        Conjunction conjunction = new Conjunction(body.size());
        for (Atom atom : body) {
            conjunction.add(new Literal(LanguageUtils.toVariableAtom(atom, variableMap, variableGenerator)));
        }

        return new HornClause(LanguageUtils.toVariableAtom(target.getAtom(), variableMap, variableGenerator),
                              conjunction);
    }

    /**
     * Checks if is to stop due to much iterations without improvements.
     *
     * @param sideWayMovements the number of iterations without improvements
     * @return {@code true} if it is to stop, {@code false} if it is to continue
     */
    protected boolean isToStopBySideWayMovements(int sideWayMovements) {
        return maximumSideWayMovements > NO_MAXIMUM_SIDE_WAY_MOVEMENTS && sideWayMovements > maximumSideWayMovements;
    }

    /**
     * Makes the {@link HornClause} more specific by adding a {@link Literal} from the candidates into the body. All
     * the possible {@link Literal}s are tested. {@link Literal}s that make the {@link HornClause} unsafe are not
     * tested.
     *
     * @param clause             the {@link HornClause}
     * @param candidates         the candidate {@link Literal}s
     * @param evaluationExamples the evaluation examples
     * @return the best obtained clause
     */
    protected AsyncTheoryEvaluator specifyRule(HornClause clause, Iterable<Literal> candidates,
                                               Collection<? extends Example> evaluationExamples) {
        Set<Pair<HornClause, Map<Term, Term>>> clauses =
                HornClauseUtils.buildAllCandidatesFromClause(clause.getHead(), clause.getBody(), candidates);

        return multithreading.getBestClausesFromCandidates(clauses, evaluationExamples);
    }

    /**
     * Evaluates the {@link Thread} with a limit of time defined by the {@link #evaluationTimeout}. If the evaluation
     * success, the correspondent evaluation value is returned. If it fails, is returned the default value of the
     * metric, instead.
     *
     * @param examples the examples
     * @return the evaluation value
     */
    public double evaluateTheory(Collection<? extends Example> examples) {
        AsyncTheoryEvaluator evaluator = new AsyncTheoryEvaluator(examples,
                                                                  learningSystem.getTheoryEvaluator(), theoryMetric,
                                                                  evaluationTimeout);
        return evaluator.call().getEvaluation();
    }

}
