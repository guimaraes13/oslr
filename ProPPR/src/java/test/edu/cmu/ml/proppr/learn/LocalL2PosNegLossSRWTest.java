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


import java.util.Map;

import edu.cmu.ml.proppr.util.math.MuParamVector;
import edu.cmu.ml.proppr.util.math.ParamVector;

public class LocalL2PosNegLossSRWTest extends SRWTest {
	@Override
	public void initSrw() {
		srw = new SRW();
		this.srw.setRegularizer(new LocalRegularizationSchedule(this.srw, new RegularizeL2()));
		this.srw.setLossFunction(new PosNegLoss());
	}
//	@Override
//	public ParamVector<String,?> makeParams(Map<String,Double> foo) {
//		return new MuParamVector(foo);
//	}
//	@Override
//	public ParamVector<String,?> makeParams() {
//		return new MuParamVector();
//	}
}
