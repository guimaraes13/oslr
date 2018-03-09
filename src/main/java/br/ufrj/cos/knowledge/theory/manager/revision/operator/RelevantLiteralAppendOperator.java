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

package br.ufrj.cos.knowledge.theory.manager.revision.operator;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.AsyncTheoryEvaluator;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.logic.*;
import br.ufrj.cos.util.*;
import br.ufrj.cos.util.multithreading.LiteralAppendAsyncTransformer;
import br.ufrj.cos.util.multithreading.MultithreadingEvaluation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A literal append operator that search for the literal based on the relevant terms from the examples.
 * <p>
 * Created on 24/06/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("CanBeFinal")
public class RelevantLiteralAppendOperator extends LiteralAppendOperator<Literal> {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    protected LiteralAppendAsyncTransformer<Object> literalTransformer;

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

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        if (generateFeatureBeforeEvaluate) {
            literalTransformer = new LiteralAppendAsyncTransformer(featureGenerator);
        } else {
            literalTransformer = new LiteralAppendAsyncTransformer();
        }
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
            HornClause substitutionClause = HornClauseUtils.buildSubstitutionClause(initialClause);
            Set<Example> querySet = HornClauseUtils.buildQueriesFromExamples(getBasedExamples(examples),
                                                                             initialClause.getHead(),
                                                                             substitutionClause.getHead(),
                                                                             true);
            if (querySet.isEmpty()) { return null; }
            Map<Example, Map<Atom, Double>> inferredExamples =
                    learningSystem.inferExamples(Collections.singleton(substitutionClause), querySet);
            Set<EquivalentAtom> skipCandidates = buildSkipCandidates(initialClause, equivalentLiterals);
            Set<Literal> literals = getLiteralCandidatesFromExamples(initialClause, substitutionClause.getHead(),
                                                                     inferredExamples, skipCandidates, true);
            if (literals.isEmpty()) { return null; }
            literalTransformer.setInitialClause(initialClause);
            return multithreading.getBestClausesFromCandidates(literals, examples);
        } catch (RuntimeException e) {
            logger.trace(ExceptionMessages.ERROR_APPENDING_LITERAL.toString(), e);
        }
        return null;
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

}
