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
import br.ufrj.cos.logic.Atom;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Calculates the are under de ROC Curve.
 * <p>
 * Created on 20/05/17.
 *
 * @author Victor Guimarães
 */
public class RocCurveMetric extends AccumulatorMetric<List<Pair<AtomExample, Double>>, Pair<AtomExample, Double>> {

    /**
     * Returns the full curve. This is the default value where the list has no negative examples, possibly empty.
     */
    protected static final List<Pair<Double, Double>> FULL_ROC_CURVE;
    /**
     * Returns the empty curve. This is the default value where the list has no positive examples, and is not empty.
     */
    protected static final List<Pair<Double, Double>> EMPTY_CURVE;

    static {
        FULL_ROC_CURVE = new ArrayList<>(2);
        FULL_ROC_CURVE.add(new ImmutablePair<>(0.0, 1.0));
        FULL_ROC_CURVE.add(new ImmutablePair<>(1.0, 1.0));
    }

    static {
        EMPTY_CURVE = new ArrayList<>(2);
        EMPTY_CURVE.add(new ImmutablePair<>(0.0, 0.0));
        EMPTY_CURVE.add(new ImmutablePair<>(0.0, 1.0));
    }

    @Override
    public List<Pair<AtomExample, Double>> calculateEvaluation(Map<Example, Map<Atom, Double>> inferredResult,
                                                               Collection<? extends Example> examples) {
        return super.calculateEvaluation(inferredResult, examples);
    }

    @Override
    protected double calculateResult(List<Pair<AtomExample, Double>> result) {
        if (result.isEmpty()) { return getDefaultValue(); }
        List<Pair<Double, Double>> rocPoints = buildRocCurve(result);
        return integrateRocCurve(rocPoints);
    }

    /**
     * Builds the ROC curve represented by a list of 2D points.
     *
     * @param pairs the pairs of examples and their values
     * @return the list of points representing the ROC curve
     */
    public static List<Pair<Double, Double>> buildRocCurve(List<Pair<AtomExample, Double>> pairs) {
        if (pairs.isEmpty()) { return FULL_ROC_CURVE; }

        int positives = (int) pairs.stream().filter(pair -> pair.getLeft().isPositive()).count();
        int negatives = pairs.size() - positives;

        if (negatives == 0) { return FULL_ROC_CURVE; }
        if (positives == 0) { return EMPTY_CURVE; }

        sortExamples(pairs);

        return getCurvePoints(pairs, positives, negatives);
    }

    /**
     * Sorts the examples from the highest classified to the lowest.
     *
     * @param pairs the pairs of examples and their values
     */
    protected static void sortExamples(List<Pair<AtomExample, Double>> pairs) {
        pairs.sort((o1, o2) -> -Double.compare(o1.getRight(), o2.getRight()));
    }

    /**
     * Calculates the curve points of the ROC curve, based on the sorted pairs of examples.
     * <p>
     * The examples must be sorted from the highest classified to the lowest.
     *
     * @param pairs     the pair of sorted examples
     * @param positives the number of positive examples
     * @param negatives the number of negative examples
     * @return the points of the ROC curve
     * @see #sortExamples(List)
     */
    protected static List<Pair<Double, Double>> getCurvePoints(List<Pair<AtomExample, Double>> pairs, int positives,
                                                               int negatives) {
        List<Pair<Double, Double>> points = new ArrayList<>();

        double current = pairs.get(0).getRight() + 1.0;
        double auxiliary;

        int truePositive = 0;
        int falsePositive = 0;
        points.add(buildROCPoint(truePositive, falsePositive, positives, negatives));
        for (Pair<AtomExample, Double> pair : pairs) {
            auxiliary = pair.getRight();
            if (pair.getLeft().isPositive()) {
                truePositive++;
            } else {
                falsePositive++;
            }
            if (auxiliary >= current) {
                continue;
            }
            points.add(buildROCPoint(truePositive, falsePositive, positives, negatives));
            current = auxiliary;
        }
        points.add(buildROCPoint(truePositive, falsePositive, positives, negatives));

        return points;
    }

    /**
     * Integrate the points to obtain the area under the ROC curve.
     * <p>
     * It uses the trapezoid method.
     *
     * @param points the points of the curve
     * @return the area under the ROC curve
     */
    public static double integrateRocCurve(List<Pair<Double, Double>> points) {
        double sum = 0;
        Pair<Double, Double> previous = points.get(0);
        Pair<Double, Double> current;
        for (int i = 1; i < points.size(); i++) {
            current = points.get(i);
            // accumulating the area of the trapezoid
            sum += (current.getLeft() - previous.getLeft()) * ((current.getRight() + previous.getRight()) / 2);
            previous = current;
        }
        return sum;
    }

    /**
     * Builds the point of the ROC curve based on true positive, false positive and total number of examples.
     *
     * @param truePositive  the number of true positive examples
     * @param falsePositive the number of false positive examples
     * @param positives     the total number of positives
     * @param negatives     the total number of negatives
     * @return the point of the ROC curve
     */
    protected static Pair<Double, Double> buildROCPoint(int truePositive, int falsePositive, int positives,
                                                        int negatives) {
        double truePositiveRate = positives > 0 ? ((double) truePositive) / (positives) : 1.0;
        double falsePositiveRate = negatives > 0 ? ((double) falsePositive) / (negatives) : 0.0;
        return new ImmutablePair<>(falsePositiveRate, truePositiveRate);
    }

    /**
     * Initial value for the accumulator. This value must be the neutral element of the
     * {@link #accumulate(List, List)} function.
     *
     * @return the initial value for the accumulator.
     */
    @Override
    protected List<Pair<AtomExample, Double>> initialAccumulatorValue() {
        return new ArrayList<>();
    }

    /**
     * Calculates the new value of the accumulator, based on the current accumulated value and the new append.
     *
     * @param initial the current accumulated value
     * @param append  the append
     * @return the new value of the accumulator
     */
    @Override
    protected List<Pair<AtomExample, Double>> accumulate(List<Pair<AtomExample, Double>> initial,
                                                         List<Pair<AtomExample, Double>> append) {
        initial.addAll(append);
        return initial;
    }

    /**
     * Calculates the new value of the accumulator, based on the current accumulated value and the new append.
     *
     * @param initial the current accumulated value
     * @param append  the append
     * @return the new value of the accumulator
     */
    @Override
    protected List<Pair<AtomExample, Double>> accumulateAppend(List<Pair<AtomExample, Double>> initial,
                                                               Pair<AtomExample, Double> append) {
        initial.add(append);
        return initial;
    }

    /**
     * Calculates the append that should be accumulated to the accumulator, given the {@link AtomExample} and its value.
     *
     * @param atomExample the {@link AtomExample}
     * @param value       the value
     * @return the append that should be accumulated
     */
    @Override
    protected Pair<AtomExample, Double> calculateAppend(AtomExample atomExample, double value) {
        return new ImmutablePair<>(atomExample, value);
    }

    @Override
    public double getRange() {
        return 1.0;
    }

    @Override
    public double getMaximumValue() {
        return 1.0;
    }

    @Override
    public String toString() {
        return "ROC Curve\t";
    }

}
