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

package edu.cmu.ml.proppr.graph;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.cmu.ml.proppr.prove.wam.CachingIdProofGraph;
import edu.cmu.ml.proppr.prove.wam.State;
import edu.cmu.ml.proppr.util.ConcurrentSymbolTable;

public class CachingIdGraphTest extends InferenceGraphTestTemplate {

	@Override
	public InferenceGraph getGraph() {
		return new CachingIdProofGraph(new ConcurrentSymbolTable.HashingStrategy<State>() {
			@Override
			public Object computeKey(State s) {
				return s.canonicalHash();
			}
			@Override
			public boolean equals(State s1, State s2) {
				if (s1.canonicalHash() != s2.canonicalHash()) return false;
				return s1.canonicalForm().equals(s2.canonicalForm());
			}});
	}

}
