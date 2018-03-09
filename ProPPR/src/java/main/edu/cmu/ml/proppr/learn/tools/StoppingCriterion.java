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

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;;

public class StoppingCriterion {
	public static final int DEFAULT_MIN_STABLE_EPOCHS = 3;
	public static final double DEFAULT_MAX_PCT_IMPROVEMENT = 1.0;
	private static final Logger log = LogManager.getLogger(StoppingCriterion.class);
	/** Stop when the percentage improvement in loss has been no more
	 * than maxPctImprovementInLoss for minStableEpochs in a row, or
	 * when at least maxEpochs have occurred.
	 */
	public double maxPctImprovementInLoss;
	public int minStableEpochs;
	public int numConseqStableEpochs;
	public int maxEpochs;
	public int numEpochs;

	public StoppingCriterion(int maxEpochs,double maxPctImprovementInLoss, int minStableEpochs) {
		this.maxPctImprovementInLoss = maxPctImprovementInLoss;
		this.minStableEpochs = minStableEpochs;
		this.numConseqStableEpochs = 0;
		this.maxEpochs = maxEpochs;
		this.numEpochs = 0;
	}
	public StoppingCriterion(int maxEpochs) {
		this(maxEpochs, DEFAULT_MAX_PCT_IMPROVEMENT, DEFAULT_MIN_STABLE_EPOCHS);
	}
	public void recordEpoch() {
		numEpochs++;
	}

	public void recordConsecutiveLosses(LossData lossThisEpoch,LossData lossLastEpoch) {
		LossData diff = lossLastEpoch.diff(lossThisEpoch);
		double percentImprovement = 100 * diff.total()/lossThisEpoch.total();
		if (Math.abs(percentImprovement) > maxPctImprovementInLoss) {
			numConseqStableEpochs = 0;				
		} else {
			numConseqStableEpochs++;
		}
	}
	public boolean satisified() {
		boolean converged = numConseqStableEpochs >= minStableEpochs;
		return converged || (numEpochs>=maxEpochs);
	}
}
