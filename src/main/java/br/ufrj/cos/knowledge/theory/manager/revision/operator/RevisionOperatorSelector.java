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

import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.theory.Theory;

import java.util.Collection;

/**
 * Responsible for selecting the best suited {@link RevisionOperator}(s).
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 */
public abstract class RevisionOperatorSelector {

    protected KnowledgeBase knowledgeBase;
    protected Theory theory;
    protected Examples examples;

    protected Collection<RevisionOperatorEvaluator> operatorEvaluators;

    /**
     * Constructs the class if the minimum required parameters
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     * @param theory        the {@link Theory}
     * @param examples      the {@link Examples}
     */
    public RevisionOperatorSelector(KnowledgeBase knowledgeBase, Theory theory, Examples examples) {
        this.knowledgeBase = knowledgeBase;
        this.theory = theory;
        this.examples = examples;
    }

    /**
     * Selects the best suited {@link RevisionOperator} to be applied on the {@link Theory}
     *
     * @param targets the target examples
     * @return the best suited {@link RevisionOperatorEvaluator}
     */
    public abstract RevisionOperatorEvaluator selectOperator(Example... targets);

}
