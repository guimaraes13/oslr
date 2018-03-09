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

package edu.cmu.ml.proppr.learn;

import edu.cmu.ml.proppr.examples.PosNegRWExample;
import edu.cmu.ml.proppr.graph.LearningGraph;
import edu.cmu.ml.proppr.util.math.MuParamVector;
import edu.cmu.ml.proppr.util.math.ParamVector;

import java.util.Set;

/**
 * Local AKA Lazy regularization.
 * <p>
 * This regularization schedule must be used with a MuParamVector.
 *
 * @author krivard
 */
public class LocalRegularizationSchedule extends RegularizationSchedule {

    public LocalRegularizationSchedule(SRW srw, Regularize r) {
        super(srw, r);
    }

    @Override
    public ParamVector<String, ?> setupParams(ParamVector<String, ?> params) {
        return new MuParamVector<String>(params);
    }

    @Override
    public Set<String> localFeatures(ParamVector<String, ?> paramVec, LearningGraph graph) {
        return graph.getFeatureSet();
    }

    @Override
    public void prepareForExample(ParamVector<String, ?> params, LearningGraph graph, ParamVector<String, ?> apply) {
        if (!(params instanceof MuParamVector)) {
            throw new IllegalArgumentException("LocalRegularizationSchedule requires a MuParamVector");
        }
        for (String f : localFeatures(params, graph)) {
            if (!parent.trainable(f)) { continue; }
            this.reg.lazyUpdate(parent.c, (MuParamVector<String>) params, apply, f, parent._cumulativeLoss(), parent
                    .learningRate(f));
        }
    }

    @Override
    public void prepareForSgd(ParamVector<String, ?> params, PosNegRWExample ex) {
        if (!(params instanceof MuParamVector)) {
            throw new IllegalArgumentException("LocalRegularizationSchedule requires a MuParamVector");
        }
        ((MuParamVector) params).count();
        ((MuParamVector) params).setLast(localFeatures(params, ex.getGraph()));
    }

    @Override
    public void cleanupParams(ParamVector<String, ?> params, ParamVector<String, ?> apply) {
        if (!(params instanceof MuParamVector)) {
            throw new IllegalArgumentException("LocalRegularizationSchedule requires a MuParamVector");
        }
        for (String f : params.keySet()) {
            // finish catching up the regularization:
            // Bj = Bj - lambda * (Rj)
            this.reg.lazyUpdate(parent.c, (MuParamVector<String>) params, apply, f, parent.cumulativeLoss(), parent.learningRate(f));
        }
        ((MuParamVector) params).setLast(params.keySet());
    }

    @Override
    public RegularizationSchedule copy(SRW srw) {
        return new LocalRegularizationSchedule(srw, this.reg);
    }
}
