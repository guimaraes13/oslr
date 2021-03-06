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

package br.ufrj.cos.knowledge.theory.evaluation.metric;

import br.ufrj.cos.engine.EngineSystemTranslator;
import br.ufrj.cos.knowledge.Knowledge;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.util.Initializable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

/**
 * Responsible for evaluating the {@link Theory} against some metric.
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("CanBeFinal")
public abstract class TheoryMetric implements Comparator<Double>, Initializable {

    /**
     * Flag to specify if the {@link EngineSystemTranslator} should retrain the parameters for each intermediary rule
     * candidate.
     * <p>
     * If it is {@code true}, each candidate rule will have its parameters fine tuned before evaluation,
     * this will reveal the real contribution of a rule, but can be too computational expensive.
     * <p>
     * If it is {@code false}, candidate rule will be evaluated with the default parameters defined be the
     * {@link EngineSystemTranslator}. This is computational cheaper, but might make a poorer candidate rule be better
     * evaluated than a better one.
     * <p>
     * Also, notice that some {@link TheoryMetric}s may be insensitive to parameters.
     */
    public boolean parametersRetrainedBeforeEvaluate = false;
    /**
     * The default value of the metric, it should be proper overridden by subclasses.
     */
    protected double defaultValue = 0;

    /**
     * Evaluates the example based on the inferred results.
     *
     * @param inferredResult the results from the {@link EngineSystemTranslator}
     * @param examples       the {@link Examples}
     * @return the evaluated metric
     */
    public abstract double evaluate(Map<Example, Map<Atom, Double>> inferredResult,
                                    Collection<? extends Example> examples);

    /**
     * Gets the default value of a metric, this value must by the worst possible value of the metric. This value
     * should be used when one fails to evaluateTheory the {@link Theory} with this metric (e.g. evaluation takes longer
     * than a specified timeout).
     *
     * @return the default value of the metric
     */
    public double getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets the range of the metric. The range of a metric is the absolute difference between the smallest and
     * biggest value the metric can assume.
     *
     * @return the range of the metric.
     */
    public abstract double getRange();

    /**
     * Calculates the best possible improvement over the currentEvaluation. The best possible improvement is the
     * comparison between the {@link #getMaximumValue()} against the current evaluation.
     *
     * @param currentEvaluation the current evaluation
     * @return the best possible improvement
     */
    public double bestPossibleImprovement(Double currentEvaluation) {
        return difference(getMaximumValue(), currentEvaluation);
    }

    /**
     * Calculates a quantitative difference between candidate and current.
     * <p>
     * If the candidate is better than the current, it should returns a positive number represents how much better it
     * is.
     * <p>
     * If the candidate is worst than the current, it should returns a negative number represents how much worst it is.
     * <p>
     * If they are equal, it should return 0.
     * <p>
     * Preferably, the implementation must have the same properties as {@link #compare(Double, Double)}
     *
     * @param candidate the candidate value
     * @param current   the current value
     * @return the quantitative difference between candidate and current
     */
    public double difference(Double candidate, Double current) {
        return Math.abs(candidate - current) * Math.signum(compare(candidate, current));
    }

    /**
     * Gets the maximum value of the metric. The maximum value of the metric is the best value the metric can take.
     *
     * @return the maximum value of the metric.
     */
    public abstract double getMaximumValue();

    /**
     * Note: this comparator imposes orderings that are inconsistent with equals; in the means that two different
     * {@link Theory}is might have the same evaluation for the same {@link Knowledge}. In addiction, two equal
     * {@link Theory}is must have the same evaluation for the same {@link Knowledge}.
     * <p>
     * By default, as higher the metric better the theory. Override this method, otherwise.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public int compare(Double o1, Double o2) {
        return Double.compare(o1, o2);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (parametersRetrainedBeforeEvaluate ? 1 : 0);
        temp = Double.doubleToLongBits(defaultValue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + this.getClass().getName().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!this.getClass().equals(o.getClass())) { return false; }

        TheoryMetric metric = (TheoryMetric) o;
        if (!this.getClass().getName().equals(o.getClass().getName())) { return false; }
        if (parametersRetrainedBeforeEvaluate != metric.parametersRetrainedBeforeEvaluate) { return false; }
        return Double.compare(metric.defaultValue, defaultValue) == 0;
    }

    @Override
    public abstract String toString();

}
