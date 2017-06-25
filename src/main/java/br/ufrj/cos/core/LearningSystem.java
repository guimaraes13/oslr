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

package br.ufrj.cos.core;

import br.ufrj.cos.engine.EngineSystemTranslator;
import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.manager.IncomingExampleManager;
import br.ufrj.cos.knowledge.manager.KnowledgeBaseManager;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.TheoryEvaluator;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.manager.TheoryRevisionManager;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.Term;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsible for the execution and control of the entire system.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
public class LearningSystem {

    /**
     * Represents a constant for no maximum depth on the transitivity of the relevant concept.
     */
    public static final int NO_MAXIMUM_DEPTH = -1;

    //Theory Manager
    protected final KnowledgeBase knowledgeBase;
    protected final Examples examples;
    protected final EngineSystemTranslator engineSystemTranslator;
    /**
     * The {@link KnowledgeBaseManager}.
     */
    @SuppressWarnings("unused")
    public KnowledgeBaseManager knowledgeBaseManager;
    /**
     * The {@link IncomingExampleManager}.
     */
    public IncomingExampleManager incomingExampleManager;
    /**
     * The {@link TheoryEvaluator}.
     */
    public TheoryEvaluator theoryEvaluator;
    /**
     * The {@link TheoryRevisionManager}.
     */
    public TheoryRevisionManager theoryRevisionManager;
    /**
     * If the system will be executed in parallel and thread access control will be necessary.
     * <p>
     * If it is {@code true}, thread local instances of the {@link EngineSystemTranslator} will be passed on methods
     * that evaluates examples retraining parameters or changing the {@link Theory}.
     */
    public boolean concurrent = false;
    protected Theory theory;

    /**
     * Constructs the class if the minimum required parameters.
     *
     * @param knowledgeBase          the {@link KnowledgeBase}
     * @param theory                 the {@link Theory}
     * @param examples               the {@link Examples}
     * @param engineSystemTranslator the {@link EngineSystemTranslator}
     */
    public LearningSystem(KnowledgeBase knowledgeBase, Theory theory, Examples examples,
                          EngineSystemTranslator engineSystemTranslator) {
        this.knowledgeBase = knowledgeBase;
        this.theory = theory;
        this.examples = examples;
        this.engineSystemTranslator = engineSystemTranslator;
    }

    /**
     * Method to call the revision on the {@link Theory} bases on the target {@link Example}s.
     *
     * @param targets the target {@link Example}s
     * @throws TheoryRevisionException in case an error occurs on the revision
     */
    public synchronized void reviseTheory(Collection<? extends Example> targets) throws TheoryRevisionException {
        theoryRevisionManager.revise(targets);
    }

    /**
     * Evaluates the {@link Theory} against all the {@link TheoryMetric}s.
     *
     * @return a {@link Map} with the evaluations per metric.
     */
    public Map<TheoryMetric, Double> evaluate() {
        return theoryEvaluator.evaluate();
    }

    /**
     * Evaluates the {@link Theory} using the given {@link TheoryMetric}.
     *
     * @param metric the {@link TheoryMetric}
     * @return the evaluation
     */
    public synchronized double evaluateTheory(TheoryMetric metric) {
        return theoryEvaluator.evaluateTheory(metric, examples);
    }

    /**
     * Gets the {@link Examples}.
     *
     * @return the {@link Examples}
     */
    public Examples getExamples() {
        return examples;
    }

    /**
     * Delegates the grounding of the examples to the {@link EngineSystemTranslator}.
     *
     * @param examples the {@link Example}s
     * @return the grounds
     */
    public Set<Atom> groundExamples(Example... examples) {
        return getEngineSystemTranslator().groundExamples(examples);
    }

    /**
     * Gets the {@link EngineSystemTranslator}.
     *
     * @return the {@link EngineSystemTranslator}
     */
    protected EngineSystemTranslator getEngineSystemTranslator() {
        if (concurrent) {
            return engineSystemTranslator.get();
        } else {
            return engineSystemTranslator;
        }
    }

    /**
     * Delegates the training of the parameters to the {@link EngineSystemTranslator}.
     *
     * @param examples the {@link Example}s
     */
    public void trainParameters(Example... examples) {
        engineSystemTranslator.trainParameters(examples);
    }

    /**
     * Delegates the training of the parameters to the {@link EngineSystemTranslator}.
     *
     * @param examples the {@link Example}s
     */
    public void trainParameters(Iterable<? extends Example> examples) {
        engineSystemTranslator.trainParameters(examples);
    }

    /**
     * Saves the trained parameters.
     */
    public void saveTrainedParameters() {
        engineSystemTranslator.saveTrainedParameters();
    }

    /**
     * Delegates the inference of the examples to the {@link EngineSystemTranslator}.
     *
     * @param examples the {@link Example}s
     * @return the {@link Map} of results.
     */
    public Map<Example, Map<Atom, Double>> inferExamples(Example... examples) {
        return engineSystemTranslator.inferExamples(examples);
    }

    /**
     * Delegates the inference of the examples to the {@link EngineSystemTranslator}.
     *
     * @param examples the {@link Example}s
     * @return the {@link Map} of results.
     */
    public Map<Example, Map<Atom, Double>> inferExamples(
            Iterable<? extends Example> examples) {
        return engineSystemTranslator.inferExamples(examples);
    }

    /**
     * Delegates the inference of the examples, with {@link Theory} modifications, to the
     * {@link EngineSystemTranslator}.
     * <p>
     * This method do not change the {@link Theory} nor the internal parameters of the {@link EngineSystemTranslator}.
     *
     * @param theory   the {@link Theory}
     * @param examples the {@link Example}s
     * @return the {@link Map} of results.
     */
    public Map<Example, Map<Atom, Double>> inferExamples(Theory theory,
                                                         Iterable<? extends Example> examples) {
        return getEngineSystemTranslator().inferExamples(theory, examples);
    }

    /**
     * Method to infer the probability of the examples based on the {@link HornClause}, {@link KnowledgeBase} and the
     * parameters from the logic engine. The parameters changes due the call of this method should not be stored.
     * <p>
     * This method is useful to evaluate a clause revision without save the parameters.
     *
     * @param clause   the {@link HornClause}
     * @param examples the iterable to infer
     * @return a {@link Map} of the solutions to its correspondent {@link Example}s.
     */
    public Map<Example, Map<Atom, Double>> inferExamples(HornClause clause,
                                                         Iterable<? extends Example> examples) {
        return engineSystemTranslator.inferExamples(clause, examples);
    }

    /**
     * Delegates the inference of the examples, with {@link Theory} modifications, to the
     * {@link EngineSystemTranslator}.
     * <p>
     * This method do not change the {@link Theory} nor the internal parameters of the {@link EngineSystemTranslator}.
     *
     * @param appendClauses clauses to be appended to the {@link Theory}
     * @param examples      the {@link Example}s
     * @return the {@link Map} of results.
     */
    public Map<Example, Map<Atom, Double>> inferExamples(
            Iterable<? extends HornClause> appendClauses,
            Iterable<? extends Example> examples) {
        return getEngineSystemTranslator().inferExamples(appendClauses, examples);
    }

    /**
     * Delegates the inference of the examples to the {@link EngineSystemTranslator}.
     * <p>
     * In addition, the parameters are trained before the inference.
     * <p>
     * This method do not change the {@link Theory} nor the internal parameters of the {@link EngineSystemTranslator}.
     *
     * @param examples the {@link Example}s
     * @return the {@link Map} of results.
     */
    public Map<Example, Map<Atom, Double>> inferExampleTrainingParameters(
            Iterable<? extends Example> examples) {
        return getEngineSystemTranslator().inferExampleTrainingParameters(examples);
    }

    /**
     * Delegates the inference of the examples, with {@link Theory} modifications, to the
     * {@link EngineSystemTranslator}.
     * <p>
     * In addition, the parameters are trained before the inference.
     * <p>
     * This method do not change the {@link Theory} nor the internal parameters of the {@link EngineSystemTranslator}.
     *
     * @param appendClauses clauses to be appended to the {@link Theory}
     * @param examples      the {@link Example}s
     * @return the {@link Map} of results.
     */
    public Map<Example, Map<Atom, Double>> inferExampleTrainingParameters(
            Iterable<? extends HornClause> appendClauses,
            Iterable<? extends Example> examples) {
        return getEngineSystemTranslator().inferExampleTrainingParameters(appendClauses, examples);
    }

    /**
     * Delegates the inference of the examples, with {@link Theory} modifications, to the
     * {@link EngineSystemTranslator}.
     * <p>
     * In addition, the parameters are trained before the inference.
     * <p>
     * This method do not change the {@link Theory} nor the internal parameters of the {@link EngineSystemTranslator}.
     *
     * @param theory   the {@link Theory}
     * @param examples the {@link Example}s
     * @return the {@link Map} of results.
     */
    public Map<Example, Map<Atom, Double>> inferExampleTrainingParameters(Theory theory,
                                                                          Iterable<? extends Example> examples) {
        return getEngineSystemTranslator().inferExampleTrainingParameters(theory, examples);
    }

    /**
     * Gets the relevant {@link Atom}s, given the relevant seed {@link Term}s, by performing a breadth-first search
     * on the {@link KnowledgeBase}'s cached graph
     *
     * @param terms          the seed {@link Term}s
     * @param relevantsDepth the depth of the relevant breadth first search
     * @return the relevant {@link Atom}s to the seed {@link Term}s
     */
    public Set<Atom> relevantsBreadthFirstSearch(Collection<Term> terms, int relevantsDepth) {
        return relevantsBreadthFirstSearch(terms, relevantsDepth, false);
    }

    /**
     * Gets the relevant {@link Atom}s, given the relevant seed {@link Term}s, by performing a breadth-first search
     * on the {@link KnowledgeBase}'s cached graph
     *
     * @param terms          the seed {@link Term}s
     * @param relevantsDepth the depth of the relevant breadth first search
     * @param safeStop       if is to stop the search when the found atoms is sufficient to make the terms safe
     * @return the relevant {@link Atom}s to the seed {@link Term}s
     */
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    public Set<Atom> relevantsBreadthFirstSearch(Iterable<? extends Term> terms, int relevantsDepth, boolean safeStop) {
        Map<Term, Integer> termDistance = new HashMap<>();
        Queue<Term> queue = new ArrayDeque<>();
        Set<Atom> atoms = new HashSet<>();
        Set<Term> currentRelevants = new HashSet<>();
        Set<Term> headTerms = new HashSet<>();
        Set<Term> bodyTerms = new HashSet<>();

        for (Term term : terms) {
            termDistance.put(term, 0);
            queue.add(term);
            currentRelevants.add(term);
            headTerms.add(term);
        }

        Set<Atom> atomSet;
        Term currentTerm;
        Integer currentDistance;
        Integer previousDistance = 0;
        while (!queue.isEmpty()) {
            currentTerm = queue.poll();
            currentDistance = termDistance.get(currentTerm);

            if (!Objects.equals(currentDistance, previousDistance)) {
                atomSet = groundRelevants(currentRelevants);
                atoms.addAll(atomSet);
                if (safeStop) {
                    // if is to safe the rule, i.e. the minimal safe rule will be returned, so there is no point in
                    // adding more atom beyond that. The bodyTerm keeps the already found term so it can checks when
                    // this method can stop early
                    atomSet.stream().flatMap(a -> a.getTerms().stream()).forEach(bodyTerms::add);
                }
                currentRelevants = new HashSet<>();
                previousDistance = currentDistance;
            }
            if (!termDistance.containsKey(currentTerm)) {
                currentRelevants.add(currentTerm);
            }

            atomSet = getKnowledgeBase().getAtomsWithTerm(currentTerm);
            atoms.addAll(atomSet);
            if (safeStop) {
                // if is not to safeStop and all the head term are already added to the atom set, we can stop the search
                bodyTerms.addAll(atomSet.stream().flatMap(a -> a.getTerms().stream()).collect(Collectors.toSet()));
                if (bodyTerms.containsAll(headTerms)) { break; }
            }
            if (relevantsDepth == NO_MAXIMUM_DEPTH || currentDistance < relevantsDepth) {
                for (Term neighbour : getKnowledgeBase().getTermNeighbours(currentTerm)) {
                    if (!termDistance.containsKey(neighbour)) {
                        termDistance.put(neighbour, currentDistance + 1);
                        queue.add(neighbour);
                    }
                }
            }
        }

        return atoms;
    }

    /**
     * Delegates the grounding of the relevants to the {@link EngineSystemTranslator}.
     *
     * @param terms the {@link Term}s
     * @return the grounds
     */
    public Set<Atom> groundRelevants(Collection<Term> terms) {
        return getEngineSystemTranslator().groundRelevants(terms);
    }

    /**
     * Gets the {@link KnowledgeBase}.
     *
     * @return the {@link KnowledgeBase}
     */
    public KnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }

    /**
     * Gets the {@link Theory}.
     *
     * @return the {@link Theory}
     */
    public Theory getTheory() {
        return theory;
    }

    /**
     * Sets the {@link Theory}.
     *
     * @param theory the {@link Theory}
     */
    public void setTheory(Theory theory) {
        this.theory = theory;
        this.engineSystemTranslator.setTheory(theory);
    }

    /**
     * Gets the {@link TheoryEvaluator}.
     *
     * @return the {@link TheoryEvaluator}
     */
    public TheoryEvaluator getTheoryEvaluator() {
        return theoryEvaluator;
    }

    /**
     * Gets the {@link IncomingExampleManager}.
     *
     * @return the {@link IncomingExampleManager}
     */
    public IncomingExampleManager getIncomingExampleManager() {
        return incomingExampleManager;
    }

    /**
     * Saves the {@link EngineSystemTranslator}'s parameters into files within the working directory.
     *
     * @param workingDirectory the working directory
     */
    public void saveParameters(File workingDirectory) {
        engineSystemTranslator.saveParameters(workingDirectory);
    }
}
