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

import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.ExampleSet;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.RevisionOperatorEvaluator;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.RevisionOperatorSelector;

/**
 * Responsible for applying the revision operator on the {@link br.ufrj.cos.knowledge.theory.Theory}.
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 */
public class RevisionManager {

    @SuppressWarnings("CanBeFinal")
    protected RevisionOperatorSelector operatorSelector;

    /**
     * Constructs with the {@link RevisionOperatorSelector}
     *
     * @param operatorSelector the {@link RevisionOperatorSelector}
     */
    public RevisionManager(RevisionOperatorSelector operatorSelector) {
        this.operatorSelector = operatorSelector;
    }

    /**
     * Method to call the revision on the selected
     * {@link br.ufrj.cos.knowledge.theory.manager.revision.operator.RevisionOperator} and apply the changes on the
     * current {@link Theory}
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     * @param theory        the {@link Theory}
     * @param examples      the {@link ExampleSet}
     * @param targets       the target {@link Example}s
     * @return the revised {@link Theory}
     * @throws TheoryRevisionException in case an error occurs on the revision
     */
    public Theory revise(KnowledgeBase knowledgeBase, Theory theory, ExampleSet examples,
                         Example... targets) throws TheoryRevisionException {
        RevisionOperatorEvaluator operatorEvaluator = operatorSelector.selectOperator(targets);
        return operatorEvaluator.getRevisedTheory(knowledgeBase, theory, examples, targets);
    }

}
