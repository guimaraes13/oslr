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
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;

import java.util.*;

/**
 * Responsible for evaluateTheory the theory against the atomExamples set and/or the knowledge base.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
public class TheoryEvaluator implements Initializable {

    protected LearningSystem learningSystem;
    protected Iterable<? extends TheoryMetric> theoryMetrics;

    /**
     * Default constructor to be in compliance to {@link Initializable} interface.
     */
    public TheoryEvaluator() {
    }

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

    @Override
    public void initialize() throws InitializationException {
        List<String> fields = new ArrayList<>();
        if (learningSystem == null) {
            fields.add(LearningSystem.class.getSimpleName());
        }
        if (theoryMetrics == null) {
            fields.add(TheoryMetric.class.getSimpleName());
        }

        if (fields.size() > 0) {
            throw new InitializationException(ExceptionMessages.errorFieldsSet(this, fields));
        }
    }

    /**
     * Evaluates the {@link Theory} against all its {@link TheoryMetric}s.
     *
     * @return a {@link Map} of evaluations per metric
     */
    public Map<TheoryMetric, Double> evaluate() {
        Map<TheoryMetric, Double> evaluations = new HashMap<>();
        Examples examples = learningSystem.getExamples();
        Map<Example, Map<Atom, Double>> inferredExamples = inferExamples(examples, false);
        for (TheoryMetric metric : theoryMetrics) {
            evaluations.put(metric, metric.evaluate(inferredExamples, examples));
        }

        return evaluations;
    }

    /**
     * Infers the examples with or without retraining the parameters.
     *
     * @param examples the examples
     * @param retrain  if it is to retrain the parameters
     * @return the inferred examples
     */
    protected Map<Example, Map<Atom, Double>> inferExamples(Examples examples, boolean retrain) {
        Map<Example, Map<Atom, Double>> evaluationResult;
        if (retrain) {
            evaluationResult = learningSystem.inferExampleTrainingParameters(examples);
        } else {
            evaluationResult = learningSystem.inferExamples(examples);
        }
        return evaluationResult;
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
        return metric.evaluate(inferExamples(examples, metric.parametersRetrainedBeforeEvaluate), examples);
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

    /**
     * Sets the {@link LearningSystem} if it is not yet set. If it is already set, throws an error.
     *
     * @param learningSystem the {@link LearningSystem}
     * @throws InitializationException if the {@link LearningSystem} is already set
     */
    public void setLearningSystem(LearningSystem learningSystem) throws InitializationException {
        if (this.learningSystem != null) {
            throw new InitializationException(
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                   LearningSystem.class.getSimpleName()));
        }
        this.learningSystem = learningSystem;
    }

    /**
     * Sets the {@link TheoryMetric} set if it is not yet set. If it is already set, throws an error.
     *
     * @param theoryMetrics the {@link TheoryMetric} set
     * @throws InitializationException if the {@link TheoryMetric} set is already set
     */
    public void setTheoryMetrics(
            Iterable<? extends TheoryMetric> theoryMetrics) throws InitializationException {
        if (this.theoryMetrics != null) {
            throw new InitializationException(
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                   TheoryMetric.class.getSimpleName()));
        }
        this.theoryMetrics = theoryMetrics;
    }
}
