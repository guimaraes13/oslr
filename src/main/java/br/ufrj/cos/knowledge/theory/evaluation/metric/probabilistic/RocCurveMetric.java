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
 * Copyright (C) 2017-2019 Victor
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
 * Calculates the are under de ROC Curve.
 * <p>
 * Created on 20/05/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("SameReturnValue")
public class RocCurveMetric extends CurveMetric {

    @Override
    protected Pair<Double, Double> buildPoint(int truePositive, int falsePositive, int positives,
                                              int negatives) {
        double truePositiveRate = positives > 0 ? ((double) truePositive) / (positives) : 1.0;
        double falsePositiveRate = negatives > 0 ? ((double) falsePositive) / (negatives) : 0.0;
        return new ImmutablePair<>(falsePositiveRate, truePositiveRate);
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
