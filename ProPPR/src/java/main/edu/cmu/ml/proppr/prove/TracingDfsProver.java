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

import edu.cmu.ml.proppr.prove.wam.LogicProgramException;
import edu.cmu.ml.proppr.prove.wam.ProofGraph;
import edu.cmu.ml.proppr.prove.wam.State;
import edu.cmu.ml.proppr.prove.wam.StateProofGraph;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.Dictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A prover with scores based on simple depth-first-search, which
    additional prints out a detailed trace.
 * @author "William Cohen <wcohen@cs.cmu.edu>"
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 *
 */
public class TracingDfsProver extends DfsProver {
	/**
	 * The logger
	 */
	public static final Logger logger = LogManager.getLogger();
	public TracingDfsProver(APROptions apr) {
		super(apr);
	}
	public TracingDfsProver(FeatureDictWeighter w, APROptions apr, boolean trueLoop) {
		super(w,apr,trueLoop);
	}
	@Override
	protected void beforeDfs(State state, ProofGraph pg, int depth) throws LogicProgramException {
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<depth;i++) sb.append("| ");
		if (!state.isCompleted()) {
			Dictionary.buildString(pg.getInterpreter().pendingGoals(state), sb, ", ");
			sb.append(" => ");
		}
		sb.append(pg.fill(state));
		logger.info(sb.toString());
	}
	@Override
	public Prover<StateProofGraph> copy() {
		return new TracingDfsProver(this.weighter, this.apr, this.trueLoop);
	}
}
