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

import edu.cmu.ml.proppr.examples.DprExample;
import edu.cmu.ml.proppr.examples.PosNegRWExample;
import edu.cmu.ml.proppr.examples.PprExample;
import edu.cmu.ml.proppr.graph.LearningGraph;
import gnu.trove.map.TIntDoubleMap;

public abstract class ExampleFactory {
		public abstract PosNegRWExample makeExample(String name, LearningGraph graph, TIntDoubleMap queryVec,
			int[] pos, int[] neg);
	public static class PprExampleFactory extends ExampleFactory {
		@Override
		public PosNegRWExample makeExample(String name, LearningGraph graph,
				TIntDoubleMap query, int[] pos, int[] neg) {
			return new PprExample(name, graph, query, pos, neg);
		}
	}
	public static class DprExampleFactory extends ExampleFactory {
		@Override
		public PosNegRWExample makeExample(String name, LearningGraph graph,
				TIntDoubleMap query, int[] pos, int[] neg) {
			return new DprExample(name,graph, query, pos, neg);
		}
	}
}
