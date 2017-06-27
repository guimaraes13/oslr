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

package br.ufrj.cos.knowledge.theory.manager.revision.heuristic;

import br.ufrj.cos.knowledge.Knowledge;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.theory.Theory;

import java.util.Collection;
import java.util.Comparator;

/**
 * Class to calculate a heuristic value of how good a collection of examples is for revise. The heuristic should be
 * as simple as possible and should not rely on inference.
 * <p>
 * Created on 26/06/17.
 *
 * @author Victor Guimarães
 */
public abstract class RevisionHeuristic implements Comparator<Collection<? extends Example>> {

    /**
     * The default value of the metric, it should be proper overridden by subclasses.
     */
    protected double defaultValue = 0;

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
        return Math.abs(candidate - current) * Double.compare(candidate, current);
    }

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
    public int compare(Collection<? extends Example> o1, Collection<? extends Example> o2) {
        return Double.compare(evaluate(o1), evaluate(o2));
    }

    /**
     * Evaluates the example based on the inferred results.
     *
     * @param examples the {@link Examples}
     * @return the evaluated metric
     */
    public abstract double evaluate(Collection<? extends Example> examples);

}
