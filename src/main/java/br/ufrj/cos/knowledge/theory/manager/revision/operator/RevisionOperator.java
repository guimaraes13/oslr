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

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.TheoryEvaluator;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for changing the {@link Theory}.
 * <p>
 * There are two main types of {@link RevisionOperator}, generalization and specialisation. The former makes the
 * {@link Theory} more generic, proving more atomExamples. The later makes it more
 * specific,
 * proving less atomExamples.
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 */
public abstract class RevisionOperator implements Initializable {

    protected LearningSystem learningSystem;

    /**
     * The external metric of the operator, this metric will be used to evaluated the theory by applying the operator.
     * In addition, a operator might have internal metrics to take internal decision.
     */
    protected TheoryMetric theoryMetric;

    @Override
    public void initialize() throws InitializationException {
        List<String> fields = new ArrayList<>();
        if (learningSystem == null) {
            fields.add(LearningSystem.class.getSimpleName());
        }
        if (theoryMetric == null) {
            fields.add(TheoryMetric.class.getSimpleName());
        }

        if (!fields.isEmpty()) {
            throw new InitializationException(ExceptionMessages.errorFieldsSet(this, fields));
        }
    }

    /**
     * Apply the operation on its {@link Theory} given the target {@link Example}
     *
     * @param targets the targets {@link Example}s
     * @return the {@link Theory}
     * @throws TheoryRevisionException in an error occurs during the revision
     */
    public abstract Theory performOperation(Iterable<? extends Example> targets) throws TheoryRevisionException;

    /**
     * Gets the {@link TheoryMetric}.
     *
     * @return the {@link TheoryMetric}
     */
    public TheoryMetric getTheoryMetric() {
        return theoryMetric;
    }

    /**
     * Sets the {@link TheoryMetric} if it is not yet set. If it is already set, throws an error.
     *
     * @param theoryMetric the {@link TheoryMetric}
     * @throws InitializationException if the {@link TheoryMetric} is already set
     */
    public void setTheoryMetric(TheoryMetric theoryMetric) throws InitializationException {
        if (this.theoryMetric != null) {
            throw new InitializationException(
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                   TheoryMetric.class.getSimpleName()));
        }
        this.theoryMetric = theoryMetric;
    }

    /**
     * Gets the {@link TheoryEvaluator}.
     *
     * @return the {@link TheoryEvaluator}
     */
    public TheoryEvaluator getTheoryEvaluator() {
        return learningSystem.getTheoryEvaluator();
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
     * Method to send a feedback to the revision operator, telling that the revision was accepted, in order to allow
     * it to do something.
     *
     * @param revised the revised theory
     */
    @SuppressWarnings("unused")
    public abstract void theoryRevisionAccepted(Theory revised);

}
