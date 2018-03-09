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

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edu.cmu.ml.proppr.prove.wam.LogicProgramException;
import edu.cmu.ml.proppr.prove.wam.ProofGraph;
import edu.cmu.ml.proppr.prove.wam.Query;
import edu.cmu.ml.proppr.prove.wam.StateProofGraph;
import edu.cmu.ml.proppr.prove.wam.WamBaseProgram;
import edu.cmu.ml.proppr.prove.wam.WamProgram;
import edu.cmu.ml.proppr.prove.wam.plugins.SparseGraphPlugin;
import edu.cmu.ml.proppr.util.APROptions;


public class SparseGraphPluginTest {
	public static final String PLUGIN="src/testcases/sparseGraph/family.sparse";
	SparseGraphPlugin plugin;
	APROptions apr = new APROptions();
	WamProgram program = new WamBaseProgram();
	@Before
	public void setup() {
		plugin =  SparseGraphPlugin.load(apr,new File(PLUGIN));
	}
	
	@Test
	public void testClaim() {
		assertTrue("child",plugin._claim("child/2"));
		assertTrue("sister",plugin._claim("sister/2"));
		assertTrue("spouse",plugin._claim("spouse/2"));
	}

	@Test
	public void testDegree() throws LogicProgramException {
		Query q = Query.parse("child(pam,X)");
		StateProofGraph pg = new StateProofGraph(q,apr,program,plugin);
		// minus 1 for reset
		assertEquals(3,pg.pgDegree(pg.getStartState())-1);
	}
	
	@Test
	public void testRowEnd() throws LogicProgramException {
		StateProofGraph pg = new StateProofGraph(Query.parse("sister(yvette,X)"),apr,program,plugin);
		// minus 1 for reset
		assertEquals("Yvette should have no sisters\n",0,pg.pgDegree(pg.getStartState())-1);
		pg = new StateProofGraph(Query.parse("sister(theresa,X)"),apr,program,plugin);
		// minus 1 for reset
		assertEquals("Theresa should have 1 sister\n",1,pg.pgDegree(pg.getStartState())-1);
	}

}
