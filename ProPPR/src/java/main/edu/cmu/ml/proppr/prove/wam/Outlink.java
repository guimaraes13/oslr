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

import java.util.Collections;
import java.util.Map;

import edu.cmu.ml.proppr.util.Dictionary;

public class Outlink {
	public static final Map<Feature, Double> EMPTY_FD = Collections.emptyMap();
	public State child;
	public Map<Feature,Double> fd;
	public double wt=0.0;
	public Outlink(Map<Feature,Double> features, State state) {
		child = state;
		fd = features;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(child.toString());
		Dictionary.buildString(fd, sb, "\n  ");
		sb.append("\n");
		return sb.toString();
	}
}
