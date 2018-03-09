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

import edu.cmu.ml.proppr.util.SymbolTable;

import java.util.Set;
import java.util.TreeSet;

public class LearningGraph {

    public final SymbolTable<String> featureLibrary;
    // length = #feature assignments (= sum(edge) #features on that edge)
    public int[] label_feature_id;
    public double[] label_feature_weight;

    // length = #edges
    public int[] edge_dest;
    public int[] edge_labels_lo;
    public int[] edge_labels_hi;

    // length = #nodes
    public int[] node_near_lo;
    public int[] node_near_hi;

    // node_lo = 0;
    public int node_hi;

    private int index = 0;
    private int labelDependencies = -1;

    public LearningGraph(SymbolTable<String> fL) {
        this.featureLibrary = fL;
    }

    public int[] getNodes() {
        int[] nodes = new int[node_hi];
        for (int i = 0; i < node_hi; i++) { nodes[i] = i; }
        return nodes;
    }

    public void setIndex(int i) {
        this.index = i;
    }

    public void setLabelDependencies(int i) {
        this.labelDependencies = i;
    }

    public void serialize(StringBuilder serialized) {
        serialized.append(nodeSize()) // nodes
                .append(LearningGraphBuilder.TAB).append(edgeSize()) //edges
                .append(LearningGraphBuilder.TAB).append(labelDependencySize()) // label dependencies
                .append(LearningGraphBuilder.TAB);
        for (int i = 0; i < getFeatureSet().size(); i++) {
            if (i > 0) { serialized.append(LearningGraphBuilder.FEATURE_INDEX_DELIM); }
            serialized.append(featureLibrary.getSymbol(i + 1));
        }
        for (int u = 0; u < node_hi; u++) {
            for (int ec = node_near_lo[u]; ec < node_near_hi[u]; ec++) {
                int v = edge_dest[ec];
                serialized.append(LearningGraphBuilder.TAB)
                        .append(u)
                        .append(LearningGraphBuilder.SRC_DST_DELIM)
                        .append(v).append(LearningGraphBuilder.EDGE_DELIM);
                for (int lc = edge_labels_lo[ec]; lc < edge_labels_hi[ec]; lc++) {
                    if (lc > edge_labels_lo[ec]) { serialized.append(LearningGraphBuilder.EDGE_FEATURE_DELIM); }
                    serialized.append(label_feature_id[lc]).append(LearningGraphBuilder.FEATURE_WEIGHT_DELIM).append
                            (label_feature_weight[lc]);
                }
            }
        }
    }

    public int nodeSize() {
        return node_hi - index;
    }

    public int edgeSize() {
        return edge_dest.length;
    }

    public int labelDependencySize() {
        return this.labelDependencies;
    }

    public Set<String> getFeatureSet() {
        TreeSet<String> features = new TreeSet<String>();
        for (int i : label_feature_id) {
            String f = featureLibrary.getSymbol(i);
            if (!features.contains(f)) { features.add(f); }
        }
        return features;
    }
}
