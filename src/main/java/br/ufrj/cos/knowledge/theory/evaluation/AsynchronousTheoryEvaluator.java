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

package br.ufrj.cos.knowledge.theory.evaluation;

import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.ExampleSet;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryMetric;

/**
 * Handle a asynchronous execution of evaluation a {@link Theory}. This is useful when a maximum amount of time is
 * specified for the task.
 * <p>
 * Created on 29/04/17.
 *
 * @author Victor Guimarães
 */
public class AsynchronousTheoryEvaluator extends Thread {

    protected KnowledgeBase knowledgeBase;
    protected Theory theory;
    protected ExampleSet examples;

    protected TheoryMetric theoryMetric;

    protected double evaluation;

    /**
     * Constructor with the needed parameters.
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     * @param theory        the {@link Theory}
     * @param examples      the {@link ExampleSet}
     * @param theoryMetric  the {@link TheoryMetric}
     */
    public AsynchronousTheoryEvaluator(KnowledgeBase knowledgeBase, Theory theory, ExampleSet examples,
                                       TheoryMetric theoryMetric) {
        this.knowledgeBase = knowledgeBase;
        this.theory = theory;
        this.examples = examples;
        this.theoryMetric = theoryMetric;
        this.evaluation = theoryMetric.getDefaultValue();
    }

    @Override
    public void run() {
        evaluation = theoryMetric.evaluateTheory(knowledgeBase, theory, examples);
    }

    /**
     * Gets the evaluated value.
     *
     * @return the evaluated value
     */
    public double getEvaluation() {
        return evaluation;
    }

}
