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

package br.ufrj.cos.knowledge.theory.evaluation.metric.probabilistic;

import br.ufrj.cos.engine.EngineSystemTranslator;
import br.ufrj.cos.knowledge.example.AtomExample;

/**
 * Calculates the likelihood of a theory, given the examples.
 * <p>
 * The likelihood of a theory is measured by the product of the probability of each example. If the example is
 * negative, the complement of the probability of this example is used.
 * <p>
 * Created on 08/05/17.
 *
 * @author Victor Guimarães
 */
public class LikelihoodMetric extends AccumulatorMetric {

    /**
     * A minimal value to be multiplied into the result. This prevents the probability to goes to 0 if a example
     * positive examples is not proved or a negative one gets probability of 1.
     */
    public static final double EPSILON = 1e-4;

    /**
     * Constructor with the needed parameters.
     *
     * @param engineSystemTranslator the {@link EngineSystemTranslator}
     */
    public LikelihoodMetric(EngineSystemTranslator engineSystemTranslator) {
        super(engineSystemTranslator);
    }

    @Override
    protected double initialAccumulatorValue() {
        return 1;
    }

    @Override
    protected double accumulate(double initial, double append) {
        return initial * append;
    }

    @Override
    protected double calculateAppend(AtomExample atomExample, double value) {
        if (atomExample.isPositive()) {
            return Math.max(value, EPSILON);
        } else {
            return Math.max(1 - value, EPSILON);
        }
    }

}
