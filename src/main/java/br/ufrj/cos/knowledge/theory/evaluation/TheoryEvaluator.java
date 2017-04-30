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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for evaluate the theory against the atomExamples set and/or the knowledge base.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("CanBeFinal")
public class TheoryEvaluator {

    protected KnowledgeBase knowledgeBase;
    protected Theory theory;
    protected ExampleSet examples;

    protected Set<TheoryMetric> theoryMetrics;

    /**
     * Constructs a {@link TheoryEvaluator} with its fields.
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     * @param theory        the {@link Theory}
     * @param examples      the {@link ExampleSet}
     * @param theoryMetrics a {@link Set} of {@link TheoryMetric}
     */
    public TheoryEvaluator(KnowledgeBase knowledgeBase, Theory theory, ExampleSet examples,
                           Set<TheoryMetric> theoryMetrics) {
        this.knowledgeBase = knowledgeBase;
        this.theory = theory;
        this.examples = examples;
        this.theoryMetrics = theoryMetrics;
    }

    /**
     * Evaluates a {@link Theory}.
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     * @param theory        the {@link Theory}
     * @param examples      the {@link ExampleSet}
     * @param theoryMetric  the {@link TheoryMetric}
     * @return the evaluation value
     */
    public static double evaluateTheory(KnowledgeBase knowledgeBase, Theory theory, ExampleSet examples,
                                        TheoryMetric theoryMetric) {
        return theoryMetric.evaluateTheory(knowledgeBase, theory, examples);
    }

    /**
     * Evaluates the {@link Theory} against all its {@link TheoryMetric}s.
     *
     * @return a {@link Map} of evaluations per metric
     */
    public Map<Class<? extends TheoryMetric>, Double> evaluate() {
        Map<Class<? extends TheoryMetric>, Double> evaluations = new HashMap<>();
        for (TheoryMetric metric : theoryMetrics) {
            evaluations.put(metric.getClass(), metric.evaluateTheory(knowledgeBase, theory, examples));
        }

        return evaluations;
    }

    /**
     * Gets the {@link KnowledgeBase}.
     *
     * @return the {@link KnowledgeBase}
     */
    public KnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }

    /**
     * Gets the {@link Theory}.
     *
     * @return the {@link Theory}
     */
    public Theory getTheory() {
        return theory;
    }

    /**
     * Gets the {@link ExampleSet}.
     *
     * @return the {@link ExampleSet}
     */
    public ExampleSet getExamples() {
        return examples;
    }

    /**
     * Gets the {@link Set} of {@link TheoryMetric}s.
     *
     * @return the {@link Set} of {@link TheoryMetric}s
     */
    public Set<TheoryMetric> getTheoryMetrics() {
        return theoryMetrics;
    }

}
