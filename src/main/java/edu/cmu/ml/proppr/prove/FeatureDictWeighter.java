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

import com.skjegstad.utils.BloomFilter;
import edu.cmu.ml.proppr.learn.tools.SquashingFunction;
import edu.cmu.ml.proppr.prove.wam.Feature;
import edu.cmu.ml.proppr.util.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public abstract class FeatureDictWeighter {

    private static final Logger log = LogManager.getLogger(FeatureDictWeighter.class);
    protected Map<Feature, Double> weights;
    protected SquashingFunction squashingFunction;
    protected boolean countFeatures;
    protected int numUnknownFeatures = 0;
    protected int numKnownFeatures = 0;
    protected BloomFilter<Feature> unknownFeatures;
    protected BloomFilter<Feature> knownFeatures;

    public FeatureDictWeighter(SquashingFunction ws) {
        this(ws, new HashMap<Feature, Double>());
    }

    public FeatureDictWeighter(SquashingFunction ws, Map<Feature, Double> w) {
        this.squashingFunction = ws;
        this.weights = w;
        this.unknownFeatures = new BloomFilter<Feature>(.01, Math.max(100, weights.size()));
        this.knownFeatures = new BloomFilter<Feature>(.01, Math.max(100, weights.size()));

        Configuration c = Configuration.getInstance();
        countFeatures = c == null || c.countFeatures;
    }

    public void put(Feature goal, double i) {
        weights.put(goal, i);
    }

    public abstract double w(Map<Feature, Double> fd);

    public String listing() {
        return "feature dict weighter <no string available>";
    }

    public SquashingFunction getSquashingFunction() {
        return squashingFunction;
    }

    public void countFeature(Feature g) {
        if (!this.countFeatures) {
            return; // early escape
        }
        if (this.weights.size() == 0) {
            return; // early escape
        }
        if (!this.weights.containsKey(g)) {
            if (!unknownFeatures.contains(g)) {
                unknownFeatures.add(g);
                numUnknownFeatures++;
            }
        } else if (!knownFeatures.contains(g)) {
            knownFeatures.add(g);
            numKnownFeatures++;
        }
    }

    public int seenUnknownFeatures() {
        return numUnknownFeatures;
    }

    public int seenKnownFeatures() {
        return numKnownFeatures;
    }
}
