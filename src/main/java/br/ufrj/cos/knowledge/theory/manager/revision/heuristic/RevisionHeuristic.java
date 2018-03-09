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

package br.ufrj.cos.knowledge.theory.manager.revision.heuristic;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.manager.Node;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.logic.HornClause;
import org.apache.commons.lang3.tuple.Pair;

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
public abstract class RevisionHeuristic implements Comparator<Pair<Collection<? extends Example>, Node<HornClause>>> {

    /**
     * The default value of the metric, it should be proper overridden by subclasses.
     */
    @SuppressWarnings("CanBeFinal")
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
     * Preferably, the implementation must have the same properties as {@link #compare(Pair, Pair)}.
     *
     * @param candidate the candidate value
     * @param current   the current value
     * @return the quantitative difference between candidate and current
     */
    @SuppressWarnings("MethodMayBeStatic")
    public double difference(Double candidate, Double current) {
        return Math.abs(candidate - current) * Double.compare(candidate, current);
    }

    /**
     * By default, as higher the metric better the theory. Override this method, otherwise.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public int compare(Pair<Collection<? extends Example>, Node<HornClause>> o1,
                       Pair<Collection<? extends Example>, Node<HornClause>> o2) {
        return -Double.compare(evaluate(o1.getKey(), o1.getValue()), evaluate(o2.getKey(), o2.getValue()));
    }

    /**
     * Evaluates the example to revise.
     *
     * @param examples     the {@link Examples}
     * @param revisionNode the revision node
     * @return the evaluated metric
     */
    public abstract double evaluate(Collection<? extends Example> examples, Node<HornClause> revisionNode);

}
