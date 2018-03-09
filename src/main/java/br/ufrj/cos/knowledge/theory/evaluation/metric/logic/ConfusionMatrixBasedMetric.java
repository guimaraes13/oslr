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

package br.ufrj.cos.knowledge.theory.evaluation.metric.logic;

import br.ufrj.cos.engine.EngineSystemTranslator;
import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.logic.Atom;

import java.util.Collection;
import java.util.Map;

/**
 * Template for confusion matrix based metrics. Calculates the confusion matrix of the system given the examples.
 * <p>
 * Created on 08/05/17.
 *
 * @author Victor Guimarães
 */
public abstract class ConfusionMatrixBasedMetric extends TheoryMetric {

    protected int truePositive;
    protected int trueNegative;
    protected int falsePositive;
    protected int falseNegative;

    @Override
    public double evaluate(Map<Example, Map<Atom, Double>> inferredResult, Collection<? extends Example> examples) {
        initializeConfusionMatrix();
        calculateConfusionMatrix(inferredResult, examples);
        return calculateConfusionMatrixMetric();
    }

    /**
     * Initializes the confusion matrix with all the cells as zero.
     */
    protected void initializeConfusionMatrix() {
        this.truePositive = 0;
        this.trueNegative = 0;
        this.falsePositive = 0;
        this.falseNegative = 0;
    }

    /**
     * Calculates the confusion matrix based on the {@link Examples}.
     *
     * @param inferredResult the results from the {@link EngineSystemTranslator}
     * @param examples       the {@link Examples}
     */
    protected void calculateConfusionMatrix(Map<Example, Map<Atom, Double>> inferredResult,
                                            Collection<? extends Example> examples) {
        Map<Atom, Double> atomValues;
        for (Example example : examples) {
            atomValues = inferredResult.get(example);
            for (AtomExample atomExample : example.getGroundedQuery()) {
                incrementMatrixCell(atomValues, atomExample);
            }
        }
    }

    /**
     * Calculates a metric based on the confusion matrix.
     *
     * @return the metric
     */
    protected abstract double calculateConfusionMatrixMetric();

    /**
     * Increments the correspondent matrix cell based on the proved example.
     *
     * @param atomValues  the atom values map
     * @param atomExample the ground example
     */
    protected void incrementMatrixCell(Map<Atom, Double> atomValues, AtomExample atomExample) {
        if (atomValues != null && atomValues.containsKey(atomExample.getAtom())) {
            // the example was proved
            if (atomExample.isPositive()) {
                truePositive++;
            } else {
                falsePositive++;
            }
        } else {
            // the example was not proved
            if (atomExample.isPositive()) {
                falseNegative++;
            } else {
                trueNegative++;
            }
        }
    }

}
