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

import com.skjegstad.utils.BloomFilter;
import edu.cmu.ml.proppr.prove.wam.*;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.ParsedFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class FactsPlugin extends WamPlugin {

    public static final String FILE_EXTENSION = "facts";
    public static final boolean DEFAULT_INDICES = false;
    private static final Logger log = LogManager.getLogger(FactsPlugin.class);
    private final String name;
    protected Map<Feature, Double> fd = new HashMap<Feature, Double>();
    protected Map<String, List<WeightedArgs>> indexJ = new HashMap<String, List<WeightedArgs>>();
    protected Map<JumpArgKey, List<WeightedArgs>> indexJA1 = new HashMap<JumpArgKey, List<WeightedArgs>>();
    protected Map<JumpArgKey, List<WeightedArgs>> indexJA2 = new HashMap<JumpArgKey, List<WeightedArgs>>();
    protected Map<JumpArgArgKey, List<WeightedArgs>> indexJA1A2 = new HashMap<JumpArgArgKey, List<WeightedArgs>>();
    // collected stats on how various indexes are used....
    int numUsesGoalsMatching = 0;
    int numUsesIndexF = 0;
    int numUsesIndexFA1 = 0;
    int numUsesIndexFA2 = 0;
    int numUsesIndexFA1A2 = 0;
    boolean useTernaryIndex;

    public FactsPlugin(APROptions apr, String name, boolean useTernaryIndex) {
        super(apr);
        this.fd.put(WamPlugin.pluginFeature(this, name), 1.0);
        this.name = name;
        this.useTernaryIndex = useTernaryIndex;
    }

    public static FactsPlugin load(APROptions apr, File f, boolean ternary) {
        return load(apr, f, ternary, -1);
    }

    public static FactsPlugin load(APROptions apr, File f, boolean ternary, int duplicates) {
        FactsPlugin p = new FactsPlugin(apr, f.getName(), ternary);
        p.load(f, duplicates);
        return p;

    }

    public void load(File f, int duplicates) {
        ParsedFile parsed = new ParsedFile(f);
        BloomFilter<String> lines = null;
        if (duplicates > 0) { lines = new BloomFilter<String>(1e-5, duplicates); }
        boolean exceeds = false;
        for (String line : parsed) {
            String[] parts = line.split("\t", 2);
            if (parts.length != 2) { parsed.parseError("expected at least 2 tab-delimited fields"); }
            if (duplicates > 0) {
                if (lines.contains(line)) {
                    log.warn("Skipping duplicate fact at " + f.getName() + ":" + parsed.getAbsoluteLineNumber() + ": " +
                                     "" + line);
                    continue;
                } else { lines.add(line); }
                if (!exceeds & parsed.getLineNumber() > duplicates) {
                    exceeds = true;
                    log.warn("Number of facts exceeds " + duplicates + "; duplicate detection may encounter false " +
                                     "positives. We should add a command line option to fix this.");
                }
            }
            addFact(parts[0], parts[1].split("\t"));
        }
    }

    public void addFact(String functor, String... args) {
        if (functor.indexOf(WamPlugin.WEIGHTED_SUFFIX) == functor.length() - 1) {
            if (log.isTraceEnabled()) {
                log.trace("Adding weighted fact " + functor + " " + Dictionary.buildString(args, new StringBuilder(),
                                                                                           " "));
            }
            addWeightedFact(
                    functor.substring(0, functor.length() - 1),
                    Double.parseDouble(args[args.length - 1]),
                    Arrays.copyOf(args, args.length - 1));
        } else {
            addWeightedFact(functor, WamPlugin.DEFAULT_DSTWEIGHT, args);
        }
    }

    public void addWeightedFact(String functor, double wt, String... args) {
        String jump = functor + "/" + args.length;
        WeightedArgs wargs = new WeightedArgs(args, wt);
        add(indexJ, jump, wargs);

        add(indexJA1, new JumpArgKey(jump, args[0]), wargs);

        if (args.length > 1) {
            add(indexJA2, new JumpArgKey(jump, args[1]), wargs);

            if (useTernaryIndex) {
                add(indexJA1A2, new JumpArgArgKey(jump, args[0], args[1]), wargs);
            }
        }
    }

    private static <T> void add(Map<T, List<WeightedArgs>> map, T key, WeightedArgs args) {
        if (!map.containsKey(key)) { map.put(key, new LinkedList<WeightedArgs>()); }
        map.get(key).add(args);
    }

    @Override
    public String about() {
        return "facts(" + name + ")";
    }

    @Override
    public boolean _claim(String jumpto) {
        return this.indexJ.containsKey(jumpto);
    }

    @Override
    public List<Outlink> outlinks(State state, WamInterpreter wamInterp,
                                  boolean computeFeatures) throws LogicProgramException {
        List<Outlink> result = new LinkedList<Outlink>();
        String jumpTo = state.getJumpTo();
        int delim = jumpTo.indexOf(WamInterpreter.JUMPTO_DELIMITER);
        int arity = Integer.parseInt(jumpTo.substring(delim + 1));
        boolean returnWeights = jumpTo.substring(0, delim).endsWith(WamPlugin.WEIGHTED_SUFFIX);
        if (returnWeights) { jumpTo = unweightedJumpto(state.getJumpTo()); }
        String[] argConst = new String[arity];
        for (int i = 0; i < arity; i++) { argConst[i] = wamInterp.getConstantArg(arity, i + 1); }
        if (returnWeights && argConst[arity - 1] != null) {
            throw new LogicProgramException("predicate " + state.getJumpTo() + " called with bound last argument!");
        }
        if (log.isDebugEnabled()) {
            log.debug("Fetching outlinks for " + jumpTo + ": " + Dictionary.buildString(argConst, new StringBuilder()
                    , ", "));
        }
        List<WeightedArgs> values = null;
        // fill values according to the query
        if (argConst[0] == null && (argConst.length == 1 || argConst[1] == null)) {
            values = indexJ.get(jumpTo);
        } else if (argConst[0] != null && (argConst.length == 1 || argConst[1] == null)) {
            values = indexJA1.get(new JumpArgKey(jumpTo, argConst[0]));
        } else if (argConst[0] == null && argConst.length > 1 && argConst[1] != null) {
            values = indexJA2.get(new JumpArgKey(jumpTo, argConst[1]));
        } else if (argConst.length > 1 && argConst[0] != null && argConst[1] != null) {
            if (useTernaryIndex) {
                values = indexJA1A2.get(new JumpArgArgKey(jumpTo, argConst[0], argConst[1]));
            } else {
                values = indexJA1.get(new JumpArgKey(jumpTo, argConst[0]));
                List<WeightedArgs> alternate = indexJA2.get(new JumpArgKey(jumpTo, argConst[1]));
                // treat null lists as empty lists here - wwc
                if (alternate == null) { alternate = new ArrayList<WeightedArgs>(); }
                if (values == null) { values = new ArrayList<WeightedArgs>(); }
                if (values.size() > alternate.size()) { values = alternate; }
            }
        } else {
            throw new IllegalStateException("Can't happen");
        }
        // then iterate through what you got
        if (values == null) { return result; }
        for (WeightedArgs val : values) {
            if (!check(argConst, val.args, returnWeights)) { continue; }
            wamInterp.restoreState(state);
            for (int i = 0; i < argConst.length; i++) {
                if (argConst[i] == null) {
                    if (i < val.args.length) {
                        wamInterp.setArg(arity, i + 1, val.args[i]);
                    } else if (returnWeights) {
                        log.debug("Using facts weight " + val.wt);
                        wamInterp.setWt(arity, i + 1, val.wt);
                    }
                }
            }
            wamInterp.returnp();
            wamInterp.executeWithoutBranching();
            if (computeFeatures) {
                result.add(new Outlink(scaleFD(this.fd, val.wt), wamInterp.saveState()));
            } else {
                result.add(new Outlink(null, wamInterp.saveState()));
            }
        }
        return result;
    }

    /**
     * Verify that all non-null values in the first arg match the values in the second arg.
     *
     * @param args
     * @param against
     * @param returnWeights
     * @return
     */
    private boolean check(String[] args, String[] against, boolean returnWeights) {
        for (int i = 0; i < args.length; i++) {
            if (i >= against.length) { return returnWeights; }
            if (args[i] != null && !(args[i].equals(against[i]))) { return false; }
        }
        return true;
    }

    public static class JumpArgKey {

        public final String jump;
        public final String arg;

        public JumpArgKey(String jump, String arg) {
            this.jump = jump;
            this.arg = arg;
        }

        @Override
        public int hashCode() {
            return jump.hashCode() ^ arg.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof JumpArgKey)) { return false; }
            JumpArgKey f = (JumpArgKey) o;
            return this.jump.equals(f.jump) && this.arg.equals(f.arg);
        }

        @Override
        public String toString() {
            return jump + ":" + arg;
        }
    }

    public static class JumpArgArgKey extends JumpArgKey {

        public final String arg2;

        public JumpArgArgKey(String jump, String arg1, String arg2) {
            super(jump, arg1);
            this.arg2 = arg2;
        }

        @Override
        public int hashCode() {
            return super.hashCode() ^ arg2.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o) && ((JumpArgArgKey) o).arg2.equals(this.arg2);
        }
    }

    public static class WeightedArgs {

        String[] args;
        double wt;

        public WeightedArgs(String[] args, double wt) {
            this.args = args;
            this.wt = wt;
        }
    }
}
