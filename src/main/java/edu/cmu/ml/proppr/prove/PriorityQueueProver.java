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

import edu.cmu.ml.proppr.prove.wam.CachingIdProofGraph;
import edu.cmu.ml.proppr.prove.wam.LogicProgramException;
import edu.cmu.ml.proppr.prove.wam.State;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.StatusLogger;
import edu.cmu.ml.proppr.util.math.LongDense;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.PriorityQueue;

/**
 * prover using depth-first approximate personalized pagerank
 *
 * @author wcohen, krivard
 */
public class PriorityQueueProver extends Prover<CachingIdProofGraph> {

    public static final double STAYPROB_DEFAULT = 0.0;
    public static final double STAYPROB_LAZY = 0.5;
    private static final Logger log = LogManager.getLogger(PriorityQueueProver.class);
    private static final boolean TRUELOOP_ON = true;
    protected final double stayProbability;
    protected final double moveProbability;

    public PriorityQueueProver() {
        this(false);
    }

    public PriorityQueueProver(boolean lazyWalk) {
        this(lazyWalk, new APROptions());
    }

    public PriorityQueueProver(boolean lazyWalk, APROptions apr) {
        this((lazyWalk ? STAYPROB_LAZY : STAYPROB_DEFAULT), apr);
    }

    protected PriorityQueueProver(double stayP, APROptions apr) {
        super(apr);
        this.stayProbability = stayP;
        this.moveProbability = 1.0 - stayProbability;
    }

    public PriorityQueueProver(APROptions apr) {
        this(false, apr);
    }

    @Override
    public String toString() {
        return String.format("qpr:%.6g:%g", apr.epsilon, apr.alpha);
    }

    @Override
    public Class<CachingIdProofGraph> getProofGraphClass() {
        return CachingIdProofGraph.class;
    }

    @Override
    public Map<State, Double> prove(CachingIdProofGraph cg, StatusLogger status) {
        //Map<State,Double> p = new HashMap<State,Double>();
        //Map<State,Double> r = new HashMap<State,Double>();
        LongDense.FloatVector p = new LongDense.FloatVector();
        LongDense.FloatVector r = new LongDense.FloatVector();
        //State state0 = pg.getStartState();
        //r.put(state0, 1.0);
        int state0 = cg.getRootId();
        r.set(state0, 1.0);

        PriorityQueue<QueueEntry> q = new PriorityQueue<QueueEntry>();
        int deg;
        try {
            deg = cg.getDegreeById(state0, null);
            q.add(new QueueEntry(state0, 1.0 / deg));
        } catch (LogicProgramException ex) {
            throw new IllegalStateException(ex);
        }

        LongDense.UnitVector params = new LongDense.UnitVector();

        int maxIterations = (int) (1.0 / apr.epsilon + 0.5);
        for (int n = 0; n < maxIterations && !q.isEmpty(); n++) {
            QueueEntry head = q.element();
            q.remove(head);
            int[] children;
            if (head.score > apr.epsilon) {
                try {
                    int uid = head.id;
                    deg = cg.getDegreeById(uid, null);
                    double z = cg.getTotalWeightOfOutlinks(uid, params, this.weighter);
                    // record states with scores to update
                    children = new int[deg + 1];
                    children[0] = uid;
                    for (int i = 0; i < deg; i++) {
                        int vid = cg.getIthNeighborById(uid, i, this.weighter);
                        q.remove(new QueueEntry(vid, r.get(vid)));
                        children[i + 1] = vid;
                    }
                    // push this state as far as you can
                    while (r.get(uid) / deg > apr.epsilon) {
                        double ru = r.get(uid);
                        p.inc(uid, ru);
                        r.set(uid, (1.0 - apr.alpha) * stayProbability * ru);
                        // for each v near u
                        for (int i = 0; i < deg; i++) {
                            // r[v] += (1-alpha) * move? * Muv * ru
                            //Dictionary.increment(r, o.child, (1.0-apr.alpha) * moveProbability * (o.wt / z) * ru,"
                            // (elided)");
                            double wuv = cg.getIthWeightById(uid, i, params, this.weighter);
                            int vid = cg.getIthNeighborById(uid, i, this.weighter);
                            r.inc(vid, (1.0 - apr.alpha) * moveProbability * (wuv / z) * ru);
                        }
                    }
                    // reinsert changed values on the queue
                    for (int i = 0; i < children.length; i++) {
                        int vi = children[i];
                        int degvi = cg.getDegreeById(vi, null);
                        double scorevi = r.get(vi) / degvi;
                        q.add(new QueueEntry(vi, scorevi));
                    }
                } catch (LogicProgramException e) {
                    throw new IllegalStateException(e);
                }
            } // if push
        }
        if (log.isInfoEnabled() && status.due(1)) {
            log.info(Thread.currentThread() + " r-states: " + r.size() + " p-states: " + p.size() + " q-size: " + q
                    .size());
        }
        return cg.asMap(p);
    }

    // wwc: might look at using a PriorityQueue together with r to find
    // just the top things.

    @Override
    public Prover<CachingIdProofGraph> copy() {
        PriorityQueueProver copy = new PriorityQueueProver(this.stayProbability, apr);
        copy.setWeighter(weighter);
        return copy;
    }

    public double getAlpha() {
        return apr.alpha;
    }

    private static class QueueEntry implements Comparable<QueueEntry> {

        public int id;
        public double score;

        public QueueEntry(int id, double score) {
            this.id = id;
            this.score = score;
        }

        public boolean equals(QueueEntry other) {
            return compareTo(other) == 0;
        }

        // highest-scoring things come first
        @Override
        public int compareTo(QueueEntry other) {
            double scoreCmp = this.score - other.score;
            if (scoreCmp < 0.0) { return +1; } else if (scoreCmp > 0.0) { return -1; } else {
                return (this.id - other.id);
            }
        }
    }
}
