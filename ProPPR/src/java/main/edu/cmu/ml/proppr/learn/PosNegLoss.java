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

import edu.cmu.ml.proppr.examples.PosNegRWExample;
import edu.cmu.ml.proppr.learn.tools.LossData;
import edu.cmu.ml.proppr.learn.tools.LossData.LOSS;
import edu.cmu.ml.proppr.util.SRWOptions;
import edu.cmu.ml.proppr.util.math.ParamVector;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.TIntDoubleMap;

public class PosNegLoss extends LossFunction {

	@Override
	public int computeLossGradient(ParamVector params, PosNegRWExample example,
			TIntDoubleMap gradient, LossData lossdata, SRWOptions c) {
		PosNegRWExample ex = (PosNegRWExample) example;
		int nonzero=0;
		
		// add empirical loss gradient term
		// positive examples
		double pmax = 0;
		for (int a : ex.getPosList()) {
			double pa = clip(ex.p[a]);
			if(pa > pmax) pmax = pa;
			for (TIntDoubleIterator da = ex.dp[a].iterator(); da.hasNext(); ) {
				da.advance();
				if (da.value()==0) continue;
				nonzero++;
				double aterm = -da.value() / pa;
				gradient.adjustOrPutValue(da.key(), aterm, aterm);
			}
			if (log.isDebugEnabled()) log.debug("+p="+pa);
			lossdata.add(LOSS.LOG, -Math.log(pa));
		}

		//negative instance booster
		double h = pmax + c.delta;
		double beta = 1;
		if(c.delta < 0.5) beta = (Math.log(1/h))/(Math.log(1/(1-h)));

		// negative examples
		for (int b : ex.getNegList()) {
			double pb = clip(ex.p[b]);
			for (TIntDoubleIterator db = ex.dp[b].iterator(); db.hasNext(); ) {
				db.advance();
				if (db.value()==0) continue;
				nonzero++;
				double bterm = beta * db.value() / (1 - pb);
				gradient.adjustOrPutValue(db.key(), bterm, bterm);
			}
			if (log.isDebugEnabled()) log.debug("-p="+pb);
			lossdata.add(LOSS.LOG, -Math.log(1.0-pb));
		}
		return nonzero;
	}

}
