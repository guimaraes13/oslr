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


import java.util.Map;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;;

import edu.cmu.ml.proppr.learn.tools.FixedWeightRules;
import edu.cmu.ml.proppr.prove.wam.CachingIdProofGraph;
import edu.cmu.ml.proppr.prove.wam.CallStackFrame;
import edu.cmu.ml.proppr.prove.wam.Goal;
import edu.cmu.ml.proppr.prove.wam.LogicProgramException;
import edu.cmu.ml.proppr.prove.wam.ProofGraph;
import edu.cmu.ml.proppr.prove.wam.State;
import edu.cmu.ml.proppr.prove.wam.StateProofGraph;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.StatusLogger;
import edu.cmu.ml.proppr.util.math.LongDense;
import edu.cmu.ml.proppr.util.math.SmoothFunction;

/**
 */
public class PruningIdPprProver extends IdPprProver {
	/**
	 * The logger
	 */
	public static final Logger logger = LogManager.getLogger();
	private CachingIdProofGraph.VisibilityFilter test;
	private FixedWeightRules prunedPredicateRules;

	public String toString() { 
		return String.format("p_ippr:%.6g:%g", apr.epsilon, apr.alpha);
	}

	public PruningIdPprProver(APROptions apr,FixedWeightRules prunedPredicateRules) {
		super(apr);
		this.test = new CachingIdProofGraph.PredicatePruner(prunedPredicateRules);
		this.prunedPredicateRules = prunedPredicateRules;
	}

	@Override
	public Prover<CachingIdProofGraph> copy() {
		PruningIdPprProver copy = new PruningIdPprProver(this.apr,this.prunedPredicateRules);
		copy.params = this.params;
		return copy;
	}

	@Override
	public Map<State, Double> prove(CachingIdProofGraph pg, StatusLogger status) {
		//logger.info("calling Prunedpredicaterules.prove");
		LongDense.FloatVector p = proveVec(pg, status);
		if (apr.traceDepth!=0) {
			logger.info("== before pruning:  edges/nodes "+pg.edgeSize()+"/"+pg.nodeSize());
			logger.info(pg.treeView(apr.traceDepth,apr.traceRoot,weighter,p));
		}
		LongDense.FloatVector prunedP = pg.prune(params,weighter,test,p);
		//logger.info("== after pruning:  edges/nodes "+pg.edgeSize()+"/"+pg.nodeSize());
		//logger.info(pg.treeView(weighter,prunedP));
		if (apr.traceDepth!=0) {
			logger.info("== after pruning:  edges/nodes "+pg.edgeSize()+"/"+pg.nodeSize());
			logger.info(pg.treeView(apr.traceDepth,apr.traceRoot,weighter,prunedP));
		}
		return pg.asMap(prunedP);
	}

}
