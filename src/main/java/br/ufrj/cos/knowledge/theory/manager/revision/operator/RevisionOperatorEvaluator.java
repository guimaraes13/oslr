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
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;

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
public class RevisionOperatorEvaluator {

    protected final RevisionOperator revisionOperator;
    protected final TheoryMetric evaluationMetric;

    protected Theory updatedTheory;
    protected boolean isEvaluated;
    protected double evaluationValue;

    /**
     * Constructs with the needed parameters
     *
     * @param revisionOperator the {@link RevisionOperator}
     * @param evaluationMetric the {@link TheoryMetric}
     */
    public RevisionOperatorEvaluator(RevisionOperator revisionOperator, TheoryMetric evaluationMetric) {
        this.revisionOperator = revisionOperator;
        this.evaluationMetric = evaluationMetric;
    }

    /**
     * Evaluates a {@link Theory} as {@link RevisionOperator} was applied.
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     * @param theory        the {@link Theory}
     * @param examples      the {@link Examples}
     * @param targets       the target {@link Example}s
     * @return the evaluated value
     * @throws TheoryRevisionException in case an error occurs on the revision
     */
    public double evaluateOperator(KnowledgeBase knowledgeBase, Theory theory, Examples examples,
                                   Example... targets) throws TheoryRevisionException {
        if (!isEvaluated) {
            isEvaluated = true;
            updatedTheory = revisionOperator.performOperation(targets);
            evaluationValue = evaluationMetric.evaluateTheory(knowledgeBase, theory, examples);
        }

        return evaluationValue;
    }

    /**
     * Gets the revised theory. This method is useful because most of the {@link RevisionOperatorEvaluator} needs to
     * previously apply the change before evaluateTheory it. This methods allows it to store the revised
     * {@link Theory} to
     * improve performance.
     * <p>
     * If the {@link Theory} was not created (or stored) it is computed on the call of this method.
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     * @param theory        the {@link Theory}
     * @param examples      the {@link Examples}
     * @param targets       the target {@link Example}s
     * @return the revised {@link Theory}
     * @throws TheoryRevisionException in case an error occurs on the revision
     */
    public Theory getRevisedTheory(KnowledgeBase knowledgeBase, Theory theory, Examples examples,
                                   Example... targets) throws TheoryRevisionException {
        if (isEvaluated) {
            isEvaluated = false;
            return updatedTheory;
        }
        return revisionOperator.performOperation(targets);
    }

}
