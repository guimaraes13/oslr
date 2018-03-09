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

import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.AsyncTheoryEvaluator;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.logic.*;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.HornClauseUtils;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.multithreading.ConjunctionAppendAsyncTransformer;
import br.ufrj.cos.util.multithreading.MultithreadingEvaluation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * A literal append operator that search for the literal based on the relevant terms from the examples.
 * <p>
 * Created on 24/06/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("CanBeFinal")
public class PathFindAppendOperator extends LiteralAppendOperator<Set<? extends Literal>> {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    /**
     * The default index of the term to be the destination of the path.
     */
    public static final int DEFAULT_DESTINATION_INDEX = 1;
    /**
     * The maximum length of the path, i.e. no maximum length.
     */
    public static final int DEFAULT_MAXIMUM_PATH_LENGTH = -1;

    /**
     * The index of the term to be the destination of the path.
     */
    public int destinationIndex = DEFAULT_DESTINATION_INDEX;
    /**
     * The maximum length of the path.
     */
    public int maximumPathLength = DEFAULT_MAXIMUM_PATH_LENGTH;

    protected ConjunctionAppendAsyncTransformer<Object> conjunctionTransformer;

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        if (generateFeatureBeforeEvaluate) {
            conjunctionTransformer = new ConjunctionAppendAsyncTransformer(featureGenerator);
        } else {
            conjunctionTransformer = new ConjunctionAppendAsyncTransformer();
        }
        multithreading = new MultithreadingEvaluation<>(learningSystem, theoryMetric, evaluationTimeout,
                                                        conjunctionTransformer);
        multithreading.numberOfThreads = numberOfThreads;
        setMaximumBasedExamples(1);
    }

    @SuppressWarnings("OverlyCoupledMethod")
    @Override
    public AsyncTheoryEvaluator buildExtendedHornClause(Collection<? extends Example> examples,
                                                        HornClause initialClause,
                                                        Collection<? extends Literal> equivalentLiterals)
            throws TheoryRevisionException {
        try {
            HornClause substitutionClause = HornClauseUtils.buildSubstitutionClause(initialClause);
            final Atom head = initialClause.getHead();
            Set<Example> querySet = HornClauseUtils.buildQueriesFromExamples(getBasedExamples(examples), head,
                                                                             substitutionClause.getHead(), true);
            if (querySet.isEmpty()) { return null; }
            Map<Example, Map<Atom, Double>> inferredExamples =
                    learningSystem.inferExamples(Collections.singleton(substitutionClause), querySet);
            Set<Literal> literals = getLiteralCandidatesFromExamples(initialClause, substitutionClause.getHead(),
                                                                     inferredExamples, new HashSet<>(), false);
            if (literals.isEmpty()) { return null; }
            KnowledgeBase knowledgeBase = new KnowledgeBase(new HashSet<>(literals));
            Collection<Term[]> paths = knowledgeBase.shortestPath(head.getTerms().get(0),
                                                                  head.getTerms().get(destinationIndex),
                                                                  maximumPathLength);
            if (paths == null || paths.isEmpty()) { return null; }
            Collection<Conjunction> conjunctions = new HashSet<>();
            paths.forEach(path -> pathToRules(path, knowledgeBase, conjunctions));
            conjunctionTransformer.setInitialClause(initialClause);
            return multithreading.getBestClausesFromCandidates(conjunctions, examples);
        } catch (RuntimeException e) {
            logger.trace(ExceptionMessages.ERROR_APPENDING_LITERAL.toString(), e);
        }
        return null;
    }

    /**
     * Creates rules with the body being the path between two terms in a knowledge base.
     *
     * @param path          the path
     * @param knowledgeBase the knowledge base
     * @param append        a collection of rules to append
     * @return the collection of rules
     */
    @SuppressWarnings("OverlyLongMethod")
    public static Collection<? super Conjunction> pathToRules(Term[] path, KnowledgeBase knowledgeBase,
                                                              Collection<? super Conjunction> append) {
        Set<Atom> currentEdges = new HashSet<>(knowledgeBase.getAtomsWithTerm(path[0]));
        Set<Atom> nextEdges;
        int pathLength = path.length - 1;
        Queue<Conjunction> queue = new ArrayDeque<>();
        queue.add(new Conjunction(pathLength));
        Conjunction currentArray;
        Conjunction auxiliary;
        int size;
        for (int i = 0; i < pathLength; i++) {
            size = queue.size();
            nextEdges = new HashSet<>(knowledgeBase.getAtomsWithTerm(path[i + 1]));
            currentEdges.retainAll(nextEdges);
            for (int j = 0; j < size; j++) {
                currentArray = queue.poll();
                for (Atom edge : currentEdges) {
                    auxiliary = new Conjunction(currentArray);
                    auxiliary.add(new Literal(edge));
                    queue.add(auxiliary);
                }
            }
            currentEdges = nextEdges;
        }

        append.addAll(queue);
        return append;
    }

    @Override
    public void theoryRevisionAccepted(Theory revised) {
    }

}
