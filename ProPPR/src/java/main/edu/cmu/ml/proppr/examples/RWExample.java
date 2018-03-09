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

package edu.cmu.ml.proppr.examples;

import java.util.Map;

import edu.cmu.ml.proppr.graph.InferenceGraph;
import edu.cmu.ml.proppr.graph.LearningGraph;
import edu.cmu.ml.proppr.util.Dictionary;
import gnu.trove.map.TIntDoubleMap;


/**
 * Example for a supervised random walk, which links a query with the graph over which it is executed.
 * Example results are specified by the subclass.
 * @author krivard
 *
 */
public abstract class RWExample {
	protected String name;
	protected TIntDoubleMap queryVec;
	protected LearningGraph graph;
	public RWExample(String name, LearningGraph graph, TIntDoubleMap queryVec) {
		this.name = name;
		this.queryVec = queryVec;
		this.graph = graph;
	}
	public TIntDoubleMap getQueryVec() {
		return queryVec;
	}
	public LearningGraph getGraph() {
		return graph;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder(queryVec.size()+" queries:");
		Dictionary.buildString(queryVec,sb," ");
		return sb.toString();
	}
	/**
	 * Give the length of the example value.
	 * @return
	 */
	public abstract int length();
}
