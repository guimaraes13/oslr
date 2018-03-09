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

package edu.cmu.ml.proppr.learn;

import java.util.Set;

import edu.cmu.ml.proppr.examples.PosNegRWExample;
import edu.cmu.ml.proppr.graph.LearningGraph;
import edu.cmu.ml.proppr.util.math.MuParamVector;
import edu.cmu.ml.proppr.util.math.ParamVector;

/**
 * Local AKA Lazy regularization.
 * 
 * This regularization schedule must be used with a MuParamVector.
 * 
 * @author krivard
 *
 */
public class LocalRegularizationSchedule extends RegularizationSchedule {
	public LocalRegularizationSchedule(SRW srw, Regularize r) {
		super(srw, r);
	}

	public ParamVector<String,?> setupParams(ParamVector<String,?> params) { return new MuParamVector<String>(params); }
	public Set<String> localFeatures(ParamVector<String,?> paramVec, LearningGraph graph) { return graph.getFeatureSet(); }
	public void prepareForExample(ParamVector<String,?> params, LearningGraph graph, ParamVector<String,?> apply) {
		if (!(params instanceof MuParamVector)) throw new IllegalArgumentException("LocalRegularizationSchedule requires a MuParamVector");
		for (String f : localFeatures(params, graph)) {
			if (!parent.trainable(f)) continue;
			this.reg.lazyUpdate(parent.c, (MuParamVector<String>) params, apply, f, parent._cumulativeLoss(), parent.learningRate(f));
		}
	}
	public void prepareForSgd(ParamVector<String,?> params, PosNegRWExample ex) {
		if (!(params instanceof MuParamVector)) throw new IllegalArgumentException("LocalRegularizationSchedule requires a MuParamVector");
		((MuParamVector)params).count();
		((MuParamVector)params).setLast(localFeatures(params,ex.getGraph()));
	}
	public void cleanupParams(ParamVector<String,?> params, ParamVector<String,?> apply) {
		if (!(params instanceof MuParamVector)) throw new IllegalArgumentException("LocalRegularizationSchedule requires a MuParamVector");
		for(String f : (Set<String>) params.keySet()) {
			// finish catching up the regularization:
			// Bj = Bj - lambda * (Rj)
			this.reg.lazyUpdate(parent.c, (MuParamVector<String>) params, apply, f, parent.cumulativeLoss(), parent.learningRate(f));
		}
		((MuParamVector)params).setLast(params.keySet());
	}
	@Override
	public RegularizationSchedule copy(SRW srw) {
		return new LocalRegularizationSchedule(srw,this.reg);
	}
}
