/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2018 Victor Guimarães
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

package edu.cmu.ml.proppr.examples;

import edu.cmu.ml.proppr.prove.wam.Query;
import edu.cmu.ml.proppr.util.Dictionary;

import java.util.Arrays;

public class InferenceExample {

    private Query[] negSet;
    private Query[] posSet;
    private Query query;

    public InferenceExample(Query q, Query[] posSet, Query[] negSet) {
        this.query = q;

        if (posSet != null) {
            this.posSet = posSet;
            Arrays.sort(this.posSet);
        } else {
            this.posSet = new Query[0];
        }
        if (negSet != null) {
            this.negSet = negSet;
            Arrays.sort(this.negSet);
        } else {
            this.negSet = new Query[0];
        }
    }

    public Query[] getNegSet() {
        return negSet;
    }

    public Query[] getPosSet() {
        return posSet;
    }

    public Query getQuery() {
        return this.query;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.query.toString());
        Dictionary.buildString(negSet, new StringBuilder(), " -", false);
        Dictionary.buildString(posSet, new StringBuilder(), " +", false);
        return sb.toString();
    }
}
