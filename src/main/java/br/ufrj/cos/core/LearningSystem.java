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

import java.util.Map;

/**
 * Responsible for the execution and control of the entire system.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
public class LearningSystem {

    //Theory Manager
    protected final KnowledgeBase knowledgeBase;
    protected final Theory theory;
    protected final Examples examples;
    protected final EngineSystemTranslator engineSystemTranslator;
    /**
     * The {@link KnowledgeBaseManager}.
     */
    public KnowledgeBaseManager knowledgeBaseManager;
    /**
     * The {@link IncomingExampleManager}.
     */
    public IncomingExampleManager incomingExampleManager;
    /**
     * The {@link TheoryRevisionManager}.
     */
    public TheoryRevisionManager theoryRevisionManager;
    /**
     * The {@link TheoryEvaluator}.
     */
    public TheoryEvaluator theoryEvaluator;

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

    // TODO: implement necessary methods!
    // It does not need to do all by itself know, just have the methods to receive the commands from outside sources!
    // In the future, create an extension of this class that have an atomExamples input stream and decides what to do
    // based on that.

    /**
     * Method to call the revision on the {@link Theory} bases on the target {@link Example}s.
     *
     * @param targets the target {@link Example}s
     * @throws TheoryRevisionException in case an error occurs on the revision
     */
    public synchronized void reviseTheory(Example... targets) throws TheoryRevisionException {
        theoryRevisionManager.revise(targets);
    }

    /**
     * Evaluates the {@link Theory} against all the {@link TheoryMetric}s.
     *
     * @return a {@link Map} with the evaluations per metric.
     */
    public Map<Class<? extends TheoryMetric>, Double> evaluate() {
        return theoryEvaluator.evaluate();
    }

    public synchronized double evaluateTheory(Theory theory, TheoryMetric metric, Example... examples) {
        return 0;
    }

    /**
     * Gets the {@link Examples}.
     *
     * @return the {@link Examples}
     */
    public Examples getExamples() {
        return examples;
    }

    public void trainParameters(Example... examples) {
        engineSystemTranslator.trainParameters(examples);
    }

    public void trainParameters(Iterable<? extends Example> examples) {
        engineSystemTranslator.trainParameters(examples);
    }

    public Map<Example, Map<Atom, Double>> inferExamples(Example... examples) {
        return engineSystemTranslator.inferExamples(examples);
    }

    public Map<Example, Map<Atom, Double>> inferExamples(
            Iterable<? extends Example> examples) {
        return engineSystemTranslator.inferExamples(examples);
    }

    public Map<Example, Map<Atom, Double>> inferExampleWithLastParameters(
            Iterable<? extends Example> examples) {
        return engineSystemTranslator.inferExampleTrainingParameters(examples);
    }
}
