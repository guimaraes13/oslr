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

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cmu.ml.proppr.examples;

import edu.cmu.ml.proppr.prove.wam.Query;
import edu.cmu.ml.proppr.util.Dictionary;

import java.util.Arrays;

public class InferenceExample {

    private final Query[] negSet;
    private final Query[] posSet;
    private final Query query;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.query.toString());
        Dictionary.buildString(negSet, new StringBuilder(), " -", false);
        Dictionary.buildString(posSet, new StringBuilder(), " +", false);
        return sb.toString();
    }
}
