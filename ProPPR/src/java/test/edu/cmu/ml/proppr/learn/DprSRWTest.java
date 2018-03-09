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

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;

import edu.cmu.ml.proppr.examples.PosNegRWExample;
import edu.cmu.ml.proppr.learn.DprSRW;
import edu.cmu.ml.proppr.learn.ExampleFactory.DprExampleFactory;
import edu.cmu.ml.proppr.learn.tools.Exp;
import edu.cmu.ml.proppr.learn.tools.Linear;
import edu.cmu.ml.proppr.learn.tools.Sigmoid;
import edu.cmu.ml.proppr.learn.tools.Tanh;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.math.MuParamVector;
import edu.cmu.ml.proppr.util.math.ParamVector;
import edu.cmu.ml.proppr.util.math.SimpleParamVector;

public class DprSRWTest extends L2PosNegLossSRWTest {
	@Override
	public void initSrw() {
		srw = new DprSRW();
		this.srw.setRegularizer(new RegularizationSchedule(this.srw, new Regularize()));
		factory = new DprExampleFactory();
	}
	
	@Override
	public void defaultSrwSettings() {
		super.defaultSrwSettings();
		srw.getOptions().set("apr","epsilon","1e-7");
	}
}
