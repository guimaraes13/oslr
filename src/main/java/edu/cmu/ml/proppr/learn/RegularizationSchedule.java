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

import edu.cmu.ml.proppr.examples.PosNegRWExample;
import edu.cmu.ml.proppr.examples.RWExample;
import edu.cmu.ml.proppr.graph.LearningGraph;
import edu.cmu.ml.proppr.util.math.ParamVector;
import gnu.trove.map.TIntDoubleMap;

import java.util.Set;

public class RegularizationSchedule {

    protected Regularize reg;
    protected SRW parent;

    public RegularizationSchedule(SRW srw, Regularize r) {
        this.parent = srw;
        this.reg = r;
    }

    public void regularization(ParamVector<String, ?> params, RWExample ex, TIntDoubleMap gradient) {
        for (String f : localFeatures(params, ex.getGraph())) {
            if (!parent.trainable(f)) { continue; }
            reg.synchronousUpdate(parent.c, params, f, gradient, parent._cumulativeLoss(), ex.getGraph()
                    .featureLibrary);
        }
    }

    public Set<String> localFeatures(ParamVector<String, ?> paramVec, LearningGraph graph) {
        return paramVec.keySet();
    }

    public ParamVector<String, ?> setupParams(ParamVector<String, ?> params) {
        return params;
    }

    public void prepareForExample(ParamVector<String, ?> params, LearningGraph graph, ParamVector<String, ?> apply) {
    }

    public void prepareForSgd(ParamVector<String, ?> params, PosNegRWExample ex) {
    }

    public void cleanupParams(ParamVector<String, ?> params, ParamVector<String, ?> apply) {
    }

    public RegularizationSchedule copy(SRW srw) {
        return new RegularizationSchedule(srw, this.reg);
    }

    public String description() {
        return this.getClass().getCanonicalName() + ", " + this.reg.getClass().getCanonicalName();
    }
}
