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
import br.ufrj.cos.knowledge.theory.manager.revision.operator.RevisionOperatorEvaluator;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.RevisionOperatorSelector;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;

/**
 * Responsible for applying the revision operator on the {@link br.ufrj.cos.knowledge.theory.Theory}.
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 */
public class RevisionManager implements Initializable {

    protected RevisionOperatorSelector operatorSelector;

    /**
     * Method to select the best suited {@link RevisionOperatorEvaluator} given the examples.
     *
     * @param targets the target {@link Example}s
     * @return the best suited {@link RevisionOperatorEvaluator}
     * @throws TheoryRevisionException in case an error occurs on the revision
     */
    @SuppressWarnings("RedundantThrows")
    public RevisionOperatorEvaluator getBestRevisionOperator(Example... targets) throws TheoryRevisionException {
        return operatorSelector.selectOperator(targets);
    }

    @Override
    public void initialize() throws InitializationException {
        if (operatorSelector == null) {
            throw new InitializationException(
                    ExceptionMessages.errorFieldsSet(this, RevisionOperatorSelector.class.getSimpleName()));
        }
    }

    /**
     * Sets the {@link RevisionOperatorSelector} if it is not yet set. If it is already set, throws an error.
     *
     * @param operatorSelector the {@link RevisionOperatorSelector}
     * @throws InitializationException if the {@link RevisionOperatorSelector} is already set
     */
    public void setOperatorSelector(RevisionOperatorSelector operatorSelector) throws InitializationException {
        if (this.operatorSelector != null) {
            throw new InitializationException(String.format(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                            RevisionOperatorSelector.class.getSimpleName()));
        }
        this.operatorSelector = operatorSelector;
    }

}
