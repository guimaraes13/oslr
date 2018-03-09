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

import static org.junit.Assert.*;

import org.junit.Test;

import edu.cmu.ml.proppr.util.Configuration;
import edu.cmu.ml.proppr.util.ModuleConfiguration;

public class FixedWeightRulesTest {

	@Test
	public void test() {
		Configuration c = new Configuration("--fixedWeights f(thing,pos)".split(" "), 
				0, 0, Configuration.USE_FIXEDWEIGHTS,0);
		assertTrue("Raw fixedWeightRules", c.fixedWeightRules.isFixed("f(thing,pos)"));
	}
	
	@Test
	public void srwTest() {
		ModuleConfiguration c = new ModuleConfiguration("--fixedWeights f(thing,pos)".split(" "), 
				0, 0, Configuration.USE_FIXEDWEIGHTS,Configuration.USE_SRW);
		assertTrue("Raw fixedWeightRules", c.fixedWeightRules.isFixed("f(thing,pos)"));
		assertFalse("in an SRW", c.srw.trainable("f(thing,pos)"));
	}
	
	@Test
	public void testCascade() {
		Configuration c = new Configuration("--fixedWeights f(*=n:*=y".split(" "),
				0, 0, Configuration.USE_FIXEDWEIGHTS,0);
		assertTrue("Most rules", c.fixedWeightRules.isFixed("id(x,12,15)"));
		assertFalse("f(* rules", c.fixedWeightRules.isFixed("f(x,12,15)"));
	}

}
