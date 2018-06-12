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
import edu.cmu.ml.proppr.graph.LightweightStateGraph;
import edu.cmu.ml.proppr.prove.FeatureDictWeighter;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.SymbolTable;
import gnu.trove.strategy.HashingStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class StateProofGraph extends ProofGraph {

    private static final Logger log = LogManager.getLogger(ProofGraph.class);
    private LightweightStateGraph graph;

    public StateProofGraph(Query query, APROptions apr, WamProgram program,
                           WamPlugin... plugins) throws LogicProgramException {
        super(query, apr, program, plugins);//this(new InferenceExample(query,null,null), apr, program, plugins);
    }

    public StateProofGraph(InferenceExample ex, APROptions apr, SymbolTable<Feature> featureTab, WamProgram program,
                           WamPlugin... plugins) throws LogicProgramException {
        super(ex, apr, featureTab, program, plugins);
    }

    //	public StateProofGraph(InferenceExample ex, APROptions apr, WamProgram program, WamPlugin[] plugins) throws
    // LogicProgramException {
//		this(ex, apr, new SimpleSymbolTable<Feature>(), program, plugins);
//	}
    @Override
    protected void init(SymbolTable<Feature> featureTab) {
        this.graph = new LightweightStateGraph(new HashingStrategy<State>() {
            @Override
            public int computeHashCode(State s) {
                return s.canonicalHash();
            }

            @Override
            public boolean equals(State s1, State s2) {
                if (s1.canonicalHash() != s2.canonicalHash()) { return false; }
                s1.setCanonicalForm(interpreter, startState);
                s2.setCanonicalForm(interpreter, startState);
                return s1.canonicalForm().equals(s2.canonicalForm());
            }
        },
                                               featureTab);
    }

    @Override
    public int getId(State s) {
        return this.graph.getId(s);
    }

    @Override
    protected InferenceGraph _getGraph() {
        return this.graph;
    }

    public LightweightStateGraph getGraph() {
        return this.graph;
    }

    /**
     * The number of outlinks for a state, including the reset outlink back to the query.
     *
     * @throws LogicProgramException
     */
    public int pgDegree(State state) throws LogicProgramException {
        return this.pgDegree(state, true);
    }

    public int pgDegree(State state, boolean trueLoop) throws LogicProgramException {
        return this.pgOutlinks(state, trueLoop).size();
    }

    @Override
    public Iterable<State> getOutState(State state, FeatureDictWeighter weighter) throws LogicProgramException {
        return graph.near(state);
    }

    /**
     * Return the list of outlinks from the provided state, including a reset outlink back to the query.
     *
     * @param state
     * @param trueLoop
     * @return
     * @throws LogicProgramException
     */
    public List<Outlink> pgOutlinks(State state, boolean trueLoop) throws LogicProgramException {
        // wwc: why aren't trueloop, restart objects precomputed and shared?
        if (!this.graph.outlinksDefined(state)) {
            List<Outlink> outlinks = this.computeOutlinks(state, trueLoop);
            if (log.isDebugEnabled()) {
                // check for duplicate hashes
                Set<Integer> canons = new TreeSet<Integer>();
                for (Outlink o : outlinks) {
                    if (canons.contains(o.child.canon)) {
                        log.warn("Duplicate canonical hash found in outlinks of state " + state);
                    }
                    canons.add(o.child.canon);
                }
            }
            this.graph.setOutlinks(state, outlinks);
            return outlinks;
        }
        return this.graph.getOutlinks(state);
    }
}
