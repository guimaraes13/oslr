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

import br.ufrj.cos.knowledge.example.Example;

import java.util.List;

/**
 * Created on 10/05/17.
 *
 * @author Victor Guimarães
 */
public class SelectFirstRevisionOperator extends RevisionOperatorSelector {

    private final List<RevisionOperatorEvaluator> evaluators;

    /**
     * Constructs the class if the minimum required parameters
     *
     * @param operatorEvaluators the {@link RevisionOperatorEvaluator}s
     */
    public SelectFirstRevisionOperator(List<RevisionOperatorEvaluator> operatorEvaluators) {
        super(operatorEvaluators);
        this.evaluators = operatorEvaluators;
    }

    @Override
    public RevisionOperatorEvaluator selectOperator(Example... targets) {
        return evaluators.get(0);
    }

}
