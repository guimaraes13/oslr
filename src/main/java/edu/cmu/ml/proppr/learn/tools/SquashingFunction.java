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

package edu.cmu.ml.proppr.learn.tools;

import edu.cmu.ml.proppr.graph.LearningGraph;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.math.ParamVector;

import java.util.Map;

public abstract class SquashingFunction<F> {

    public abstract double computeDerivative(double weight);

    /**
     * Support method for proving
     *
     * @param params
     * @param features
     * @return
     */
    public double edgeWeight(Map<F, Double> params, Map<F, Double> features) {
        double ret = 0.0;
        for (Map.Entry<F, Double> f : features.entrySet()) {
            ret += Dictionary.safeGet(params, f.getKey(), this.defaultValue()) * f.getValue(); // this is wrong. f
            // .getValue() is only used if f.key is not in the parameter map. wtf
        }
        ret = compute(ret);
        if (Double.isInfinite(ret)) { return Double.MAX_VALUE; }
        return Math.max(0, ret);
    }

    public abstract double defaultValue();
//	public abstract double fixedValue();

    /**
     * Wrapper functions must deliver a value >= 0
     */
    public abstract double compute(double x);

    /**
     * Support method for learning
     *
     * @param sum
     * @return
     */
    public double edgeWeight(double sum) {
        return Math.max(0, compute(sum));
    }

    /**
     * Support method for learning
     *
     * @param params
     * @param features
     * @return
     */
    public double edgeWeight(LearningGraph g, int eid,
                             ParamVector<String, ?> params) {
        double ret = 0.0;
        // iterate over the features on the edge
        for (int fid = g.edge_labels_lo[eid]; fid < g.edge_labels_hi[eid]; fid++) {
            ret += Dictionary.safeGet(params,
                                      g.featureLibrary.getSymbol(g.label_feature_id[fid]),
                                      this.defaultValue()) * g.label_feature_weight[fid];
        }
        ret = compute(ret);
        if (Double.isInfinite(ret)) { return Double.MAX_VALUE; }
        return Math.max(0, ret);
    }
}
