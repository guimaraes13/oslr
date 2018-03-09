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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.junit.Test;


public class LearningGraphTest {
	
	@Test
	public void testArray() throws GraphFormatException {
		String s = "3	2	2	foo	1->2:1	3->2:1";
		LearningGraphBuilder b = new ArrayLearningGraphBuilder();
		LearningGraph g = (LearningGraph) b.deserialize(s);
		assertEquals("#nodes",3,g.nodeSize());
		assertEquals("#edges",2,g.edgeSize());
		assertEquals("#edges on 1",1,g.node_near_hi[1] - g.node_near_lo[1]);
		assertEquals("#features on 1->2",1,g.edge_labels_hi[0] - g.edge_labels_lo[0]);
	}

}
