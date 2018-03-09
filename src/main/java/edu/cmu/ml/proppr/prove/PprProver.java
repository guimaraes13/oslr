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

package edu.cmu.ml.proppr.prove;

import edu.cmu.ml.proppr.prove.wam.LogicProgramException;
import edu.cmu.ml.proppr.prove.wam.Outlink;
import edu.cmu.ml.proppr.prove.wam.State;
import edu.cmu.ml.proppr.prove.wam.StateProofGraph;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.StatusLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * prover using power iteration
 *
 * @author "William Cohen <wcohen@cs.cmu.edu>"
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 */
public class PprProver extends Prover<StateProofGraph> {

    private static final double SEED_WEIGHT = 1.0;
    private static final Logger log = LogManager.getLogger(PprProver.class);
    private static final boolean DEFAULT_TRACE = false;
    private static final boolean RESTART = true;
    private static final boolean TRUELOOP = true;
    private static final boolean NORMLX_TRUELOOP = true;
    protected boolean trace;

    public PprProver() {
        this(DEFAULT_TRACE);
    }

    public PprProver(boolean tr) {
        init(tr);
    }

    private void init(boolean tr) {
        trace = tr;
    }

    public PprProver(APROptions apr) {
        super(apr);
        init(DEFAULT_TRACE);
    }

    public PprProver(FeatureDictWeighter w, APROptions apr, boolean tr) {
        super(w, apr);
        init(tr);
    }

    @Override
    public String toString() {
        return "ppr:" + this.apr.maxDepth;
    }

    @Override
    public Prover<StateProofGraph> copy() {
        Prover<StateProofGraph> copy = new PprProver(weighter, this.apr, this.trace);
        return copy;
    }

    @Override
    public Map<State, Double> prove(StateProofGraph pg, StatusLogger status) {
        Map<State, Double> startVec = new HashMap<State, Double>();
        startVec.put(pg.getStartState(), SEED_WEIGHT);
        Map<State, Double> vec = startVec;

        for (int i = 0; i < this.apr.maxDepth; i++) {
            vec = walkOnce(pg, vec);
            if (log.isInfoEnabled() && status.due(1)) { log.info("iteration/descent " + (i - 1) + " complete"); }
            if (log.isDebugEnabled()) {
                log.debug("after iteration " + (i + 1) + " :" +
                                  Dictionary.buildString(vec, new StringBuilder(), "\n\t"));
            }
//			System.out.println("ppr iter "+(i+1)+" size "+vec.size());
/*
            for (Map.Entry<State,Double> e : vec.entrySet()) {
				try {
					State s = e.getKey();
					System.out.println(" - "+e.getValue()+"\t"+s.canonicalHash()+"\t"+pg.getInterpreter()
					.canonicalForm(pg.getStartState(),s));
				} catch (LogicProgramException ex) {
					throw new IllegalStateException(ex);
				}
			}
*/
        }
        return vec;
    }

    @Override
    public Class<StateProofGraph> getProofGraphClass() {
        return StateProofGraph.class;
    }

    protected Map<State, Double> walkOnce(StateProofGraph pg, Map<State, Double> vec) {
        Map<State, Double> nextVec = new HashMap<State, Double>();
        int i = 1, n = vec.size();
        // p[u in s] += alpha * s[u]
        Dictionary.increment(nextVec, pg.getStartState(), apr.alpha * SEED_WEIGHT);
        for (Map.Entry<State, Double> p : vec.entrySet()) {
            if (log.isInfoEnabled()) { log.info("state " + (i++) + " of " + n); }
            try {
                for (Map.Entry<State, Double> e : this.normalizedOutlinks(pg, p.getKey()).entrySet()) {
                    if (log.isTraceEnabled()) {
                        log.trace("walkonce normalizedOutlinks " + p.getKey() + " " + e.getValue() + " " + e.getKey());
                    }
                    // p[v] += (1-alpha) * Muv * p[u]
                    Dictionary.increment(nextVec, e.getKey(), e.getValue() * (1 - apr.alpha) * p.getValue(), "" +
                            "(elided)");
                }
            } catch (LogicProgramException e) {
                throw new IllegalStateException(e);
            }
        }
        return nextVec;
    }

    protected Map<State, Double> normalizedOutlinks(StateProofGraph pg, State s) throws LogicProgramException {
        List<Outlink> outlinks = pg.pgOutlinks(s, NORMLX_TRUELOOP);
        Map<State, Double> weightedOutlinks = new HashMap<State, Double>();
        double z = 0;
        for (Outlink o : outlinks) {
            o.wt = this.weighter.w(o.fd);
            weightedOutlinks.put(o.child, o.wt);
            z += o.wt;
        }

        for (Map.Entry<State, Double> e : weightedOutlinks.entrySet()) {
            e.setValue(e.getValue() / z);
        }
        return weightedOutlinks;
    }

    public void setMaxDepth(int i) {
        this.apr.maxDepth = i;
    }

    public void setTrace(boolean b) {
        this.trace = b;
    }

}
