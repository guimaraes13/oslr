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

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import edu.cmu.ml.proppr.Grounder;
import edu.cmu.ml.proppr.GrounderTest;
import edu.cmu.ml.proppr.examples.GroundedExample;
import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.prove.wam.LogicProgramException;
import edu.cmu.ml.proppr.prove.wam.ProofGraph;
import edu.cmu.ml.proppr.prove.wam.Query;
import edu.cmu.ml.proppr.prove.wam.StateProofGraph;
import edu.cmu.ml.proppr.prove.wam.WamBaseProgram;
import edu.cmu.ml.proppr.prove.wam.WamProgram;
import edu.cmu.ml.proppr.prove.wam.plugins.FactsPlugin;
import edu.cmu.ml.proppr.prove.wam.plugins.SparseGraphPlugin;
import edu.cmu.ml.proppr.prove.wam.plugins.SparseGraphPluginTest;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.StatusLogger;
import gnu.trove.map.hash.TIntDoubleHashMap;

public class PathDprProverTest {
	public static final String RULES = "src/testcases/sparseGraph/family.wam";

	@Test
	public void test() throws IOException, LogicProgramException {
		APROptions apr = new APROptions(); apr.epsilon=1e-5; apr.alpha=0.01;
		WamProgram program = WamBaseProgram.load(new File(RULES));
		WamPlugin plugins[] = new WamPlugin[] {SparseGraphPlugin.load(apr, new File(SparseGraphPluginTest.PLUGIN))};
		PathDprProver p = new PathDprProver(apr);
		
		Query query = Query.parse("kids(bette,Y)");
		StateProofGraph pg = new StateProofGraph(query,apr,program,plugins);
		p.prove(pg, new StatusLogger());
	}
	
	@Test
	public void foo() {
		TIntDoubleHashMap foo = new TIntDoubleHashMap();
		foo.put(1,0.0);
		foo.adjustOrPutValue(1, 0.5, 0.5);
		foo.adjustOrPutValue(2, 0.5, 0.5);
		System.out.println("1: "+foo.get(1));
		System.out.println("2: "+foo.get(2));
		
	}

}
