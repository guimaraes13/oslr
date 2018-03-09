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

import edu.cmu.ml.proppr.graph.LightweightStateGraph;
import edu.cmu.ml.proppr.prove.wam.*;
import edu.cmu.ml.proppr.util.*;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.custom_hash.TObjectDoubleCustomHashMap;
import gnu.trove.strategy.HashingStrategy;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PathDprProver extends DprProver {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    private static final int NUMPATHS = 10;//TODO: move this to command line
    protected Map<State, TopPaths> paths = new HashMap<State, TopPaths>();

    public PathDprProver(APROptions apr) {
        super(apr);
    }

    public static void main(String[] args) throws LogicProgramException {
        CustomConfiguration c = new CustomConfiguration(args,
                                                        Configuration.USE_PARAMS, //input
                                                        0, //output
                                                        Configuration.USE_WAM | Configuration.USE_SQUASHFUNCTION,
                                                        //constants
                                                        0 //modules
        ) {
            String query;

            @Override
            protected void addCustomOptions(Options options, int[] flags) {
                options.getOption(Configuration.PARAMS_FILE_OPTION).setRequired(false);
                options.addOption(
                        OptionBuilder.withLongOpt("query")
                                .withArgName("functor(arg1,Var1)")
                                .hasArg()
                                .isRequired()
                                .withDescription("specify query to print top paths for")
                                .create());
                //TODO: add prompt option (for large datasets)
            }

            @Override
            protected void retrieveCustomSettings(CommandLine line,
                                                  int[] flags, Options options) {
                query = line.getOptionValue("query");
            }

            @Override
            public Object getCustomSetting(String name) {
                return query;
            }
        };
        PathDprProver p = new PathDprProver(c.apr);

        Query query = Query.parse((String) c.getCustomSetting(null));
        StateProofGraph pg = new StateProofGraph(query, c.apr, c.program, c.plugins);
        p.prove(pg, new StatusLogger());
    }

    @Override
    public Map<State, Double> prove(StateProofGraph pg, StatusLogger status) {
        Map<State, Double> ret = super.prove(pg, status);

        //after proving, print top paths for each solution
        logger.info("Q " + pg.getExample().getQuery());
        for (Map.Entry<State, Double> e : Dictionary.sort(ret)) {
            if (!paths.containsKey(e.getKey())) { continue; }
            Query q = pg.fill(e.getKey());
            logger.info("A   " + q + "    " + e.getValue());
            for (WeightedPath p : paths.get(e.getKey()).result()) {
                logger.info("P     " + p.humanReadable(pg, ret));
            }
        }
        return ret;
    }

    @Override
    protected void addToP(Map<State, Double> p, State u, double ru) {
        super.addToP(p, u, ru);
        double wt = ru;
        if (u.isCompleted() && !u.isFailed()) {
            // add paths to solution state to our top-paths tracker
            int[] path = createPath(this.backtrace);
            if (path == null) { return; }
            if (!paths.containsKey(u)) { paths.put(u, new TopPaths()); }
            TopPaths t = paths.get(u);
            t.add(wt, path);
        }
    }

    protected int[] createPath(Backtrace<State> b) {
        int[] path = new int[b.backtrace.size()];
        int i = path.length;
        for (State s : b.backtrace) {
            if (i < path.length && s.getJumpTo() == null) { return null; }
            path[--i] = current.getId(s);
        }
        return path;
    }

    /**
     * Model each path as an array of ints.
     * <p>
     * Track a new path only when <k are being tracked, or the weight of the new path is above the lowest weight in
     * the set.
     * <p>
     * Accumulate weight if a path comes up more than once.
     * <p>
     * At end of computation, return a list of the top paths ordered by their weight.
     *
     * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
     */
    protected class TopPaths {

        // Use hashcode from Arrays instead of Object
        TObjectDoubleMap<int[]> top = new TObjectDoubleCustomHashMap<int[]>(new HashingStrategy<int[]>() {
            @Override
            public int computeHashCode(int[] arg0) {
                return Arrays.hashCode(arg0);
            }

            @Override
            public boolean equals(int[] arg0, int[] arg1) {
                return Arrays.equals(arg0, arg1);
            }
        }, NUMPATHS);
        double leastWt;
        int[] leastPath;

        public void add(double wt, int[] path) {
            if (!qualify(wt, path)) { return; }
            top.adjustOrPutValue(path, wt, wt);
            if (top.size() > NUMPATHS) { top.remove(leastPath); }
            rebalance();
        }

        /**
         * Should we add this path?
         */
        public boolean qualify(double wt, int[] path) {
            return top.size() < NUMPATHS ||
                    top.containsKey(path) ||
                    wt > leastWt;
        }

        /**
         * Find and record lowest-weighted path
         */
        private void rebalance() {
            leastWt = Double.MAX_VALUE;
            for (TObjectDoubleIterator<int[]> it = top.iterator(); it.hasNext(); ) {
                it.advance();
                if (it.value() < leastWt) {
                    leastWt = it.value();
                    leastPath = it.key();
                }
            }
        }

        /**
         * Return ordered list of top paths & their weights
         */
        public WeightedPath[] result() {
            WeightedPath[] ret = new WeightedPath[top.size()];
            int i = 0;
            for (TObjectDoubleIterator<int[]> it = top.iterator(); it.hasNext(); ) {
                it.advance();
                ret[i++] = new WeightedPath(it.key(), it.value());
            }
            Arrays.sort(ret);
            return ret;
        }
    }

    protected class WeightedPath implements Comparable<WeightedPath> {

        int[] path;
        double wt;

        public WeightedPath(int[] p, double w) {
            this.wt = w;
            this.path = p;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof WeightedPath)) { return false; }
            WeightedPath w = (WeightedPath) o;
            if (w.path.length != this.path.length) { return false; }
            for (int i = 0; i < this.path.length; i++) {
                if (w.path[i] != this.path[i]) { return false; }
            }
            return true;
        }

        @Override
        public int compareTo(WeightedPath w) {
            return Double.compare(w.wt, this.wt);
        }

        /**
         * Convert the array of state IDs to a string representation of state jumpto, feature, state jumpto, feature,
         * etc
         **/
        public String humanReadable(StateProofGraph pg, Map<State, Double> ans) {
//			GroundedExample ex = pg.makeRWExample(ans);
            LightweightStateGraph g = pg.getGraph();
            StringBuilder sb = new StringBuilder(String.format("%+1.8f ", this.wt)).append(this.path.length).append(" ");
            for (int i = 1; i < path.length; i++) {
                sb.append(g.getState(path[i - 1]).getJumpTo());
                sb.append(" ->");
                for (Feature phi : g.getFeatures(g.getState(path[i - 1]), g.getState(path[i])).keySet()) {
                    sb.append(phi).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append(": ");
            }
            sb.delete(sb.length() - 2, sb.length() - 1);
            return sb.toString();
        }
    }
}
