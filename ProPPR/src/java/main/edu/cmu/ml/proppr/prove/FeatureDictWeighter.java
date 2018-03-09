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

package edu.cmu.ml.proppr.prove;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;;

import com.skjegstad.utils.BloomFilter;

import edu.cmu.ml.proppr.learn.tools.SquashingFunction;
import edu.cmu.ml.proppr.prove.wam.Feature;
import edu.cmu.ml.proppr.prove.wam.Goal;
import edu.cmu.ml.proppr.util.Configuration;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.multithreading.NamedThreadFactory;

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
		this(ws,new HashMap<Feature,Double>());
	}
	public FeatureDictWeighter(SquashingFunction ws, Map<Feature,Double> w) {
		this.squashingFunction = ws;
		this.weights = w;
		this.unknownFeatures = new BloomFilter<Feature>(.01,Math.max(100, weights.size()));
		this.knownFeatures = new BloomFilter<Feature>(.01,Math.max(100, weights.size()));

		Configuration c = Configuration.getInstance(); countFeatures = c!=null ? c.countFeatures : true;
	}
	public void put(Feature goal, double i) {
		weights.put(goal,i);
	}
	public abstract double w(Map<Feature, Double> fd);
	public String listing() {
		return "feature dict weighter <no string available>";
	}
	public SquashingFunction getSquashingFunction() { return squashingFunction; }
	public void countFeature(Feature g) {
		if (!this.countFeatures) return; // early escape
		if (this.weights.size() == 0) return; // early escape
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
