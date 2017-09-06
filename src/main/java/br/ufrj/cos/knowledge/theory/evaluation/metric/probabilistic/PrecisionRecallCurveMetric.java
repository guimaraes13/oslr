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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Calculates the are under de Precision-Recall Curve.
 * <p>
 * Created on 05/09/17.
 *
 * @author Victor Guimarães
 */
public class PrecisionRecallCurveMetric extends CurveMetric {

    @Override
    protected Pair<Double, Double> buildPoint(int truePositive, int falsePositive, int positives, int negatives) {
        double precision =
                truePositive + falsePositive > 0 ? ((double) truePositive) / (truePositive + falsePositive) : 1.0;
        double recall = positives > 0 ? ((double) truePositive) / (positives) : 1.0;
        return new ImmutablePair<>(recall, precision);
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
        return "PR Curve\t";
    }

}
