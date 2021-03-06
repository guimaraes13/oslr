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

package edu.cmu.ml.proppr.prove;

import edu.cmu.ml.proppr.learn.tools.Linear;
import edu.cmu.ml.proppr.learn.tools.SquashingFunction;
import edu.cmu.ml.proppr.prove.wam.Feature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * featureDictWeighter that weights each feature with a default
 * value of 1.0, but allows a one to plug in a dictionary of
 * non-default weights.
 *
 * @author krivard
 */
public class InnerProductWeighter extends FeatureDictWeighter {

    private static final int MAX_UNKNOWN_FEATURE_WARNINGS = 10;
    private static final Logger log = LogManager.getLogger(InnerProductWeighter.class);

    public InnerProductWeighter() {
        this(new HashMap<Feature, Double>());
    }

    public InnerProductWeighter(Map<Feature, Double> weights) {
        this(DEFAULT_SQUASHING_FUNCTION(), weights);
    }

    public InnerProductWeighter(SquashingFunction f, Map<Feature, Double> w) {
        super(f, w);
    }

    private static SquashingFunction DEFAULT_SQUASHING_FUNCTION() {
        return new Linear();
    }

    public static FeatureDictWeighter fromParamVec(Map<String, Double> paramVec) {
        return fromParamVec(paramVec, DEFAULT_SQUASHING_FUNCTION());
    }

    public static InnerProductWeighter fromParamVec(Map<String, Double> paramVec, SquashingFunction f) {
        Map<Feature, Double> weights = new HashMap<Feature, Double>();
        for (Map.Entry<String, Double> s : paramVec.entrySet()) {
            weights.put(new Feature(s.getKey()), s.getValue());
        }
        return new InnerProductWeighter(f, weights);
    }

    @Override
    public double w(Map<Feature, Double> featureDict) {
        // track usage of known & unknown features
        for (Feature g : featureDict.keySet()) {
            countFeature(g);
        }
        return this.squashingFunction.edgeWeight(this.weights, featureDict);
    }

    public Map<Feature, Double> getWeights() {
        return this.weights;
    }
}
