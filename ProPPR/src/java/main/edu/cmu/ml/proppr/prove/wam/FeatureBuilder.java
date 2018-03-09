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

package edu.cmu.ml.proppr.prove.wam;

import java.util.Arrays;

/**
 * Substitute for mixed-type feature-accumulator from Python
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 *
 */
public class FeatureBuilder {
	public String functor;
	public int arity;
	public int[] args;
	private int ai;
	public double wt=1.0;
	public FeatureBuilder(String f, int a) {
		functor = f;
		arity = a;
		args = new int[arity];
		ai = 0;
	}
	public void append(int a) {
		args[ai] = a;
		ai++;
	}
	public String toString() {
		return new StringBuilder(functor).append("/").append(arity).append(Arrays.toString(args)).toString();
	}
}
