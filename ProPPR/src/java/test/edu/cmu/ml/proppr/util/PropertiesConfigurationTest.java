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

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import edu.cmu.ml.proppr.prove.DfsProver;
import edu.cmu.ml.proppr.prove.PprProver;
import edu.cmu.ml.proppr.util.Configuration;
import edu.cmu.ml.proppr.util.ModuleConfiguration;

public class PropertiesConfigurationTest {

	@Test
	public void test() {
		// config.properties defines train, test, params, prover, queries, force (unary), and two nonexistant options.
		System.setProperty(Configuration.PROPFILE, "src/testcases/config.properties");
		ModuleConfiguration c = new ModuleConfiguration("--prover dfs".split(" "), 
				0,
				Configuration.USE_PARAMS,
				Configuration.USE_FORCE,
				Configuration.USE_PROVER | Configuration.USE_SQUASHFUNCTION);
		assertTrue("Didn't fetch properties from file",c.paramsFile != null);
		assertTrue("Didn't prefer command line properties",c.prover instanceof DfsProver);
		assertTrue("Didn't fetch unary argument",c.force);
		assertEquals("Didn't fetch apr options properly",0.01,c.apr.alpha,1e-10);
	}
	
	@Test
	public void testProperties() throws ParseException {

		Options options = new Options();
		options.addOption(
				OptionBuilder
				.withLongOpt("force")
				.withDescription("Ignore errors and run anyway")
				.create());

		DefaultParser parser = new DefaultParser();
		Properties props = new Properties();
//		props.put("--force", true);
		props.setProperty("--force","true");
		CommandLine line = parser.parse(options, new String[0], props);
		assertTrue(line.hasOption("force"));
	}
}
