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


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.cmu.ml.proppr.examples.PosNegRWExample;
import edu.cmu.ml.proppr.learn.tools.LossData;
import edu.cmu.ml.proppr.learn.tools.LossData.LOSS;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.SRWOptions;
import edu.cmu.ml.proppr.util.SymbolTable;
import edu.cmu.ml.proppr.util.math.MuParamVector;
import edu.cmu.ml.proppr.util.math.ParamVector;
import gnu.trove.map.TIntDoubleMap;

public class RegularizeL2 extends Regularize {
	private static final Logger log = LogManager.getLogger(RegularizeL2.class);

	/**
	 * Remember - this update modifies the GRADIENT, which is applied later with learningRate() automatically.
	 * 
	 * L2 loss is mu * theta_f^2
	 * d/df L2 loss is then 2 * mu * theta_f
	 * @param f
	 * @param paramVec
	 * @return
	 */
	@Override
	protected void synchronousUpdate(SRWOptions c, ParamVector<String,?> params, String f,
			TIntDoubleMap gradient, LossData loss,
			SymbolTable<String> featureLibrary) {
		double value = Dictionary.safeGet(params, f);
		double ret = 2*c.mu*value;
		if (log.isDebugEnabled()) log.debug("Regularizing "+f+" += "+ret);
		loss.add(LOSS.REGULARIZATION, c.mu * Math.pow(value,2));
		gradient.adjustOrPutValue(featureLibrary.getId(f), ret, ret);
	}
	/**
	 * Remember - this update modifies the PARAMETER VECTOR, so we have to include learningRate() by hand.
	 * 
	 * We want to do g regularization updates simultaneously, as if they had been applied in previous gradients.
	 * 
	 * A single full regularization update is:
	 *   theta_f' = theta_f - learningRate * (2 * mu * theta_f )
	 *            = theta_f * (1 - 2 * mu * learningRate)
	 * 
	 * We can apply multiple updates by subbing theta' for theta, raising the multiplication factor to a power.
	 * In this way, g updates produce:
	 *   theta_f' = theta_f * (1 - 2 * mu * learningRate) ^ g
	 * 
	 * We want to pose the update as an increment, so we can use our threadsafe update method.
	 * 
	 *   theta_f' = theta_f + theta_f * (1 - 2 * mu * learningRate) ^ g - theta_f
	 *            = theta_f + theta_f * [(1 - 2 * mu * learningRate) ^ g - 1]
	 *            
	 * Thus our adjustment value is
	 *   theta_f * [(1 - 2 * mu * learningRate) ^ g - 1]
	 * @param apply
	 * @param f
	 * @param paramVec
	 */
	@Override
	protected void lazyUpdate(SRWOptions c, MuParamVector<String> params, ParamVector<String,?> apply,
			String f, LossData loss, double learningRate) {
		int gap = getGap(params,f);
		if (gap==0) return;

		double value = Dictionary.safeGet(params,f);
		double powerTerm = Math.pow(1 - 2 * c.mu * learningRate, gap);
		double weightDecay = value * (powerTerm - 1);
		//FIXME: opportunity for out-of-date `value`; probably ought to convert to a try loop
		if (log.isDebugEnabled()) log.debug("Regularizing "+f+" += "+ -weightDecay);
		double l2loss = gap * c.mu * Math.pow(value, 2);
		loss.add(LOSS.REGULARIZATION, l2loss);
		apply.adjustValue(f, weightDecay);
	}
}
