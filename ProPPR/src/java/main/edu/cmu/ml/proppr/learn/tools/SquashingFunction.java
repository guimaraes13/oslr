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

package edu.cmu.ml.proppr.learn.tools;

import java.util.Map;

import edu.cmu.ml.proppr.graph.LearningGraph;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.math.ParamVector;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.TObjectDoubleMap;

public abstract class SquashingFunction<F> {
	/** Wrapper functions must deliver a value >= 0 */
	public abstract double compute(double x);
	public abstract double computeDerivative(double weight);
	public abstract double defaultValue();
//	public abstract double fixedValue();
	
	/** Support method for proving
	 * 
	 * @param params
	 * @param features
	 * @return
	 */
	public double edgeWeight(Map<F,Double> params, Map<F,Double> features) {
		double ret = 0.0;
		for (Map.Entry<F,Double> f : features.entrySet()) {
			ret += Dictionary.safeGet(params, f.getKey(), this.defaultValue()) * f.getValue(); // this is wrong. f.getValue() is only used if f.key is not in the parameter map. wtf
		}
		ret = compute(ret);
		if (Double.isInfinite(ret)) return Double.MAX_VALUE;
		return Math.max(0, ret);
	}
	
	/** Support method for learning
	 * 
	 * @param sum
	 * @return
	 */
	public double edgeWeight(double sum) {
		return Math.max(0,compute(sum));
	}
	
	/** Support method for learning
	 * 
	 * @param params
	 * @param features
	 * @return
	 */
	public double edgeWeight(LearningGraph g, int eid,
			ParamVector<String, ?> params) {
		double ret = 0.0;
		// iterate over the features on the edge
		for(int fid = g.edge_labels_lo[eid]; fid<g.edge_labels_hi[eid]; fid++) {
			ret += Dictionary.safeGet(params, 
					g.featureLibrary.getSymbol(g.label_feature_id[fid]), 
					this.defaultValue()) * g.label_feature_weight[fid];
		}
		ret = compute(ret);
		if (Double.isInfinite(ret)) return Double.MAX_VALUE;
		return Math.max(0, ret);
	}
}
