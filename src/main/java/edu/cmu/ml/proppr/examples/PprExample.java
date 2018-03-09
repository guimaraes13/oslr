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

import edu.cmu.ml.proppr.graph.LearningGraph;
import gnu.trove.map.TIntDoubleMap;

public class PprExample extends PosNegRWExample {

    // length = sum(nodes i) (degree of i) = #edges
    public double[][] M;
    // length = sum(edges e) (# features on e) = #feature assignments
    public int[] dM_feature_id;
    public double[] dM_value;
    // length = sum(nodes i) degree of i = #edges
    public int[][] dM_lo;
    public int[][] dM_hi;

    public PprExample(String name, LearningGraph graph, TIntDoubleMap queryVec,
                      int[] pos, int[] neg) {
        super(name, graph, queryVec, pos, neg);
        this.allocate();
    }

    @Override
    protected void allocate() {
        super.allocate();
        this.M = new double[graph.node_hi][];
        this.dM_lo = new int[graph.node_hi][];
        this.dM_hi = new int[graph.node_hi][];
        for (int uid = 0; uid < graph.node_hi; uid++) {
            int udeg = graph.node_near_hi[uid] - graph.node_near_lo[uid];
            this.M[uid] = new double[udeg];
            this.dM_lo[uid] = new int[udeg];
            this.dM_hi[uid] = new int[udeg];
        }
        this.dM_feature_id = new int[graph.labelDependencySize()];
        this.dM_value = new double[graph.labelDependencySize()];
    }

}
