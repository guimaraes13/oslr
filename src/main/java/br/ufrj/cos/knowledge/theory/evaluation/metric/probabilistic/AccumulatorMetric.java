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
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.logic.Atom;

import java.util.Map;

/**
 * A template for metrics that simply accumulates a value for each proved ground example.
 * <p>
 * Created on 08/05/17.
 *
 * @author Victor Guimarães
 */
public abstract class AccumulatorMetric extends TheoryMetric {

    /**
     * Constructor with the needed parameters.
     *
     * @param engineSystemTranslator the {@link EngineSystemTranslator}
     */
    public AccumulatorMetric(EngineSystemTranslator engineSystemTranslator) {
        super(engineSystemTranslator);
    }

    @Override
    protected double evaluate(Map<Example, Map<Atom, Double>> inferredResult, Examples examples) {
        Map<Atom, Double> atomValues;
        double result = initialAccumulatorValue();
        for (Example example : examples) {
            atomValues = inferredResult.get(example);
            if (atomValues == null) { continue; }
            result = accumulate(result, evaluateExamples(example.getGroundedQuery(), atomValues));
        }
        return result;
    }

    /**
     * Initial value for the accumulator. This value must be the neutral element of the
     * {@link #accumulate(double, double)} function.
     *
     * @return the initial value for the accumulator.
     */
    protected abstract double initialAccumulatorValue();

    /**
     * Calculates the new value of the accumulator, based on the current accumulated value and the new append.
     *
     * @param initial the current accumulated value
     * @param append  the append
     * @return the new value of the accumulator
     */
    protected abstract double accumulate(double initial, double append);

    /**
     * Evaluates the grounds of the {@link Example}.
     *
     * @param groundedQuery the grounds
     * @param atomValues    the values of the grounds
     * @return the product of the values of the grounds
     */
    protected double evaluateExamples(Iterable<? extends AtomExample> groundedQuery, Map<Atom, Double> atomValues) {
        double result = initialAccumulatorValue();
        Double value;
        for (AtomExample atomExample : groundedQuery) {
            value = atomValues.get(atomExample.getAtom());
            if (value != null) {
                result = accumulate(result, calculateAppend(atomExample, value));
            }
        }
        return result;
    }

    /**
     * Calculates the append that should be accumulated to the accumulator, given the {@link AtomExample} and its value.
     *
     * @param atomExample the {@link AtomExample}
     * @param value       the value
     * @return the append that should be accumulated
     */
    protected abstract double calculateAppend(AtomExample atomExample, double value);

}
