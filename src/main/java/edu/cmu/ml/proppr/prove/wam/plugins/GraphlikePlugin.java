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

package edu.cmu.ml.proppr.prove.wam.plugins;

import edu.cmu.ml.proppr.prove.wam.*;
import edu.cmu.ml.proppr.util.APROptions;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * An 'extensional database' - restricted to be a labeled directed
 * graph, or equivalently, a set of f(+X,-Y) unit predicates.
 * <p>
 * As an alternative usage, the predicate f#(+X,-Y,-W) will return
 * the weight assigned to the edge, encoded as an atom which
 * can be converted back to a double with Doubel.parseDouble()
 *
 * @author "William Cohen <wcohen@cs.cmu.edu>"
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 */
public abstract class GraphlikePlugin extends WamPlugin {

    public static final String FILE_EXTENSION = "graph";
    protected static final TObjectDoubleMap<String> DEFAULT_DSTLIST = new TObjectDoubleHashMap<String>(0);
    protected static final List<String> DEFAULT_SRCLIST = Collections.emptyList();
    protected static final String GRAPH_ARITY = "/2";
    private static final Logger log = LogManager.getLogger(GraphlikePlugin.class);

    public GraphlikePlugin(APROptions apr) {
        super(apr);
    }

    public void addEdge(String functor, String src, String dst) {
        indexAdd(functor + GRAPH_ARITY, src, dst);
    }

    protected abstract void indexAdd(String label, String src, String dst);

    public void addEdge(String functor, String src, String dst, double weight) {
        if (weight <= 0) {
            log.error("Weights must be positive. Discarded graph edge " + functor + "(" + src + "," + dst + ") with " +
                              "weight " + weight);
        } else {
            indexAdd(functor + GRAPH_ARITY, src, dst, weight);
        }
    }

    protected abstract void indexAdd(String label, String src, String dst, double weight);

    @Override
    public boolean _claim(String jumpto) {
        return indexContains(jumpto);
    }

    protected abstract boolean indexContains(String label);

    @Override
    public List<Outlink> outlinks(State state, WamInterpreter wamInterp,
                                  boolean computeFeatures) throws LogicProgramException {
        List<Outlink> result = new LinkedList<Outlink>();
        String indexKey = state.getJumpTo();
        int delim = indexKey.indexOf(WamInterpreter.JUMPTO_DELIMITER);
        int arity = Integer.parseInt(indexKey.substring(delim + 1));
        boolean returnWeights = indexKey.substring(0, delim).endsWith(WamPlugin.WEIGHTED_SUFFIX);

        String srcConst = wamInterp.getConstantArg(arity, 1);
        String dstConst = wamInterp.getConstantArg(arity, 2);
        String weightConst = null;
        if (returnWeights) {
            indexKey = unweightedJumpto(indexKey);
            weightConst = wamInterp.getConstantArg(arity, 3);
            if (weightConst != null) {
                throw new LogicProgramException("predicate " + state.getJumpTo() + " called with bound third " +
                                                        "argument!");
            }
        }
        if (srcConst == null) {
            //throw new LogicProgramException("predicate "+state.getJumpTo()+" called with non-constant first
            // argument!");
            for (String src : indexGet(indexKey)) {
                wamInterp.restoreState(state);
                wamInterp.setArg(arity, 1, src);
                State srcState = wamInterp.saveState();
                outlinksPerSource(srcState, wamInterp, computeFeatures, returnWeights, indexKey, src, dstConst,
                                  weightConst, result, arity);
            }
        } else {
            outlinksPerSource(state, wamInterp, computeFeatures, returnWeights, indexKey, srcConst, dstConst,
                              weightConst, result, arity);
        }
        return result;
    }

    protected abstract Collection<String> indexGet(String label);

    private void outlinksPerSource(final State state, final WamInterpreter wamInterp,
                                   final boolean computeFeatures, final boolean returnWeights, final String indexKey,
                                   final String srcConst, final String dstConst, final String weightConst,
                                   final List<Outlink> result, final int arity) throws LogicProgramException {
        TObjectDoubleMap<String> values = this.indexGet(indexKey, srcConst);
        if (!values.isEmpty()) {
            try {
                values.forEachEntry(new TObjectDoubleProcedure<String>() {
                    @Override
                    public boolean execute(String val, double wt) {
                        try {
//							String weightString = returnWeights ? Double.toString(wt) : null;
                            if (dstConst != null) {
                                if (val.equals(dstConst)) {
                                    wamInterp.restoreState(state);
                                    if (returnWeights) {
                                        wamInterp.setWt(arity, 3, wt);
                                    }
                                    wamInterp.returnp();
                                    wamInterp.executeWithoutBranching();
                                } else {
                                    return true;
                                }
                            } else { // dstConst == null
                                wamInterp.restoreState(state);
                                wamInterp.setArg(arity, 2, val);
                                if (returnWeights) {
                                    wamInterp.setWt(arity, 3, wt);
                                }
                                wamInterp.returnp();
                                wamInterp.executeWithoutBranching();
                            }
                            if (computeFeatures) {
                                result.add(new Outlink(scaleFD(getFD(), wt), wamInterp.saveState()));
                            } else {
                                State save = wamInterp.saveState();
                                if (log.isDebugEnabled()) { log.debug("Result " + save); }
                                result.add(new Outlink(null, save));
                            }
                        } catch (LogicProgramException e) {
                            // wow this is awkward but whatcha gonna do
                            throw new IllegalStateException(e);
                        }
                        return true;
                    }
                });
            } catch (IllegalStateException e) {
                // awkward c.f. above
                if (e.getCause() instanceof LogicProgramException) { throw (LogicProgramException) e.getCause(); }
            }
        }
    }

    protected abstract TObjectDoubleMap<String> indexGet(String label, String src);

    protected abstract Map<Feature, Double> getFD();
}
