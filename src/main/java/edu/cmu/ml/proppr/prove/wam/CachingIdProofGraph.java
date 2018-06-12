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

package edu.cmu.ml.proppr.prove.wam;

import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.graph.InferenceGraph;
import edu.cmu.ml.proppr.graph.LearningGraphBuilder;
import edu.cmu.ml.proppr.learn.tools.FixedWeightRules;
import edu.cmu.ml.proppr.prove.FeatureDictWeighter;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.ConcurrentSymbolTable;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.SymbolTable;
import edu.cmu.ml.proppr.util.math.LongDense;
import edu.cmu.ml.proppr.util.math.SimpleSparse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/* ************************** optimized version of the proofgraph  *********************** */
public class CachingIdProofGraph extends ProofGraph implements InferenceGraph {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    private LongDense.ObjVector<SimpleSparse.FloatMatrix> nodeVec;
    private ConcurrentSymbolTable<State> nodeTab;
    private SymbolTable<Feature> featureTab;
    private int edgeCount = 0;

    public CachingIdProofGraph(Query query, APROptions apr, WamProgram program,
                               WamPlugin... plugins) throws LogicProgramException {
        super(query, apr, program, plugins);
    }

    public CachingIdProofGraph(InferenceExample ex, APROptions apr, SymbolTable<Feature> featureTab, WamProgram program,
                               WamPlugin... plugins) throws LogicProgramException {
        super(ex, apr, featureTab, program, plugins);
    }

    public CachingIdProofGraph(ConcurrentSymbolTable.HashingStrategy<State> strat) {
        super();
        nodeVec = new LongDense.ObjVector<SimpleSparse.FloatMatrix>();
        this.featureTab = new ConcurrentSymbolTable<Feature>();
        nodeTab = new ConcurrentSymbolTable<State>(strat);
    }

    public CachingIdProofGraph emptyCopy() throws LogicProgramException {
        CachingIdProofGraph copy = new CachingIdProofGraph(nodeTab.getHashingStrategy());
        copy.featureTab = this.featureTab;
        return copy;
    }

    @Override
    protected void init(SymbolTable<Feature> featureTab) {
        nodeVec = new LongDense.ObjVector<SimpleSparse.FloatMatrix>();
        this.featureTab = featureTab;
        nodeTab = new ConcurrentSymbolTable<State>(new ConcurrentSymbolTable.HashingStrategy<State>() {
            @Override
            public Object computeKey(State s) {
                return s.canonicalHash();
            }

            @Override
            public boolean equals(State s1, State s2) {
                if (s1.canonicalHash() != s2.canonicalHash()) { return false; }
                s1.setCanonicalForm(interpreter, startState);
                s2.setCanonicalForm(interpreter, startState);
                return s1.canonicalForm().equals(s2.canonicalForm());
            }
        });
        this.nodeTab.insert(this.getStartState());
    }

    @Override
    public int getId(State u) {
        return nodeTab.getId(u);
    }

    @Override
    protected InferenceGraph _getGraph() {
        return this;
    }

    public boolean isCompleted(int uid) {
        return getState(uid).isCompleted();
    }

    @Override
    public State getState(int uid) {
        return nodeTab.getSymbol(uid);
    }

    @Override
    public int nodeSize() {
        return nodeTab.size();
    }

    @Override
    public int edgeSize() {
        return edgeCount;
    }

    @Override
    public String serialize() {
        return serialize(false);
    }

    @Override
    public String serialize(boolean featureIndex) {
        StringBuilder ret = new StringBuilder().append(this.nodeSize()) //numNodes
                .append("\t")
                .append(this.edgeCount)
                .append("\t"); // waiting for label dependency size
        int labelDependencies = 0;

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        if (featureIndex) {
            sb.append("\t");
            for (int fi = 1; fi <= this.featureTab.size(); fi++) {
                if (!first) { sb.append(LearningGraphBuilder.FEATURE_INDEX_DELIM); } else { first = false; }
                Feature f = this.featureTab.getSymbol(fi);
                sb.append(f);
            }
        }

        // foreach src node
        for (int u = getRootId(); u <= this.nodeSize(); u++) {
            SimpleSparse.FloatMatrix nearu = this.nodeVec.get(u);
            if (nearu == null) { continue; }
            HashSet<Integer> outgoingFeatures = new HashSet<Integer>();
            //foreach dst from src
            for (int vi = 0; vi < nearu.index.length; vi++) {
                int v = nearu.index[vi];
                sb.append("\t");
                sb.append(u).append(LearningGraphBuilder.SRC_DST_DELIM).append(v);
                sb.append(LearningGraphBuilder.EDGE_DELIM);
                SimpleSparse.FloatVector uvf = nearu.val[vi];
                //foreach feature on src,dst
                for (int fi = 0; fi < uvf.index.length; fi++) {
                    int f = uvf.index[fi];
                    double w = uvf.val[fi];
                    outgoingFeatures.add(fi);
                    sb.append(f).append(LearningGraphBuilder.FEATURE_WEIGHT_DELIM)
                            .append(w).append(LearningGraphBuilder.EDGE_FEATURE_DELIM);
                }
                // drop last ','
                sb.deleteCharAt(sb.length() - 1);
            }
            labelDependencies += outgoingFeatures.size() * nearu.index.length;
        }
        ret.append(labelDependencies).append(sb);
        return ret.toString();

    }

    @Override
    public Iterable<State> getOutState(State state, FeatureDictWeighter weighter) throws LogicProgramException {
        int degree = getDegreeByIdWithoutLazyExpansion(getId(state));
        List<State> outStates = new ArrayList<>(degree);
        for (int i = 0; i < degree; i++) {
            int vi = getIthNeighborById(degree, i, weighter);
            outStates.add(getState(vi));
        }
        return outStates;
    }

    @Override
    public void setOutlinks(int uid, List<Outlink> outlinks) {
        setOutlinks(uid, outlinks, null);
    }

    public void setOutlinks(int uid, List<Outlink> outlinks, FeatureDictWeighter weighter) {
        edgeCount += outlinks.size();
        nodeVec.set(uid, outlinksAsMatrix(outlinks, weighter));
    }

    public SimpleSparse.FloatMatrix outlinksAsMatrix(List<Outlink> outlinks, FeatureDictWeighter weighter) {
        // convert the outlinks to a sparse matrix
        SimpleSparse.FloatMatrix mat = new SimpleSparse.FloatMatrix(outlinks.size());
        int i = 0;
        for (Outlink o : outlinks) {
            int vi = this.nodeTab.getId(o.child);
            // convert features for link from u to vi to a SimpleSparse.Vector
            int numFeats = o.fd.size();
            int[] featBuf = new int[numFeats];
            float[] featVal = new float[numFeats];
            int j = 0;
            for (Map.Entry<Feature, Double> e : o.fd.entrySet()) {
                if (weighter != null) { weighter.countFeature(e.getKey()); }
                featBuf[j] = featureTab.getId(e.getKey());
                featVal[j] = e.getValue().floatValue();
                j++;
            }
            mat.val[i] = new SimpleSparse.FloatVector(featBuf, featVal);
            mat.index[i] = vi;
            i++;
        }
        mat.sortIndex();
        return mat;
    }

    public int getDegreeById(int ui, FeatureDictWeighter weighter) throws LogicProgramException {
        expandIfNeeded(ui, weighter);
        return nodeVec.get(ui).index.length;
    }

    public int getDegreeByIdWithoutLazyExpansion(int ui) {
        SimpleSparse.FloatMatrix outlinks = nodeVec.get(ui);
        if (outlinks == null) { return 0; } else { return outlinks.index.length; }
    }

    public int getIthNeighborById(int ui, int i, FeatureDictWeighter weighter) throws LogicProgramException {
        expandIfNeeded(ui, weighter);
        return nodeVec.get(ui).index[i];
    }

    public double getIthWeightById(int ui, int i, LongDense.AbstractFloatVector params,
                                   FeatureDictWeighter weighter) throws LogicProgramException {
        expandIfNeeded(ui, weighter);
        SimpleSparse.FloatVector phi = nodeVec.get(ui).val[i];
        return Math.max(0, weighter.getSquashingFunction().compute(phi.dot(params, (float) weighter
                .getSquashingFunction().defaultValue())));
    }

    public double getTotalWeightOfOutlinks(int ui, LongDense.AbstractFloatVector params,
                                           FeatureDictWeighter weighter) throws LogicProgramException {
        expandIfNeeded(ui, weighter);
        double z = 0.0;
        int d = getDegreeById(ui, weighter);
        for (int i = 0; i < d; i++) {
            z += getIthWeightById(ui, i, params, weighter);
        }
        return z;
    }

    /**
     * Convert a vector indexed by state id's to a map
     **/

    public Map<State, Double> asMap(LongDense.FloatVector vec) {
        Map<State, Double> result = new HashMap<State, Double>();
        for (int uid = getRootId(); uid < vec.size(); uid++) {
            double vu = vec.get(uid);
            State s = getState(uid);
            if (s != null && vu >= 0.0) {
                result.put(s, vu);
            }
        }
        return result;
    }

    public int getRootId() {
        return 1;
    }

    /* produce and cache outlinks if you haven't yet */
    private void expandIfNeeded(int uid, FeatureDictWeighter weighter) throws LogicProgramException {
        if (nodeVec.get(uid) == null) {
            State u = nodeTab.getSymbol(uid);
            if (u != null) {
                List<Outlink> outlinks = this.computeOutlinks(u, true);
                setOutlinks(uid, outlinks, weighter);
            }
        }
    }

    public LongDense.FloatVector paramsAsVector(Map<Feature, Double> weights, Double dflt) {
        return paramsAsVector(weights, dflt, featureTab);
    }

    public static LongDense.FloatVector paramsAsVector(Map<Feature, Double> weights, Double dflt,
                                                       SymbolTable<Feature> featureTab) {
        int numFeats = featureTab.size();
        float[] featVal = new float[numFeats + 1];
        for (int j = 0; j < numFeats; j++) {
            featVal[j + 1] = Dictionary.safeGet(weights, featureTab.getSymbol(j + 1), dflt).floatValue();
        }
        return new LongDense.FloatVector(featVal, featVal.length - 1, dflt.floatValue());
    }

    @Override
    public String toString() {
        return this.serialize(true);
    }

    /**
     * Prune a graph by removing 'invisible states', which would
     * typically be states that have, on the call stack, some predicate
     * P, where P is used in theorem-proving but ignored in learning.
     * Edges between visible states are copied over.  It's assumed that
     * leaves and roots are visible. Edges between a visible state and
     * an invisible state are discarded - instead there will be edges,
     * with the appropriate weights, leading into each visible state
     * from its closest visible ancestor in the graph.
     **/

    public LongDense.FloatVector prune(LongDense.AbstractFloatVector params, FeatureDictWeighter weighter,
                                       VisibilityFilter test,
                                       LongDense.FloatVector p) {
        // prune the graph
        CachingIdProofGraph copy = prunedCopy(params, weighter, test);
        // recode the weights to the new node indices in the pruned copy
        LongDense.FloatVector prunedP = null;
        if (p != null) {
            prunedP = new LongDense.FloatVector(copy.nodeSize());
            // note smallest id index is 1, not 0
            for (int i = 1; i < p.size(); i++) {
                int j = copy.nodeTab.getId(getState(i));
                prunedP.set(j, p.get(i));
            }
        }
        // replace this graph with the copy
        this.nodeVec = copy.nodeVec;
        this.featureTab = copy.featureTab;
        this.nodeTab = copy.nodeTab;
        // return the new node weights
        return prunedP;
    }

    private CachingIdProofGraph prunedCopy(LongDense.AbstractFloatVector params, FeatureDictWeighter weighter,
                                           VisibilityFilter test) {
        try {
            CachingIdProofGraph pruned = emptyCopy();
            // line up the roots
            int prunedRootId = pruned.getId(getState(getRootId()));
            // collect two kinds of edges: 'real' and 'virtual' - see EdgeCollector comments
            EdgeCollector collector = new EdgeCollector();
            collectUnprunedEdges(collector, 0, params, weighter, new HashSet<Integer>(), test, getRootId(), getRootId
                    (), 1.0);
            // merge the two edge types together
            collector.makeVirtualEdgesReal(featureTab);
            // Now convert the collected edges to a CachingIdProofGraph.
            // Note that we need to recode the node id's into the right
            // range for the new graph.
            for (Integer u : collector.realEdgeSources()) {
                int ui = u.intValue();
                int uj = pruned.getId(getState(ui));
                int d = collector.realEdgeDegree(ui);
                // space for the FloatMatrix we'll create
                int[] destIndex = new int[d];
                SimpleSparse.FloatVector[] destValue = new SimpleSparse.FloatVector[d];
                int k = 0;
                for (Integer v : collector.realEdgeDestinations(ui)) {
                    int vi = v.intValue();
                    int vj = pruned.getId(getState(vi));
                    destIndex[k] = vj;
                    destValue[k] = collector.realEdgeFeatures(ui, vi);
                    k++;
                }
                SimpleSparse.FloatMatrix m = new SimpleSparse.FloatMatrix(destIndex, destValue);
                m.sortIndex();
                pruned.edgeCount += d;
                pruned.nodeVec.set(uj, m);
            }
            return pruned;
        } catch (LogicProgramException ex) {
            throw new IllegalStateException("I really shouldn't have seen a LogicProgramException here. How " +
                                                    "awkward...");
        }
    }

    /**
     * Recursively traverse the graph and figure out what edges to
     * keep
     **/

    private void collectUnprunedEdges(EdgeCollector collector, int depth,
                                      LongDense.AbstractFloatVector params, FeatureDictWeighter weighter,
                                      HashSet<Integer> previouslyProcessed, VisibilityFilter test,
                                      int visibleAncestor, int ui, double weightOfPathFromVisibleAncestor)
            throws LogicProgramException {
        if (previouslyProcessed.add(ui)) {
            // depth is kept track of just for debugging purposes....
            // trace(depth,ui,test,"check:" );
            int du = getDegreeByIdWithoutLazyExpansion(ui);
            for (int i = 0; i < du; i++) {
                int vi = getIthNeighborById(ui, i, weighter);
                double wuv = getIthWeightById(ui, i, params, weighter);
                boolean vIsVisible = test.visible(getState(vi));
                if (vIsVisible) {
                    //trace(depth,ui,test,"direct visible child "+vi+" of "+ui+": " );
                    if (ui == visibleAncestor) {
                        // add an edge from the ancestor to the visible node vi
                        SimpleSparse.FloatMatrix uiOutlinkMat = nodeVec.get(ui);
                        int vx = Arrays.binarySearch(uiOutlinkMat.index, vi);
                        collector.collectRealEdge(ui, vi, uiOutlinkMat.val[vx]);
                    } else if (ui != visibleAncestor) {
                        // add a virtual edge from the ancestor to the visible node vi
                        //trace(depth,ui,test,"indirect visible descendant "+vi+" of "+visibleAncestor+": " );
                        collector.collectVirtualEdge(visibleAncestor, vi, weightOfPathFromVisibleAncestor * wuv);
                    }
                    // recurse through v
                    collectUnprunedEdges(collector, depth + 1, params, weighter, previouslyProcessed, test, vi, vi,
                                         1.0);
                } else if (!vIsVisible) {
                    //trace(depth,ui,test,"direct invisible child "+vi+" of "+ui+": " );
                    // recurse through children of v
                    int dv = getDegreeByIdWithoutLazyExpansion(vi);
                    for (int j = 0; j < dv; j++) {
                        int xj = getIthNeighborById(vi, j, weighter);
                        double wvx = getIthWeightById(vi, j, params, weighter);
                        collectUnprunedEdges(collector, depth + 1, params, weighter, previouslyProcessed, test,
                                             visibleAncestor, xj,
                                             weightOfPathFromVisibleAncestor * wuv * wvx * (1.0 - apr.alpha) * (1.0 -
                                                     apr.alpha));
                    }
                } else {
                    throw new IllegalStateException("impossible!");
                }
            }
        }
    }

    /**
     * Construct a tree-like ascii representation of a proof graph for
     * debugging purposes.
     **/

    public String treeView(int maxDepth, int root, FeatureDictWeighter weighter) {
        return treeView(maxDepth, root, weighter, null);
    }

    /**
     * Construct a tree-like ascii representation of a proof graph for
     * debugging purposes.
     **/

    public String treeView(int maxDepth, int root, FeatureDictWeighter weighter, LongDense.AbstractFloatVector p) {
        StringBuilder sb = new StringBuilder();
        try {
            treeView(0, maxDepth, sb, new HashSet<Integer>(), weighter, p, null, root);
        } catch (LogicProgramException ex) {
            throw new IllegalStateException("I really shouldn't have seen a LogicProgramException here. How " +
                                                    "awkward...");
        }
        return sb.toString();
    }

    /**
     * Recursively traverse a graph like a tree.
     *
     * @param depth               - recursion depth
     * @param sb                  - buffer holding what's been "printed" so far
     * @param previouslyProcessed - record of what node id's have been visualized
     * @param weighter            - needed getIthNeighborById (why? because graphs are expanded as needed)
     * @param p                   - inferred node weights or null, to be printed if possible
     * @param featureVec          - features for the edge leading into this node
     * @param ui                  - node we've recursed down to
     **/

    private void treeView(int depth, int maxDepth, StringBuilder sb, HashSet<Integer> previouslyProcessed,
                          FeatureDictWeighter weighter,
                          LongDense.AbstractFloatVector p,
                          SimpleSparse.FloatVector featureVec, int ui)
            throws LogicProgramException {
        if (!previouslyProcessed.add(ui)) {
            if (ui != getRootId()) {
                for (int i = 0; i < depth; i++) { sb.append("|  "); }
                sb.append("%repeat%");
                sb.append(treeViewNodeSummary(p, ui, featureVec, weighter));
                sb.append("\n");
            }
        } else {
            for (int i = 0; i < depth; i++) { sb.append("|  "); }
            sb.append(getState(ui).canonicalForm() + ":");
            sb.append(treeViewNodeSummary(p, ui, featureVec, weighter));
            if (maxDepth >= 0 && (depth + 1 > maxDepth)) {
                sb.append(" ...\n");
            } else {
                sb.append("\n");
                // recurse to children
                int du = getDegreeByIdWithoutLazyExpansion(ui);
                for (int i = 0; i < du; i++) {
                    int vi = getIthNeighborById(ui, i, weighter);
                    SimpleSparse.FloatVector featureVecToV = nodeVec.get(ui).val[i];
                    treeView(depth + 1, maxDepth, sb, previouslyProcessed, weighter, p, featureVecToV, vi);
                }
            }
        }
    }

    private String treeViewNodeSummary(LongDense.AbstractFloatVector p, int ui, SimpleSparse.FloatVector featureVec,
                                       FeatureDictWeighter weighter) {
        StringBuilder sb1 = new StringBuilder();
        sb1.append(" #" + ui);
        if (p != null) {
            sb1.append(String.format(" [%f]", p.get(ui)));
        }
        if (featureVec != null) {
            sb1.append(" via {");
            for (int i = 0; i < featureVec.index.length; i++) {
                if (i > 0) { sb1.append(", "); }
                sb1.append(featureTab.getSymbol(featureVec.index[i]));
            }
            sb1.append("}");
        }
        if (getState(ui).isCompleted()) {
            sb1.append(" [_]");
        }
        return sb1.toString();
    }

    private void trace(int depth, int ui, VisibilityFilter test, String msg) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("|  ");
        }
        sb.append(msg);
        if (test.visible(getState(ui))) {
            sb.append("#" + ui + "  ");
        } else {
            sb.append("{" + ui + "} ");
        }
        sb.append(getState(ui).canonicalForm() + " ");
        logger.info(sb.toString());
    }

    /**
     * Object used by the prune() method to decide which nodes are
     * 'visible'.
     */
    public static interface VisibilityFilter {

        public boolean visible(State state);
    }

    /**
     * Buffers up edges to add to a pruned version of this proofgraph.
     * Real edges are between visible nodes; virtual edges are from a
     * visible node through a path through invisible nodes to a visible
     * descendant node.
     */

    private static class EdgeCollector {

        static final HashSet<Integer> EMPTYSET = new HashSet<Integer>();
        HashMap<Integer, HashMap<Integer, SimpleSparse.FloatVector>> space = new HashMap<Integer, HashMap<Integer,
                SimpleSparse.FloatVector>>();
        HashMap<Integer, HashMap<Integer, Double>> accum = new HashMap<Integer, HashMap<Integer, Double>>();

        /**
         * Collect an edge that connects a visible node to its closest
         * visible descendent in the unpruned graph
         **/
        public void collectVirtualEdge(int ui, int vi, double delta) {
            //System.out.println("++ virt edge from "+ui+" to "+vi);
            if (accum.get(ui) == null) {
                accum.put(ui, new HashMap<Integer, Double>());
            }
            if (accum.get(ui).get(vi) == null) {
                accum.get(ui).put(vi, 0.0);
            }
            double old = accum.get(ui).get(vi).doubleValue();
            accum.get(ui).put(vi, old + delta);
        }

        /**
         * Combine the virtual and real edges
         */
        public void makeVirtualEdgesReal(SymbolTable<Feature> featureTab) {
            int virtualFeatureId = featureTab.getId(new Feature("subproof"));
            // loop through the virtual edges u->v
            for (Integer u : accum.keySet()) {
                for (Integer v : accum.get(u).keySet()) {
                    if (space.get(u) != null && space.get(u).get(v) != null) {
                        // there's also a real edge from u to v, so extend the
                        // real edge's feature vector with the virtual feature,
                        // with the weight from the accumulator
                        SimpleSparse.FloatVector fuv = space.get(u).get(v);
                        int n = fuv.index.length;
                        int[] index1 = new int[n + 1];
                        float[] val1 = new float[n + 1];
                        for (int i = 0; i < n; i++) {
                            index1[i] = fuv.index[i];
                            val1[i] = fuv.val[i];
                        }
                        index1[n] = virtualFeatureId;
                        val1[n] = (float) accum.get(u).get(v).doubleValue();
                        space.get(u).put(v, new SimpleSparse.FloatVector(index1, val1));
                    } else {
                        // there's no real edge connecting these, so create a new
                        // feature vector for this edge
                        int[] index1 = new int[]{virtualFeatureId};
                        float[] val1 = new float[]{(float) accum.get(u).get(v).doubleValue()};
                        collectRealEdge(u.intValue(), v.intValue(), new SimpleSparse.FloatVector(index1, val1));
                    }
                }
            }
        }

        /**
         * Collect an edge that directly connected visible nodes in the
         * unpruned graph
         **/
        public void collectRealEdge(int ui, int vi, SimpleSparse.FloatVector featureVec) {
            //System.out.println("++ real edge from "+ui+" to "+vi);
            if (space.get(ui) == null) {
                space.put(ui, new HashMap<Integer, SimpleSparse.FloatVector>());
            }
            space.get(ui).put(vi, featureVec);
        }

        public boolean hasRealEdge(int ui) {
            return space.get(ui) != null;
        }

        public boolean hasRealEdgeDest(int ui, int vi) {
            return space.get(ui) != null && space.get(ui).get(vi) != null;
        }

        public Set<Integer> realEdgeSources() {
            return space.keySet();
        }

        public Set<Integer> realEdgeDestinations(int ui) {
            return space.get(ui).keySet();
        }

        public int realEdgeDegree(int ui) {
            return space.get(ui).keySet().size();
        }

        public SimpleSparse.FloatVector realEdgeFeatures(int ui, int vi) {
            return space.get(ui).get(vi);
        }

        public boolean hasVirtualEdge(int ui) {
            return accum.get(ui) != null;
        }

        public Set<Integer> virtualEdgeSources() {
            return accum.keySet();
        }

        public Set<Integer> virtualEdgeDestinations(int ui) {
            if (accum.get(ui) == null) {
                return EMPTYSET;
            } else {
                return accum.get(ui).keySet();
            }
        }

        public int virtualEdgeDegree(int ui) {
            if (accum.get(ui) == null) {
                return 0;
            } else {
                return accum.get(ui).keySet().size();
            }
        }

        public double virtualEdgeWeight(int ui, int vi) {
            return accum.get(ui).get(vi).doubleValue();
        }
    }

    /**
     * Implements visibilty test by checking for any 'pruned' predicate
     * on the callstack.
     */

    public static class PredicatePruner implements VisibilityFilter {

        private final FixedWeightRules rules;

        public PredicatePruner(FixedWeightRules rules) {
            this.rules = rules;
        }

        @Override
        public boolean visible(State state) {
            if (rules == null) { return true; }
            // test to see if any 'pruned' predicate is on the stack
            String jumpTo = state.getJumpTo();
            if (jumpTo != null && rules.isFixed(state.getJumpTo())) {
                return false;
            }
            for (CallStackFrame frame : state.getCalls()) {
                jumpTo = frame.getJumpTo();
                if (jumpTo != null && rules.isFixed(jumpTo)) {
                    return false;
                }
            }
            return true;
        }
    }

}
