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

package br.ufrj.cos.knowledge.theory.manager.revision.operator;

import br.ufrj.cos.knowledge.KnowledgeException;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.AsyncTheoryEvaluator;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.logic.*;
import br.ufrj.cos.util.*;
import br.ufrj.cos.util.multithreading.MultithreadingEvaluation;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static br.ufrj.cos.util.log.RevisionLog.ERROR_REVISING_THEORY;

/**
 * Class of operator that given an rule and a set of examples, finds a new literal to append, into the body of the
 * rule, that improves the rule's classification of the examples.
 * <p>
 * Created on 23/06/17.
 *
 * @author Victor Guimarães
 */
public abstract class LiteralAppendOperator<V> extends RevisionOperator {

    /**
     * The default value of the {@link #generateFeatureBeforeEvaluate}.
     */
    public static final boolean DEFAULT_FEATURE_BEFORE_EVALUATE = false;
    /**
     * The default maximum based examples to use in the revision.
     */
    public static final int DEFAULT_MAXIMUM_BASED_EXAMPLES = -1;
    /**
     * If {@code true}, generates the feature before evaluating the rule to decide which one is the best.
     */
    @SuppressWarnings("CanBeFinal")
    public boolean generateFeatureBeforeEvaluate = DEFAULT_FEATURE_BEFORE_EVALUATE;
    /**
     * The maximum number of threads this class is allowed to create.
     */
    @SuppressWarnings("CanBeFinal")
    public int numberOfThreads = MultithreadingEvaluation.DEFAULT_NUMBER_OF_THREADS;
    /**
     * The maximum amount of time, in seconds, allowed to the evaluation of the {@link HornClause}.
     * <p>
     * By default, is 300 seconds (i.e. 5 minutes).
     */
    @SuppressWarnings("CanBeFinal")
    public int evaluationTimeout = MultithreadingEvaluation.DEFAULT_EVALUATION_TIMEOUT;
    /**
     * The random generator to select subsets of the examples.
     */
    public Random randomGenerator;
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
    @SuppressWarnings("CanBeFinal")
    public int relevantsDepth = 0;
    /**
     * The maximum based examples to use in the revision.
     */
    protected int maximumBasedExamples = DEFAULT_MAXIMUM_BASED_EXAMPLES;

    protected MultithreadingEvaluation<V, Object> multithreading;

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        if (theoryMetric == null) {
            throw new InitializationException(
                    ExceptionMessages.errorFieldsSet(this, TheoryMetric.class.getSimpleName()));
        }
        if (randomGenerator == null) { randomGenerator = new SecureRandom(); }
    }

    @Override
    public Theory performOperation(Collection<? extends Example> targets) throws TheoryRevisionException {
        try {
            Theory theory = learningSystem.getTheory().copy();
            HornClause initialClause = buildEmptyClause(targets);
            if (initialClause == null) { return theory; }

            HornClause hornClause = buildExtendedHornClause(targets, initialClause, new HashSet<>()).getHornClause();
            hornClause = featureGenerator.createFeatureForRule(hornClause, targets);
            theory.add(hornClause);
            return theory;
        } catch (KnowledgeException e) {
            throw new TheoryRevisionException(ExceptionMessages.ERROR_DURING_THEORY_COPY.toString(), e);
        }
    }

    /**
     * Returns a initial empty clause, from the first target example in targets, by putting the head of the clause as
     * the predicate of the example, replacing the terms by new distinct variables.
     *
     * @param targets the target
     * @return the empty clause
     */
    protected static HornClause buildEmptyClause(Iterable<? extends Example> targets) {
        Iterator<? extends Example> iterator = targets.iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        Atom atom = iterator.next().getAtom();
        Atom head = LanguageUtils.toVariableAtom(atom.getPredicate());
        return new HornClause(head, new Conjunction());
    }

    /**
     * Method to build a {@link HornClause}, that improves the metric on the examples, based on the initial clause.
     * This method creates a {@link HornClause} by adding a new literal to the body of the initialClause, if possible.
     * <p>
     * This method should not modify the initial clause nor generate literals equivalent to the, possibly empty,
     * collection of equivalentLiterals.
     *
     * @param examples           the examples
     * @param initialClause      the initial clause
     * @param equivalentLiterals the equivalent literals
     * @return the horn clause
     * @throws TheoryRevisionException in an error occurs during the revision
     */
    @SuppressWarnings("RedundantThrows")
    public abstract AsyncTheoryEvaluator buildExtendedHornClause(Collection<? extends Example> examples,
                                                                 HornClause initialClause,
                                                                 Collection<? extends Literal> equivalentLiterals)
            throws TheoryRevisionException;

    /**
     * Gets the maximum based examples to use in the revision.
     *
     * @return the maximum based examples to use in the revision
     */
    public int getMaximumBasedExamples() {
        return maximumBasedExamples;
    }

    /**
     * Sets the maximum based examples to use in the revision.
     *
     * @param maximumBasedExamples the maximum based examples to use in the revision.
     */
    public void setMaximumBasedExamples(int maximumBasedExamples) {
        this.maximumBasedExamples = maximumBasedExamples;
    }

    /**
     * Gets the examples to base the revision on.
     * <p>
     * It can be all the examples or a subset randomly pick from then.
     *
     * @param examples the possible examples
     * @return the examples to base of the revision on
     */
    protected <K extends Example> List<K> getBasedExamples(Collection<K> examples) {
        List<K> positiveExamples = examples.stream().filter(Example::isPositive).collect(Collectors.toList());
        if (maximumBasedExamples > 0 && maximumBasedExamples < positiveExamples.size()) {
            return FileIOUtils.pickNRandomElements(positiveExamples, maximumBasedExamples, randomGenerator);
        } else {
            return positiveExamples;
        }
    }

    /**
     * Gets the literal candidates from the examples. The literals that are candidates to be appended to the initial
     * clause in order to get it better.
     *
     * @param initialClause    the initial clause
     * @param substitutionGoal the substitution query
     * @param inferredExamples the inferred examples by the substitution query
     * @param skipCandidates   the skip candidates
     * @param connected        if {@code true} only literals connected to the rules will be returned.
     * @return the candidate literal
     */
    @SuppressWarnings("MethodWithTooManyParameters")
    protected Set<Literal> getLiteralCandidatesFromExamples(HornClause initialClause, Atom substitutionGoal,
                                                            Map<Example, Map<Atom, Double>> inferredExamples,
                                                            Set<EquivalentAtom> skipCandidates, boolean connected) {
        Example example;
        Set<Term> constants;
        Set<Atom> relevants;
        Set<Literal> variableRelevants;
        VariableGenerator variableGenerator = new VariableGenerator(substitutionGoal);
        Map<Term, Term> substitutionMap;
        Set<Literal> candidateLiterals = new HashSet<>();
        for (Map.Entry<Example, Map<Atom, Double>> inferredExample : inferredExamples.entrySet()) {
            example = inferredExample.getKey();
            constants = example.getGoalQuery().getTerms().stream().filter(Term::isConstant).collect(Collectors.toSet());
            relevants = learningSystem.relevantsBreadthFirstSearch(constants, relevantsDepth);
            variableRelevants = new HashSet<>();
            for (Atom answer : inferredExample.getValue().keySet()) {
                try {
                    substitutionMap = RelevantLiteralAppendOperator.createSubstitutionMap(substitutionGoal, example,
                                                                                          answer);
                    RelevantLiteralAppendOperator.appendVariableAtomToSet(relevants, variableRelevants,
                                                                          substitutionMap, variableGenerator);
                    HornClauseUtils.buildAllLiteralFromClause(initialClause, variableRelevants, candidateLiterals,
                                                              skipCandidates, connected);
                } catch (InstantiationException | IllegalAccessException e) {
                    RelevantLiteralAppendOperator.logger.debug(ERROR_REVISING_THEORY.toString(), e);
                }
            }
        }
        return candidateLiterals;
    }
}
