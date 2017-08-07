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

import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.logic.Atom;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A template for metrics that simply accumulates a value for each proved ground example.
 * <p>
 * {@link J} is the type of the initial accumulated object;
 * <br>
 * {@link K} is the type of a possible appended object.
 * <p>
 * Created on 08/05/17.
 *
 * @author Victor Guimarães
 */
public abstract class AccumulatorMetric<J, K> extends TheoryMetric {

    /**
     * The default value for unanswered examples.
     */
    public static final double NOT_INFERRED_EXAMPLE_VALUE = 0.0;

    @Override
    public double evaluate(Map<Example, Map<Atom, Double>> inferredResult, Collection<? extends Example> examples) {
        J evaluation = calculateEvaluation(inferredResult, examples);
        return evaluation != null ? calculateResult(evaluation) : getDefaultValue();
    }

    /**
     * Calculates the internal evaluation of the inferred results over the examples.
     *
     * @param inferredResult the inferred results
     * @param examples       the examples
     * @return the result
     */
    protected J calculateEvaluation(Map<Example, Map<Atom, Double>> inferredResult,
                                    Collection<? extends Example> examples) {
        Map<Atom, Double> atomValues;
        J result = initialAccumulatorValue();

        if (inferredResult.isEmpty()) { return null; }
        Set<Example> provedExamples;
        provedExamples = examples.stream().filter(e -> inferredResult.keySet().contains(e)).collect(Collectors.toSet());
        boolean proved = false;
        for (Example example : provedExamples) {
            atomValues = inferredResult.get(example);
            if (atomValues == null) { continue; }
            result = accumulate(result, evaluateExamples(example.getGroundedQuery(), atomValues));
            proved |= true;
        }

        return proved ? result : null;
    }

    /**
     * Converts the result from {@link J} to {@code double}.
     *
     * @param result {@link J} result
     * @return the {@code double} result
     */
    protected abstract double calculateResult(J result);

    /**
     * Initial value for the accumulator. This value must be the neutral element of the
     * {@link #accumulate(Object, Object)} function.
     *
     * @return the initial value for the accumulator.
     */
    protected abstract J initialAccumulatorValue();

    /**
     * Calculates the new value of the accumulator, based on the current accumulated value and the new append.
     *
     * @param initial the current accumulated value
     * @param append  the append
     * @return the new value of the accumulator
     */
    protected abstract J accumulate(J initial, J append);

    /**
     * Evaluates the grounds of the {@link Example}.
     *
     * @param groundedQuery the grounds
     * @param atomValues    the values of the grounds
     * @return the product of the values of the grounds
     */
    protected J evaluateExamples(Iterable<? extends AtomExample> groundedQuery, Map<Atom, Double> atomValues) {
        J result = initialAccumulatorValue();
        Double value;
        for (AtomExample atomExample : groundedQuery) {
            value = atomValues.getOrDefault(atomExample.getAtom(), NOT_INFERRED_EXAMPLE_VALUE);
            result = accumulateAppend(result, calculateAppend(atomExample, value));
        }
        return result;
    }

    /**
     * Calculates the new value of the accumulator, based on the current accumulated value and the new append.
     *
     * @param initial the current accumulated value
     * @param append  the append
     * @return the new value of the accumulator
     */
    protected abstract J accumulateAppend(J initial, K append);

    /**
     * Calculates the append that should be accumulated to the accumulator, given the {@link AtomExample} and its value.
     *
     * @param atomExample the {@link AtomExample}
     * @param value       the value
     * @return the append that should be accumulated
     */
    protected abstract K calculateAppend(AtomExample atomExample, double value);

}
