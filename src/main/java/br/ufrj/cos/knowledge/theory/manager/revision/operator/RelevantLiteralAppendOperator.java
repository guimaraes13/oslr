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

import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.AsyncTheoryEvaluator;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.logic.*;
import br.ufrj.cos.util.*;
import br.ufrj.cos.util.multithreading.LiteralAppendAsyncTransformer;
import br.ufrj.cos.util.multithreading.MultithreadingEvaluation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static br.ufrj.cos.util.log.RevisionLog.ERROR_REVISING_THEORY;

/**
 * A literal append operator that search for the literal based on the relevant terms from the examples.
 * <p>
 * Created on 24/06/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("CanBeFinal")
public class RelevantLiteralAppendOperator extends LiteralAppendOperator {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * The substitution predicate name
     */
    public static final String SUBSTITUTION_NAME = "sub_";
    /**
     * The predicate of the query to find the substitution of the variables.
     */
    public static final Predicate SUBSTITUTION_PREDICATE = new Predicate(SUBSTITUTION_NAME, Predicate.VAR_ARGS_ARITY);
    /**
     * The maximum number of threads this class is allowed to create.
     */
    public int numberOfThreads = MultithreadingEvaluation.DEFAULT_NUMBER_OF_THREADS;
    /**
     * The maximum amount of time, in seconds, allowed to the evaluation of the {@link HornClause}.
     * <p>
     * By default, is 300 seconds (i.e. 5 minutes).
     */
    public int evaluationTimeout = MultithreadingEvaluation.DEFAULT_EVALUATION_TIMEOUT;

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

    protected MultithreadingEvaluation<Literal> multithreading;
    protected LiteralAppendAsyncTransformer literalTransformer;

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        if (theoryMetric == null) {
            throw new InitializationException(
                    ExceptionMessages.errorFieldsSet(this, TheoryMetric.class.getSimpleName()));
        }
        literalTransformer = new LiteralAppendAsyncTransformer();
        multithreading = new MultithreadingEvaluation<>(learningSystem, theoryMetric, evaluationTimeout,
                                                        literalTransformer);
        multithreading.numberOfThreads = numberOfThreads;
    }

    @Override
    public void theoryRevisionAccepted(Theory revised) {
    }

    @Override
    public AsyncTheoryEvaluator buildExtendedHornClause(Collection<? extends Example> examples,
                                                        HornClause initialClause,
                                                        Collection<? extends Literal> equivalentLiterals)
            throws TheoryRevisionException {
        try {
            HornClause substitutionClause = buildSubstitutionClause(initialClause);
            Set<Example> querySet = buildQueriesFromExamples(examples, initialClause.getHead(),
                                                             substitutionClause.getHead());
            if (querySet.isEmpty()) { return null; }
            Map<Example, Map<Atom, Double>> inferredExamples =
                    learningSystem.inferExamples(Collections.singleton(substitutionClause), querySet);
            Set<EquivalentAtom> skipCandidates = buildSkipCandidates(initialClause, equivalentLiterals);
            Set<Literal> literals = getLiteralCandidatesFromExamples(initialClause, substitutionClause.getHead(),
                                                                     inferredExamples, skipCandidates);
            if (literals.isEmpty()) { return null; }
            literalTransformer.setInitialClause(initialClause);
            return multithreading.getBestClausesFromCandidates(literals, examples);
        } catch (RuntimeException e) {
            logger.trace(ExceptionMessages.ERROR_APPENDING_LITERAL.toString(), e);
        }
        return null;
    }

    /**
     * Creates the clause to find the substitution of variable instantiated by the initial clause.
     *
     * @param initialClause the initial clause
     * @return the clause to find the substitutions
     */
    protected static HornClause buildSubstitutionClause(HornClause initialClause) {
        List<Term> terms = new ArrayList<>();
        appendVariablesFromAtom(initialClause.getHead(), terms);

        Conjunction body;
        if (initialClause.getBody().isEmpty()) {
            body = new Conjunction(Literal.TRUE_LITERAL);
        } else {
            for (Literal literal : initialClause.getBody()) {
                appendVariablesFromAtom(literal, terms);
            }
            body = initialClause.getBody();
        }

        return new HornClause(new Atom(SUBSTITUTION_PREDICATE, terms), body);
    }

    /**
     * Builds the queries from the positive examples to make possible find the substitution of each proved example.
     *
     * @param examples          the examples
     * @param initialClauseHead the initial clause head
     * @param substitutionHead  the substitution clause head
     * @return the queries of examples
     */
    protected static Set<Example> buildQueriesFromExamples(Iterable<? extends Example> examples,
                                                           Atom initialClauseHead, Atom substitutionHead) {
        Set<Example> querySet = new HashSet<>();
        for (Example example : examples) {
            if (!LanguageUtils.isAtomUnifiableToGoal(example.getGoalQuery(), initialClauseHead)) { continue; }
            for (AtomExample atomExample : example.getGroundedQuery()) {
                if (!atomExample.isPositive()) { continue; }
                querySet.add(buildQueryFromExample(substitutionHead, atomExample));
            }
        }
        return querySet;
    }

    /**
     * Builds the set of Equivalent Atom to be skipped during the creation of the candidate literal. This set includes
     * the literal that are already in the initial clause and the literals from the equivalentLiterals collection.
     *
     * @param initialClause      the initial clause
     * @param equivalentLiterals the equivalent literal collection, i.e. the literal from other clause, to avoid
     *                           creating equivalent clauses
     * @return the set of {@link EquivalentAtom}s
     */
    protected static Set<EquivalentAtom> buildSkipCandidates(HornClause initialClause,
                                                             Collection<? extends Literal> equivalentLiterals) {
        Set<Term> fixedTerms = initialClause.getBody().stream().flatMap(l -> l.getTerms().stream())
                .collect(Collectors.toSet());
        fixedTerms.addAll(initialClause.getHead().getTerms());
        Set<EquivalentAtom> skipCandidate = new HashSet<>();
        for (Literal literal : initialClause.getBody()) {
            skipCandidate.add(new EquivalentAtom(literal, fixedTerms));
        }
        if (equivalentLiterals == null) { return skipCandidate; }
        for (Literal literal : equivalentLiterals) {
            skipCandidate.add(new EquivalentAtom(literal, fixedTerms));
        }
        return skipCandidate;
    }

    /**
     * Gets the literal candidates from the examples. The literals that are candidates to be appended to the initial
     * clause in order to get it better.
     *
     * @param initialClause    the initial clause
     * @param substitutionGoal the substitution query
     * @param inferredExamples the inferred examples by the substitution query
     * @param skipCandidates   the skip candidates
     * @return the candidate literal
     */
    protected Set<Literal> getLiteralCandidatesFromExamples(HornClause initialClause, Atom substitutionGoal,
                                                            Map<Example, Map<Atom, Double>> inferredExamples,
                                                            Set<EquivalentAtom> skipCandidates) {
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
                    substitutionMap = createSubstitutionMap(substitutionGoal, example, answer);
                    appendVariableAtomToSet(relevants, variableRelevants, substitutionMap, variableGenerator);
                    HornClauseUtils.buildAllLiteralFromClause(initialClause, variableRelevants, candidateLiterals,
                                                              skipCandidates);
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.debug(ERROR_REVISING_THEORY.toString(), e);
                }
            }
        }
        return candidateLiterals;
    }

    /**
     * Appends the variables from the atom to the append set.
     *
     * @param atom   the atom
     * @param append the append set
     */
    protected static void appendVariablesFromAtom(Atom atom, List<Term> append) {
        for (Term term : atom.getTerms()) {
            if (!term.isConstant() && !append.contains(term)) {
                append.add(term);
            }
        }
    }

    /**
     * Builds a query from the example, in order to find the substitution map for all variables in the initial clause
     * to the constants that satisfies the example.
     *
     * @param head    the head of the initial clause
     * @param example the example
     * @return the query to find the substitutions
     */
    protected static Example buildQueryFromExample(Atom head, AtomExample example) {
        List<Term> terms = new ArrayList<>(head.getArity());
        int i;
        for (i = 0; i < example.getArity(); i++) {
            terms.add(example.getTerms().get(i));
        }
        for (; i < head.getArity(); i++) {
            terms.add(head.getTerms().get(i));
        }
        return new AtomExample(SUBSTITUTION_PREDICATE, terms);
    }

    /**
     * Creates the substitution map to replace the constants from the relevants to their instantiated variable.
     *
     * @param head    the head of the query
     * @param example the queried example
     * @param answers the answer of the example, i.e. the substitution
     * @return the substitution map
     */
    protected static Map<Term, Term> createSubstitutionMap(Atom head, Example example, Atom answers) {
        Map<Term, Term> substitutionMap = new HashMap<>();
        for (int i = 0; i < example.getAtom().getArity(); i++) {
            substitutionMap.put(answers.getTerms().get(i), head.getTerms().get(i));
        }

        return substitutionMap;
    }

    /**
     * Appends the variable form of the initialAtom, as literal, to the set of variableLiterals.
     *
     * @param initialAtoms      the initial atom, i.e. the candidate atoms
     * @param variableLiterals  the variable literals
     * @param variableMap       the variable map
     * @param variableGenerator the variable generator
     * @throws IllegalAccessException if an error occurs when instantiating a new list of {@link Term}s
     * @throws InstantiationException if an error occurs when instantiating a new list of {@link Term}s
     */
    protected static void appendVariableAtomToSet(Set<Atom> initialAtoms, Set<Literal> variableLiterals,
                                                  Map<Term, Term> variableMap, VariableGenerator variableGenerator)
            throws InstantiationException, IllegalAccessException {
        for (Atom atom : initialAtoms) {
            variableLiterals.add(LanguageUtils.toVariableLiteral(atom, variableMap, variableGenerator));
        }
    }

}
