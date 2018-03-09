/*
 * Online Structure Learner by Revision (OSLR) is an online relational
 * learning algorithm that can handle continuous, open-ended
 * streams of relational examples as they arrive. We employ
 * techniques from theory revision to take advantage of the already
 * acquired knowledge as a starting point, find where it should be
 * modified to cope with the new examples, and automatically update it.
 * We rely on the Hoeffding's bound statistical theory to decide if the
 * model must in fact be updated accordingly to the new examples.
 * The system is built upon ProPPR statistical relational language to
 * describe the induced models, aiming at contemplating the uncertainty
 * inherent to real data.
 *
 * Copyright (C) 2017-2018 Victor Guimarães
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
import java.util.Collection;
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
    public RevisionOperatorEvaluator selectOperator(Collection<? extends Example> targets, TheoryMetric metric) {
        return evaluators.get(0);
    }

}
