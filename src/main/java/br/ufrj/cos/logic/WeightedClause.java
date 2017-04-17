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

package br.ufrj.cos.logic;

import br.ufrj.cos.util.LanguageUtils;

/**
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class WeightedClause extends HornClause {

    protected double weight = 1.0;

    public WeightedClause(Atom head) {
        super(head);
    }

    public WeightedClause(Atom head, Conjunction body) {
        super(head, body);
    }

    public WeightedClause(double weight, Atom head) {
        super(head);
        this.weight = weight;
    }

    public WeightedClause(double weight, Atom head, Conjunction body) {
        super(head, body);
        this.weight = weight;
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
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return weight + " " + LanguageUtils.WEIGHT_SIGN + " " + super.toString();
    }

}
