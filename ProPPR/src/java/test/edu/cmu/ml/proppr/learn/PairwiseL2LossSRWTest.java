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

package edu.cmu.ml.proppr.learn;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.cmu.ml.proppr.learn.tools.LossData.LOSS;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.StatusLogger;
import edu.cmu.ml.proppr.util.math.SimpleParamVector;

/**
 * Created by kavyasrinet on 10/4/15.
 */
public class PairwiseL2LossSRWTest extends SRWTest {
    public void initSrw() {
        srw = new SRW();
        this.srw.setRegularizer(new RegularizationSchedule(this.srw, new RegularizeL2()));
        this.srw.setLossFunction(new PairwiseL2SqLoss());
    }
    
	@Test
	public void testLogLoss() {
		int[] pos = new int[blues.size()]; { int i=0; for (String k : blues) pos[i++] = nodes.getId(k); }
		int[] neg = new int[reds.size()];  { int i=0; for (String k : reds)  neg[i++] = nodes.getId(k); }

		srw.clearLoss();
		srw.accumulateGradient(uniformParams, factory.makeExample("loss",brGraph, startVec, pos,neg), new SimpleParamVector<String>(), new StatusLogger());
		System.out.println(Dictionary.buildString(srw.cumulativeLoss().loss, new StringBuilder(),"\n").toString());
		assertTrue("loss must be nonzero",srw.cumulativeLoss().total() - srw.cumulativeLoss().loss.get(LOSS.REGULARIZATION) > 0);
	}
    
}
