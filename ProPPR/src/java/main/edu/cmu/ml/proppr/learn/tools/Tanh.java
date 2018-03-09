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

public class Tanh extends SquashingFunction {

	@Override
	public double compute(double sum) {
		// kmm & th 30 july 2014 to make return >=0
		return Math.max(0,Math.tanh(sum));
	}

	@Override
	public double computeDerivative(double weight) {
		return (1 - compute(weight) * compute(weight));
	}

	@Override
	public double defaultValue() {
		return 0.0;
	}

	@Override
	public String toString() { return "tanh"; }

//	@Override
//	public double projection(double rw, double alpha, int nonRestartNodeNum) {
//		return arcTanh(rw * (1 - alpha) / (alpha * nonRestartNodeNum));
//	}
	private double arcTanh (double z) {
		if (z>1 || z<-1) return -Double.MAX_VALUE;
		return 0.5 * (Math.log(1.0 + z) - Math.log(1.0 - z));
	}
}
