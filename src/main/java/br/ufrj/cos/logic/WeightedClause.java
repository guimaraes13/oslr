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

package br.ufrj.cos.logic;

import br.ufrj.cos.util.LanguageUtils;

/**
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class WeightedClause extends HornClause {

    protected double weight = 1.0;

    /**
     * Constructs with head {@link Atom} and default {@link #weight}.
     *
     * @param head the head
     */
    public WeightedClause(Atom head) {
        super(head);
    }

    /**
     * Constructs with head {@link Atom}, body and default {@link #weight}.
     *
     * @param head the head
     * @param body the body
     */
    public WeightedClause(Atom head, Conjunction body) {
        super(head, body);
    }

    /**
     * Constructs with head {@link Atom} and weight
     *
     * @param head   the head
     * @param weight the weight
     */
    public WeightedClause(double weight, Atom head) {
        super(head);
        this.weight = weight;
    }

    /**
     * Constructs with head {@link Atom}, body and weight.
     *
     * @param weight the weight
     * @param head   the head
     * @param body   the body
     */
    public WeightedClause(double weight, Atom head, Conjunction body) {
        super(head, body);
        this.weight = weight;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WeightedClause)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        WeightedClause that = (WeightedClause) o;

        return Double.compare(that.weight, weight) == 0;
    }

    @Override
    public String toString() {
        return weight + " " + LanguageUtils.WEIGHT_SIGN + " " + super.toString();
    }

}
