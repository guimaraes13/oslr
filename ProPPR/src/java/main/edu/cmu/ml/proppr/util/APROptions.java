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

package edu.cmu.ml.proppr.util;
import edu.cmu.ml.proppr.learn.tools.FixedWeightRules;

public class APROptions {
	public static final double EPS_DEFAULT = 1e-4, MINALPH_DEFAULT=0.1;
	public static final int MAXDEPTH_DEFAULT=20;
	public static final int TRACEDEPTH_DEFAULT = 0;
	public static final int TRACEROOT_DEFAULT = 1;
	public static final int STOPEARLY_DEFAULT = -1;

	private enum names {
		eps,
		epsilon,
		alph,
		alpha,
		depth,
		stop,
		stopEarly,
		traceDepth,
		traceRoot
	}
	public int maxDepth;
	public double alpha;
	public double epsilon;
	public int traceDepth, traceRoot;
	public int stopEarly;
	
	public APROptions() {
		this(EPS_DEFAULT,MINALPH_DEFAULT,MAXDEPTH_DEFAULT,TRACEDEPTH_DEFAULT,TRACEROOT_DEFAULT,STOPEARLY_DEFAULT);
	}
	public APROptions(double eps, double minalph,int depth,int traceDepth,int traceRoot,int stopEarly) {
		this.epsilon = eps;
		this.alpha = minalph;
		this.maxDepth = depth;
		this.traceDepth = traceDepth;
		this.traceRoot = traceRoot;		
		this.stopEarly = stopEarly;
	}
	public APROptions(String...optionValues) {
		this();
		for (String o : optionValues) {
			this.set(o.split("="));
		}
	}
	public void set(String...setting) {
		switch(names.valueOf(setting[0])) {
		case eps:
		case epsilon:
			this.epsilon = Double.parseDouble(setting[1]); return;
		case alph:
		case alpha:
			this.alpha = Double.parseDouble(setting[1]); return;
		case depth:
			this.maxDepth = Integer.parseInt(setting[1]); return;
		case traceDepth:
			this.traceDepth = Integer.parseInt(setting[1]); return;
		case traceRoot:
			this.traceRoot = Integer.parseInt(setting[1]); return;
		case stop:
		case stopEarly:
			this.stopEarly = Integer.parseInt(setting[1]); return;
		default:
			throw new IllegalArgumentException("No option to set '"+setting[0]+"'");
		}
	}
}
