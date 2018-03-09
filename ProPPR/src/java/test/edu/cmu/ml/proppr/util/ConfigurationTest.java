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

package edu.cmu.ml.proppr.util;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.cmu.ml.proppr.util.Configuration;

public class ConfigurationTest {

	@Test
	public void test() {		
		int inputFiles = Configuration.USE_TRAIN;
		int outputFiles = Configuration.USE_PARAMS;
		int constants = Configuration.USE_EPOCHS | Configuration.USE_FORCE | Configuration.USE_THREADS;
		int modules = Configuration.USE_TRAINER | Configuration.USE_SRW | Configuration.USE_SQUASHFUNCTION;
		ModuleConfiguration c = new ModuleConfiguration(
				"--train src/testcases/train.examples.grounded --params params.wts --threads 3 --srw ppr:reg=l2:sched=local:mu=0.001:eta=1.0 --epochs 20".split(" "),
				inputFiles,outputFiles,constants,modules);
		assertNotNull(c.squashingFunction);
	}

}
