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
 * Responsible for selecting the best suited {@link RevisionOperator}(s).
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 */
public abstract class RevisionOperatorSelector implements Initializable {

    protected Collection<RevisionOperatorEvaluator> operatorEvaluators;

    @Override
    public void initialize() throws InitializationException {
        if (operatorEvaluators == null || operatorEvaluators.isEmpty()) {
            throw new InitializationException(
                    ExceptionMessages.errorFieldsSet(this, RevisionOperatorEvaluator.class.getSimpleName()));
        }
        for (RevisionOperatorEvaluator operator : operatorEvaluators) {
            operator.initialize();
        }
    }

    /**
     * Selects the best suited {@link RevisionOperator} to be applied on the {@link Theory}, based on the metric.
     *
     * @param targets the target iterator
     * @param metric  the metric
     * @return the best suited {@link RevisionOperatorEvaluator}
     */
    public abstract RevisionOperatorEvaluator selectOperator(Collection<? extends Example> targets,
                                                             TheoryMetric metric);

    /**
     * Sets the {@link RevisionOperatorEvaluator} set if it is not yet set. If it is already set, throws an error.
     *
     * @param operatorEvaluators the {@link RevisionOperatorEvaluator} set
     * @throws InitializationException if the {@link RevisionOperatorEvaluator} is already set
     */
    public void setOperatorEvaluators(
            Collection<RevisionOperatorEvaluator> operatorEvaluators) throws InitializationException {
        if (isOperatorEvaluatorsSetted()) {
            throw new InitializationException(
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                   RevisionOperatorEvaluator.class.getSimpleName()));
        }
        this.operatorEvaluators = operatorEvaluators;
    }

    /**
     * Checks if the {@link #operatorEvaluators} is setted.
     *
     * @return {@code true} if it is, {@code false} otherwise
     */
    public boolean isOperatorEvaluatorsSetted() {
        return this.operatorEvaluators != null;
    }

}
