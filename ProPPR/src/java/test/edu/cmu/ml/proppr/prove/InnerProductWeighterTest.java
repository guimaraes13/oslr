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

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;;
import org.junit.Test;

import edu.cmu.ml.proppr.prove.InnerProductWeighter;
import edu.cmu.ml.proppr.prove.wam.Feature;
import edu.cmu.ml.proppr.prove.wam.Goal;

public class InnerProductWeighterTest {

	@Test
	public void test() {
		BasicConfigurator.configure(); Logger.getRootLogger().setLevel(Level.INFO);
		HashMap<Feature,Double> weights = new HashMap<Feature,Double>();
		weights.put(new Feature("feathers"), 0.5);
		weights.put(new Feature("scales"), 0.3);
		weights.put(new Feature("fur"), 0.7);
		FeatureDictWeighter w = new InnerProductWeighter(weights);
		Feature ng = new Feature("hair");
		HashMap<Feature,Double> featureDict = new HashMap<Feature,Double>();
		featureDict.put(ng, 0.9);
		featureDict.putAll(weights);
		
		assertFalse("Should start empty!",w.unknownFeatures.contains(ng));
		for (Map.Entry<Feature,Double> e : featureDict.entrySet()) {
			e.setValue(e.getValue()-Math.random()/10);
		}
		w.w(featureDict);
		assertTrue("Wasn't added!",w.unknownFeatures.contains(ng));
	}

}
