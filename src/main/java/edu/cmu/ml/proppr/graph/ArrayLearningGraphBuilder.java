/*
 * Online Structure Learner by Revision (OSLR) is an online relational
 * learning algorithm that can handle continuous, open-ended
 * streams of relational examples as they arrive. We employ
 * techniques from theory revision to take advantage of the already
 * acquired knowledge as a starting point, find where it should be
 * modified to cope with the new examples, and automatically update it.
 * We rely on the Hoeffding's bound statistical theory to decide if the
 * model must in fact be updated accordingly to the new examples.
 * The system is built upon ProPPR statistical relational language to
 * describe the induced models, aiming at contemplating the uncertainty
 * inherent to real data.
 *
 * Copyright (C) 2017-2018 Victor Guimarães
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

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cmu.ml.proppr.graph;

import edu.cmu.ml.proppr.util.SymbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ArrayLearningGraphBuilder extends LearningGraphBuilder {

    static final HashMap<String, ArrayLearningGraphBuilder> copies = new HashMap<String, ArrayLearningGraphBuilder>();
    public ArrayList<RWOutlink>[] outlinks = null;
    LearningGraph current = null;
    int labelSize = 0;
    int index = 0;

    @Override
    public LearningGraphBuilder copy() {
        return new ArrayLearningGraphBuilder();
    }

    @Override
    public LearningGraph create(SymbolTable<String> features) {
        if (current != null) { throw new IllegalStateException("ArrayLearningGraphBuilder not threadsafe"); }
        current = new LearningGraph(features);
        return current;
    }

    @Override
    public void setGraphSize(LearningGraph g, int nodeSize, int edgeSize, int dependencySize) {
        if (!current.equals(g)) { throw new IllegalStateException("ArrayLearningGraphBuilder not threadsafe"); }
        nodeSize += index;
        current.node_hi = nodeSize;
        current.node_near_hi = new int[nodeSize];
        current.node_near_lo = new int[nodeSize];
        current.setLabelDependencies(dependencySize);
        outlinks = new ArrayList[nodeSize];
        if (edgeSize < 0) { return; }
        initEdges(edgeSize);
    }

    private void initEdges(int edgeSize) {
        current.edge_dest = new int[edgeSize];
        current.edge_labels_hi = new int[edgeSize];
        current.edge_labels_lo = new int[edgeSize];

    }

    @Override
    public void addOutlink(LearningGraph g, int u, RWOutlink rwOutlink) {
        if (!current.equals(g)) { throw new IllegalStateException("ArrayLearningGraphBuilder not threadsafe"); }
        if (outlinks[u] == null) { outlinks[u] = new ArrayList<RWOutlink>(); }
        if (rwOutlink != null) {
            outlinks[u].add(rwOutlink);
            labelSize += rwOutlink.labelSize();
        } else { labelSize++; }
    }

    @Override
    public void freeze(LearningGraph g) {
        if (!current.equals(g)) { throw new IllegalStateException("ArrayLearningGraphBuilder not threadsafe"); }
        current.label_feature_id = new int[labelSize];
        current.label_feature_weight = new double[labelSize];
        if (current.edge_dest == null) {
            // then figure out size empirically and initialize
            int edgeSize = 0;
            for (int u = 0; u < current.node_hi; u++) {
                if (outlinks[u] == null) { continue; }
                edgeSize += outlinks[u].size();
            }
            initEdges(edgeSize);
        }
        int edge_cursor = 0;
        int label_cursor = 0;
        int label_deps = 0;
        HashSet<Integer> outgoingFeatures = null;
        for (int u = 0; u < current.node_hi; u++) {
            current.node_near_lo[u] = edge_cursor;
//			if (current.labelDependencySize() < 0) outgoingFeatures = new HashSet<Integer>();
            outgoingFeatures = new HashSet<Integer>();
            if (outlinks[u] != null) {
                for (RWOutlink o : outlinks[u]) {
                    current.edge_dest[edge_cursor] = o.nodeid;
                    current.edge_labels_lo[edge_cursor] = label_cursor;
                    for (int fi = 0; fi < o.labelSize(); fi++) {
//					for(Map.Entry<String,Double> it : o.fd.entrySet()) {
                        current.label_feature_id[label_cursor] = o.feature_id[fi];
                        current.label_feature_weight[label_cursor] = o.feature_value[fi];
//						if (current.labelDependencySize() < 0) outgoingFeatures.add(current
// .label_feature_id[label_cursor]);
                        outgoingFeatures.add(current.label_feature_id[label_cursor]);
                        label_cursor++;
                    }
                    current.edge_labels_hi[edge_cursor] = label_cursor;
                    edge_cursor++;
                }
//				if (current.labelDependencySize() < 0) label_deps += outgoingFeatures.size() * outlinks[u].size();
                label_deps += outgoingFeatures.size() * outlinks[u].size();
            }
            current.node_near_hi[u] = edge_cursor;
        }
//		if (current.labelDependencySize() < 0) current.setLabelDependencies(label_deps);
        current.setLabelDependencies(label_deps);
        init();
    }

    @Override
    public void index(int i0) {
        if (outlinks != null) {
            throw new IllegalStateException("Bad Programmer: You must call index() BEFORE setGraphSize().");
        }
        this.index = i0;
        current.setIndex(i0);
    }

    @Override
    public SymbolTable<String> getFeatureLibrary() {
        return current.featureLibrary;
    }

    private void init() {
        current = null;
        outlinks = null;
        labelSize = 0;
        index = 0;
    }

    private LearningGraphBuilder threadsafeCopy() {
        String name = Thread.currentThread().getName();
        if (!copies.containsKey(name)) { copies.put(name, new ArrayLearningGraphBuilder()); }
        return copies.get(name);
    }
}