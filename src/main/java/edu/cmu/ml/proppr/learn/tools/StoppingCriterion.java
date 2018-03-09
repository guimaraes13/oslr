/*
 * Online Structure Learner by Revision (OSLR) is an online relational
 * learning algorithm that can handle continuous, open-ended
 * streams of relational examples as they arrive. We employ
 * techniques from theory revision to take advantage of the already
 * acquired knowledge as a starting point, find where it should be
 * modified to cope with the new examples, and automatically update it.
 * We rely on the Hoeffding's bound statistical theory to decide if the
 * model must in fact be updated accordingly to the new examples.
 * The system is built upon ProPPR statistical relational language to
 * describe the induced models, aiming at contemplating the uncertainty
 * inherent to real data.
 *
 * Copyright (C) 2017-2018 Victor Guimarães
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

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cmu.ml.proppr.learn.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StoppingCriterion {

    public static final int DEFAULT_MIN_STABLE_EPOCHS = 3;
    public static final double DEFAULT_MAX_PCT_IMPROVEMENT = 1.0;
    private static final Logger log = LogManager.getLogger(StoppingCriterion.class);
    /**
     * Stop when the percentage improvement in loss has been no more
     * than maxPctImprovementInLoss for minStableEpochs in a row, or
     * when at least maxEpochs have occurred.
     */
    public double maxPctImprovementInLoss;
    public int minStableEpochs;
    public int numConseqStableEpochs;
    public int maxEpochs;
    public int numEpochs;

    public StoppingCriterion(int maxEpochs) {
        this(maxEpochs, DEFAULT_MAX_PCT_IMPROVEMENT, DEFAULT_MIN_STABLE_EPOCHS);
    }

    public StoppingCriterion(int maxEpochs, double maxPctImprovementInLoss, int minStableEpochs) {
        this.maxPctImprovementInLoss = maxPctImprovementInLoss;
        this.minStableEpochs = minStableEpochs;
        this.numConseqStableEpochs = 0;
        this.maxEpochs = maxEpochs;
        this.numEpochs = 0;
    }

    public void recordEpoch() {
        numEpochs++;
    }

    public void recordConsecutiveLosses(LossData lossThisEpoch, LossData lossLastEpoch) {
        LossData diff = lossLastEpoch.diff(lossThisEpoch);
        double percentImprovement = 100 * diff.total() / lossThisEpoch.total();
        if (Math.abs(percentImprovement) > maxPctImprovementInLoss) {
            numConseqStableEpochs = 0;
        } else {
            numConseqStableEpochs++;
        }
    }

    public boolean satisified() {
        boolean converged = numConseqStableEpochs >= minStableEpochs;
        return converged || (numEpochs >= maxEpochs);
    }
}
