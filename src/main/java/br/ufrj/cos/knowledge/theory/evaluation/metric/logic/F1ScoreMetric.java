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

package br.ufrj.cos.knowledge.theory.evaluation.metric.logic;

/**
 * Measure the F1 Score of the system given the examples. The F1 Score does only considers the probability of the proved
 * examples, only if it was proved or not.
 * <p>
 * The F1 Score is the harmonic mean of precision and recall.
 * <p>
 * Created on 08/05/17.
 *
 * @author Victor Guimarães
 */
public class F1ScoreMetric extends ConfusionMatrixBasedMetric {

    @Override
    protected double calculateConfusionMatrixMetric() {
        return (double) (2 * truePositive) / (2 * truePositive + falsePositive + falseNegative);
    }

    @Override
    public String toString() {
        return "F1 Score";
    }

}
