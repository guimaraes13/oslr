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

/**
 * Leaky ReLU nonlinearity based on
 * Rectifier Nonlinearities Improve Neural Network Acoustic Models
 * Maas, Hannun, Ng
 * http://web.stanford.edu/~awni/papers/relu_hybrid_icml2013_final.pdf
 * 
 * See also the ReLU/LReLU sections in
 * http://cs231n.github.io/neural-networks-1/#actfun
 * @author krivard
 *
 * @param <G>
 */
public class LReLU<G> extends SquashingFunction<G> {
	private static final double LEAK=0.01;
	@Override
	public double compute(double sum) {
		return Math.max(LEAK*sum,sum);
	}

	@Override
	public double computeDerivative(double weight) {
		double grad = LEAK;
		if (weight > 0) {grad = 1;}            
		return grad;
	}

	@Override
	public double defaultValue() {
		return 1.0;
	}

	@Override
	public String toString() { return "leaky ReLU"; }
}
