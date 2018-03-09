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

import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.SymbolTable;

public class Feature implements Comparable<Feature> {
	public final String name;
	public Feature(FeatureBuilder f, SymbolTable<String> constants) {
		StringBuilder sb = new StringBuilder();
		boolean isWeighted = false;
		if (f.functor.endsWith(WamPlugin.WEIGHTED_SUFFIX)) {
			sb.append(f.functor.substring(0,f.functor.length()-WamPlugin.WEIGHTED_SUFFIX.length()));
			isWeighted = true;
		} else sb.append(f.functor);
		if (f.arity>0) {
			sb.append("(");
			for (int i=0; i<f.arity - (isWeighted?1:0); i++) {
				sb.append(constants.getSymbol(f.args[i])).append(",");
			}
			sb.setCharAt(sb.length()-1, ')');
		}
		this.name = sb.toString();
	}
	public Feature(String key) {
		this.name = key;
	}
	@Override
	public String toString() {
		return this.name;
	}
	@Override
	public int compareTo(Feature o) {
		return this.name.compareTo(o.name);
	}
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Feature)) return false;
		return this.name.equals(((Feature)o).name);
	}
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
}
