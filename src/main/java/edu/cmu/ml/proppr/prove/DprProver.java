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

import edu.cmu.ml.proppr.prove.wam.*;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.StatusLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * prover using depth-first approximate personalized pagerank
 *
 * @author wcohen, krivard
 */
public class DprProver extends Prover<StateProofGraph> {

    public static final double STAYPROB_DEFAULT = 0.0;
    public static final double STAYPROB_LAZY = 0.5;
    private static final Logger log = LogManager.getLogger(DprProver.class);
    private static final boolean TRUELOOP_ON = true;
    protected final double stayProbability;
    protected final double moveProbability;
    // for debug
    protected Backtrace<State> backtrace = new Backtrace<State>(log);
    protected ProofGraph current;
    private int maxTreeDepth = -1;

    public DprProver() {
        this(false);
    }

    public DprProver(boolean lazyWalk) {
        this(lazyWalk, new APROptions());
    }

    public DprProver(boolean lazyWalk, APROptions apr) {
        this((lazyWalk ? STAYPROB_LAZY : STAYPROB_DEFAULT), apr);
    }

    protected DprProver(double stayP, APROptions apr) {
        super(apr);
        this.stayProbability = stayP;
        this.moveProbability = 1.0 - stayProbability;
    }

    public DprProver(APROptions apr) {
        this(false, apr);
    }

    @Override
    public String toString() {
        return String.format("dpr:%.6g:%g", apr.epsilon, apr.alpha);
    }

    @Override
    public Prover<StateProofGraph> copy() {
        DprProver copy = new DprProver(this.stayProbability, apr);
        copy.setWeighter(weighter);
        return copy;
    }

    @Override
    public Map<State, Double> prove(StateProofGraph pg, StatusLogger status) {
        if (this.current != null) {
            throw new IllegalStateException("DprProver not threadsafe -- one instance per thread only, please!");
        }
        this.current = pg;

        Map<State, Double> p = new HashMap<State, Double>();
        Map<State, Double> r = new HashMap<State, Double>();
        State state0 = pg.getStartState();
        r.put(state0, 1.0);
        backtrace.start();
        int numPushes = 0;
        int numIterations = 0;
        double iterEpsilon = 1.0;
        for (int pushCounter = 0; ; ) {
            iterEpsilon = Math.max(iterEpsilon / 10, apr.epsilon);
            if (log.isDebugEnabled()) { log.debug("Starting iteration with eps = " + iterEpsilon); }
            pushCounter = this.proveState(pg, p, r, state0, 0, iterEpsilon, status);
            numIterations++;
            if (log.isInfoEnabled() && status.due(1)) {
                log.info(Thread.currentThread() + " iteration: " + numIterations + " pushes: " + pushCounter + " " +
                                 "r-states: " + r.size() + " p-states: " + p.size());
            }
            if (iterEpsilon == apr.epsilon && pushCounter == 0) { break; }
            numPushes += pushCounter;
        }
        //if(log.isInfoEnabled()) log.info(Thread.currentThread()+" total iterations "+numIterations+" total pushes
        // "+numPushes);

        //clear state
        this.current = null;
        return p;
    }

    @Override
    public Class<StateProofGraph> getProofGraphClass() {
        return StateProofGraph.class;
    }

    // wwc: might look at using a PriorityQueue together with r to find
    // just the top things.

    // wwc: could we use canonical hashes instead of states somehow, to
    // make this smaller/faster for lookups? or make hashcode for the
    // state the canonical hash

    @Override
    public void configure(String param) {
        if (param.startsWith("maxTreeDepth=")) {
            this.maxTreeDepth = Integer.parseInt(param.substring(param.indexOf('=') + 1));
        }
    }

    protected int proveState(StateProofGraph pg, Map<State, Double> p, Map<State, Double> r,
                             State u, int pushCounter, double iterEpsilon,
                             StatusLogger status) {
        return proveState(pg, p, r, u, pushCounter, 1, iterEpsilon, status);
    }

    protected int proveState(StateProofGraph pg, Map<State, Double> p, Map<State, Double> r,
                             State u, int pushCounter, int depth, double iterEpsilon,
                             StatusLogger status) {
        if (this.maxTreeDepth > 0 && depth > this.maxTreeDepth) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Rejecting eps %f @depth %d > %d ru %.6f deg %d state %s", iterEpsilon,
                                        depth, this.maxTreeDepth, r.get(u), -1, u));
            }
            return pushCounter;
        }
        try {
            int deg = pg.pgDegree(u);
            if (r.get(u) / deg > iterEpsilon) {
                backtrace.push(u);
                pushCounter += 1;
                try {

                    List<Outlink> outs = pg.pgOutlinks(u, TRUELOOP_ON);
                    double z = 0.0;
                    for (Outlink o : outs) {
                        o.wt = this.weighter.w(o.fd);
                        if (Double.isInfinite(o.wt) || Double.isNaN(o.wt)) {
                            log.warn("Illegal weight (" + Double.toString(o.wt) + ") at outlink " + o.child + ";"
                                             + Dictionary.buildString(o.fd, new StringBuilder(), "\n\t"));
                        }
                        z += o.wt;
                    }
                    if (z == 0) {
                        //then we're in trouble
                        log.warn("Illegal graph: weight on this node has nowhere to go");
                        for (Outlink o : outs) {
                            log.warn("Outlink: " + Dictionary.buildString(o.fd, new StringBuilder(), "; "));
                        }
                    }

                    // push this state as far as you can
                    while (r.get(u) / deg > iterEpsilon) {
                        double ru = r.get(u);
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Pushing eps %f @depth %d ru %.6f deg %d z %.6f state %s",
                                                    iterEpsilon, depth, ru, deg, z, u));
                        } else if (log.isInfoEnabled() && status.due(2)) {
                            log.info(String.format("Pushing eps %f @depth %d ru %.6f deg %d z %.6f state %s",
                                                   iterEpsilon, depth, ru, deg, z, u));
                        }

                        // p[u] += alpha * ru
                        addToP(p, u, ru);
                        // r[u] *= (1-alpha) * stay?
                        r.put(u, (1.0 - apr.alpha) * stayProbability * ru);

                        // for each v near u:
                        for (Outlink o : outs) {
                            // skip 0-weighted links
                            if (o.wt == 0) { continue; }
                            // r[v] += (1-alpha) * move? * Muv * ru
                            Dictionary.increment(r, o.child, (1.0 - apr.alpha) * moveProbability * (o.wt / z) * ru, "" +
                                    "(elided)");
                        }

                        if (log.isDebugEnabled()) {
                            // sanity-check r:
                            double sumr = 0;
                            for (Double d : r.values()) { sumr += d; }
                            double sump = 0;
                            for (Double d : p.values()) { sump += d; }
                            if (Math.abs(sump + sumr - 1.0) > apr.epsilon) {
                                log.debug("Should be 1.0 but isn't: after push sum p + r = " + sump + " + " + sumr + " = " + (sump + sumr));
                            }
                        }
                    }

                    // for each v near u:
                    for (Outlink o : outs) {
                        // proveState(v):
                        // current pushcounter is passed down, gets incremented and returned, and
                        // on the next for loop iter is passed down again...
                        if (o.child.equals(pg.getStartState())) { continue; }
                        if (o.wt == 0) { continue; }
                        pushCounter = this.proveState(pg, p, r, o.child, pushCounter, depth + 1, iterEpsilon, status);
                    }
                } catch (LogicProgramException e) {
                    backtrace.rethrow(e);
                }
                backtrace.pop(u);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Rejecting eps %f @depth %d ru %.6f deg %d state %s", iterEpsilon, depth,
                                            r.get(u), deg, u));
                }
            }
        } catch (LogicProgramException e) {
            this.backtrace.rethrow(e);
        }
        return pushCounter;
    }

    /**
     * Template: Subclasses may add side effects
     */
    protected void addToP(Map<State, Double> p, State u, double ru) {
        Dictionary.increment(p, u, apr.alpha * ru, "(elided)");
    }

    public double getAlpha() {
        return apr.alpha;
    }
}
