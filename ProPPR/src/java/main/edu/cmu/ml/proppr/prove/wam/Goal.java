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

import edu.cmu.ml.proppr.prove.wam.Argument;

public class Goal implements Comparable<Goal> {
    protected String functor;
    protected Argument[] args;
    protected int arity;
	public Goal(String functor, Argument ... args) {
		this.functor = functor;
		this.args = args;
		this.arity = args.length;
	}
	public static Goal build(String functor, String ... constArgs) {
		Argument[] args = new Argument[constArgs.length];
		for (int i=0; i<constArgs.length; i++) {
			args[i] = new ConstantArgument(constArgs[i]);
		}
		return new Goal(functor,args);
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(functor);
		if (this.arity > 0) {
			sb.append("(");
			boolean first=true;
			for (Argument a : args) {
				if (!first) sb.append(",");
				first=false;
				sb.append(a.getName());
			}
			sb.append(")");
		}
		return sb.toString();
	}
	public String getFunctor() {
		return functor;
	}
	public Argument[] getArgs() {
		return args;
	}
	public Argument getArg(int i) {
		return args[i];
	}
	public int getArity() {
		return this.arity;
	}
	@Override
	public int compareTo(Goal o) {
		int k = this.functor.compareTo(o.functor);
		if (k!=0) return k;
		for (int i=0; i<this.arity; i++) {
			if (i>= o.arity) return -1;
			k = this.args[i].getName().compareTo(o.args[i].getName());
			if (k!=0) return k;
		}
		return 0;
	}
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Goal)) return false;
		Goal g = (Goal) o;
		if (!this.functor.equals(g.functor)) return false;
		if (this.arity != g.arity) return false;
		for (int i=0; i<this.arity; i++) {
			if (!this.args[i].equals(g.args[i])) return false;
		}
		return true;
	}
	@Override
	public int hashCode() {
		return functor.hashCode() ^ Arrays.hashCode(args);
	}
}
