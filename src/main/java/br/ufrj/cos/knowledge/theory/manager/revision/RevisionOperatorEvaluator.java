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

package br.ufrj.cos.knowledge.theory.manager.revision;

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.RevisionOperator;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;

import java.util.Collection;

/**
 * Responsible for evaluating an specific {@link RevisionOperator}.
 * <p>
 * This class is not thread-safe, if you want to perform multiple evaluations using the same {@link RevisionOperator}
 * with the same {@link TheoryMetric} in different {@link Theory}(is), please create a instance of this class for each
 * thread.
 * <p>
 * This class may cache the updated {@link Theory}, depends on the implementation.
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 */
public class RevisionOperatorEvaluator implements Initializable {

    protected RevisionOperator revisionOperator;

    protected Theory updatedTheory;
    protected boolean isEvaluated;
    protected double evaluationValue;

    /**
     * Constructs a {@link RevisionOperatorEvaluator} with its fields.
     *
     * @param revisionOperator the {@link RevisionOperator}
     */
    public RevisionOperatorEvaluator(RevisionOperator revisionOperator) {
        this.revisionOperator = revisionOperator;
    }

    /**
     * Default constructor to be in compliance to {@link Initializable} interface.
     */
    public RevisionOperatorEvaluator() {
    }

    @Override
    public void initialize() throws InitializationException {
        revisionOperator.initialize();
    }

    /**
     * Evaluates a {@link Theory} as {@link RevisionOperator} was applied, based on the metric.
     *
     * @param targets the target {@link Example}s
     * @param metric  the metric
     * @return the evaluated value
     * @throws TheoryRevisionException in case an error occurs on the revision
     */
    public double evaluateOperator(Collection<? extends Example> targets,
                                   TheoryMetric metric) throws TheoryRevisionException {
        if (!isEvaluated) {
            updatedTheory = revisionOperator.performOperation(targets);
            isEvaluated = true;
            if (updatedTheory == null) { return metric.getDefaultValue(); }
            evaluationValue = revisionOperator.getTheoryEvaluator().evaluateTheory(metric, updatedTheory, targets);
        }

        return evaluationValue;
    }

    /**
     * Gets the revised theory. This method is useful because most of the {@link RevisionOperatorEvaluator} needs to
     * previously apply the change before evaluateTheory it. This methods allows it to store the revised
     * {@link Theory} to improve performance.
     * <p>
     * If the {@link Theory} was not created (or stored) it is computed on the call of this method.
     *
     * @param targets the target {@link Example}s
     * @return the revised {@link Theory}
     * @throws TheoryRevisionException in case an error occurs on the revision
     */
    public Theory getRevisedTheory(Collection<? extends Example> targets) throws TheoryRevisionException {
        if (isEvaluated) {
            isEvaluated = false;
            return updatedTheory;
        }
        return revisionOperator.performOperation(targets);
    }

    /**
     * Sets the {@link LearningSystem} if it is not yet set. If it is already set, throws an error.
     *
     * @param learningSystem the {@link LearningSystem}
     * @throws InitializationException if the {@link LearningSystem} is already set
     */
    public void setLearningSystem(LearningSystem learningSystem) throws InitializationException {
        revisionOperator.setLearningSystem(learningSystem);
    }

    /**
     * Gets the {@link RevisionOperator}.
     *
     * @return the {@link RevisionOperator}
     */
    public RevisionOperator getRevisionOperator() {
        return revisionOperator;
    }

    /**
     * Sets the {@link RevisionOperator} if it is not yet set. If it is already set, throws an error.
     *
     * @param revisionOperator the {@link RevisionOperator}
     * @throws InitializationException if the {@link RevisionOperator} is already set
     */
    public void setRevisionOperator(RevisionOperator revisionOperator) throws InitializationException {
        if (this.revisionOperator != null) {
            throw new InitializationException(
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                   RevisionOperator.class.getSimpleName()));
        }
        this.revisionOperator = revisionOperator;
    }

    /**
     * Clears the revised theory.
     */
    public void clearCachedTheory() {
        isEvaluated = false;
    }

    /**
     * Method to send a feedback to the revision operator, telling that the revision was accepted, in order to allow
     * it to do something.
     *
     * @param revised the revised theory
     */
    public void theoryRevisionAccepted(Theory revised) {
        revisionOperator.theoryRevisionAccepted(revised);
    }

}
