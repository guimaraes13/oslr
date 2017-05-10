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

package br.ufrj.cos.knowledge.theory.evaluation;

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.HornClause;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for evaluateTheory the theory against the atomExamples set and/or the knowledge base.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
public class TheoryEvaluator {

    protected final LearningSystem learningSystem;
    protected final Iterable<? extends TheoryMetric> theoryMetrics;

    /**
     * Constructs a {@link TheoryEvaluator} with its fields.
     *
     * @param learningSystem the {@link LearningSystem}
     * @param theoryMetrics  a {@link Iterable} of {@link TheoryMetric}
     */
    public TheoryEvaluator(LearningSystem learningSystem, Iterable<? extends TheoryMetric> theoryMetrics) {
        this.learningSystem = learningSystem;
        this.theoryMetrics = theoryMetrics;
    }

    /**
     * Constructs a {@link TheoryEvaluator} with its fields.
     *
     * @param learningSystem the {@link LearningSystem}
     * @param theoryMetrics  an array of {@link TheoryMetric}
     */
    public TheoryEvaluator(LearningSystem learningSystem, TheoryMetric... theoryMetrics) {
        this.learningSystem = learningSystem;
        this.theoryMetrics = Arrays.asList(theoryMetrics);
    }

    /**
     * Evaluates the {@link Theory} against all its {@link TheoryMetric}s.
     *
     * @return a {@link Map} of evaluations per metric
     */
    public Map<Class<? extends TheoryMetric>, Double> evaluate() {
        Map<Class<? extends TheoryMetric>, Double> evaluations = new HashMap<>();
        for (TheoryMetric metric : theoryMetrics) {
            evaluations.put(metric.getClass(), evaluateTheory(metric, learningSystem.getExamples()));
        }

        return evaluations;
    }

    /**
     * Evaluates the {@link Theory} against the represented metric.
     * <p>
     * The parameters and theory changes due the call of this method should not be stored.
     *
     * @param metric   the {@link TheoryMetric}
     * @param examples the {@link Examples}
     * @return the evaluation value
     */
    public double evaluateTheory(TheoryMetric metric, Examples examples) {
        Map<Example, Map<Atom, Double>> evaluationResult;
        if (metric.parametersRetrainedBeforeEvaluate) {
            evaluationResult = learningSystem.inferExampleTrainingParameters(examples);
        } else {
            evaluationResult = learningSystem.inferExamples(examples);
        }

        return metric.evaluate(evaluationResult, examples);
    }

    /**
     * Evaluates the {@link Theory} against the represented metric, appending new {@link HornClause}.
     * <p>
     * The parameters and theory changes due the call of this method should not be stored.
     *
     * @param metric   the {@link TheoryMetric}
     * @param examples the {@link Examples}
     * @param theory   the {@link Theory}
     * @return the evaluation value
     */
    public double evaluateTheory(TheoryMetric metric, Examples examples, Theory theory) {
        Map<Example, Map<Atom, Double>> evaluationResult;
        if (metric.parametersRetrainedBeforeEvaluate) {
            evaluationResult = learningSystem.inferExampleTrainingParameters(theory, examples);
        } else {
            evaluationResult = learningSystem.inferExamples(theory, examples);
        }

        return metric.evaluate(evaluationResult, examples);
    }

    /**
     * Evaluates the {@link Theory} against the represented metric, appending new {@link HornClause}.
     * <p>
     * The parameters and theory changes due the call of this method should not be stored.
     *
     * @param metric        the {@link TheoryMetric}
     * @param examples      the {@link Examples}
     * @param appendClauses new {@link HornClause}s to append to the theory.
     * @return the evaluation value
     */
    public double evaluateTheory(TheoryMetric metric, Examples examples, HornClause... appendClauses) {
        Iterable<HornClause> iterable = Arrays.asList(appendClauses);
        Map<Example, Map<Atom, Double>> evaluationResult;
        if (metric.parametersRetrainedBeforeEvaluate) {
            evaluationResult = learningSystem.inferExampleTrainingParameters(iterable, examples);
        } else {
            evaluationResult = learningSystem.inferExamples(iterable, examples);
        }

        return metric.evaluate(evaluationResult, examples);
    }

}
