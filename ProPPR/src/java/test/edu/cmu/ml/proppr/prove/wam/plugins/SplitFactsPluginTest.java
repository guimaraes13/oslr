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

package edu.cmu.ml.proppr.prove.wam.plugins;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import edu.cmu.ml.proppr.GrounderTest;
import edu.cmu.ml.proppr.prove.wam.LogicProgramException;
import edu.cmu.ml.proppr.prove.wam.Outlink;
import edu.cmu.ml.proppr.prove.wam.Query;
import edu.cmu.ml.proppr.prove.wam.WamInterpreter;
import edu.cmu.ml.proppr.util.Configuration;

public class SplitFactsPluginTest {
	public static final String ADDLFACTS = "src/testcases/classifyPredict_minAlphaAdditions.cfacts";
	@Test
	public void test() throws LogicProgramException {
		int input=0;
		int output = 0;
		int constants = Configuration.USE_WAM;
		int modules = 0;
		Configuration c = new Configuration(
				("--programFiles "+GrounderTest.RULES+":"+GrounderTest.FACTS+":"+ADDLFACTS).split(" "),
				input,output,constants,modules);
		assertEquals("# of plugins",c.plugins.length,1);
		assertEquals("# of members",((SplitFactsPlugin)c.plugins[0]).plugins.size(),2);
		assertTrue("claim",c.plugins[0]._claim("validClass/1"));
		
		Query q = Query.parse("validClass(X)");
		WamInterpreter interp = new WamInterpreter(c.program,c.plugins);
		int queryStartAddr = c.program.size();
		q.variabilize();
		c.program.append(q);
		interp.executeWithoutBranching(queryStartAddr);
		List<Outlink> outs = c.plugins[0].outlinks(interp.saveState(), interp, true);
		assertEquals("# outlinks",6,outs.size());
	}

}
