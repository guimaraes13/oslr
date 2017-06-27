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
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.util.InitializationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 10/05/17.
 *
 * @author Victor Guimarães
 */
public class SelectFirstRevisionOperator extends RevisionOperatorSelector {

    private List<RevisionOperatorEvaluator> evaluators;

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        if (operatorEvaluators instanceof List) {
            evaluators = (List<RevisionOperatorEvaluator>) operatorEvaluators;
        } else {
            evaluators = new ArrayList<>(operatorEvaluators);
        }
    }

    @Override
    public RevisionOperatorEvaluator selectOperator(Iterable<? extends Example> targets, TheoryMetric metric) {
        return evaluators.get(0);
    }

}
