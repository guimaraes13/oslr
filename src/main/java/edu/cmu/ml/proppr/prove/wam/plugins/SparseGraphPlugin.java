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

import edu.cmu.ml.proppr.prove.wam.Feature;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.ParsedFile;
import edu.cmu.ml.proppr.util.SimpleSymbolTable;
import edu.cmu.ml.proppr.util.SymbolTable;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class SparseGraphPlugin extends GraphlikePlugin {

    public static final String FILE_EXTENSION = ".sparse";
    public static final String INDEX_EXTENSION = ".i";
    public static final String MANIFEST = "sparseIndex.txt";
    private static final Logger log = LogManager.getLogger(SparseGraphPlugin.class);
    private static final long LOGUPDATE_MS = 10000;

    protected String name;
    protected SymbolTable<String> functors = new SimpleSymbolTable<String>();
    protected Map<Feature, Double> featureDict;
    protected TIntObjectMap<SparseMatrixIndex> index;
    protected TIntObjectMap<TObjectIntMap<String>> arg1s;
    protected TIntObjectMap<String[]> arg2s;

    public SparseGraphPlugin(APROptions apr, File matrixDir) {
        super(apr);
        this.name = matrixDir.getName();

        log.info("Loading sparse graph component " + matrixDir);
        long start = System.currentTimeMillis();
        arg1s = new TIntObjectHashMap<TObjectIntMap<String>>();
        arg2s = new TIntObjectHashMap<String[]>();

        index = new TIntObjectHashMap<SparseMatrixIndex>();
        for (String matrix : new ParsedFile(new File(matrixDir, MANIFEST))) {
            String[] parts = matrix.split("_");
            int[] partIDs = new int[parts.length];
            for (int i = 0; i < parts.length; i++) { partIDs[i] = functors.getId(parts[i]); }
            if (index.containsKey(partIDs[0])) {
                throw new IllegalArgumentException("Only one matrix allowed per functor. You've already used '" +
                                                           partIDs[0] + "'");
            }
            if (!arg1s.containsKey(partIDs[1])) { // read arg1.i
                arg1s.put(partIDs[1], new TObjectIntHashMap<String>());
                loadArgs(arg1s.get(partIDs[1]), new File(matrixDir, parts[1] + INDEX_EXTENSION));
            }
            if (!arg2s.containsKey(partIDs[2])) {
                ParsedFile rce = new ParsedFile(new File(matrixDir, matrix + ".rce"));
                Iterator<String> rceit = rce.iterator();
                rceit.next();
                String line = rceit.next();
                rce.close();
                if (line == null) {
                    throw new IllegalArgumentException("Bad format for " + matrix + ".rce: line 2 must list #cols");
                }
                int ncols = Integer.parseInt(line.trim());
                arg2s.put(partIDs[2], new String[ncols]);

                loadArgs(arg2s.get(partIDs[2]), new File(matrixDir, parts[2] + INDEX_EXTENSION));
            }
            try {
                index.put(partIDs[0], new SparseMatrixIndex(matrixDir, matrix, arg1s.get(partIDs[1]), arg2s.get
                        (partIDs[2])));
            } catch (Exception e) {
                log.error("Problem reading sparse matrix " + matrix + ".* in " + matrixDir, e);
                throw new RuntimeException(e);
            }
        }
        this.featureDict = new HashMap<Feature, Double>();
        this.featureDict.put(WamPlugin.pluginFeature(this, matrixDir.getName()), 1.0);
        this.featureDict = Collections.unmodifiableMap(this.featureDict);

        long del = System.currentTimeMillis() - start;
        if (del > LOGUPDATE_MS) {
            log.info("Finished loading sparse graph component " + matrixDir + " (" + (del / 1000.) + " sec)");
        }
    }

    private void loadArgs(TObjectIntMap<String> args, File file) {
        log.debug("Loading args file " + file.getName() + " in String...");
        ParsedFile parsed = new ParsedFile(file);
        for (String line : parsed) { args.put((line.trim()), parsed.getLineNumber()); }
        parsed.close();
    }

    /**
     * subroutine - populates an array of strings from a file
     **/
    private void loadArgs(String[] args, File file) {
        log.debug("Loading args file " + file.getName() + " in ConstantArgument...");
        ParsedFile parsed = new ParsedFile(file);
        for (String line : parsed) { args[parsed.getLineNumber()] = line.trim(); }
        parsed.close();
    }

    public static SparseGraphPlugin load(APROptions apr, File matrixDir) {
        return new SparseGraphPlugin(apr, matrixDir);
    }

    @Override
    protected void indexAdd(String label, String src, String dst) {
        throw new UnsupportedOperationException("Can't add to a sparse graph!");
    }

    @Override
    protected void indexAdd(String label, String src, String dst, double weight) {
        throw new UnsupportedOperationException("Can't add to a sparse graph!");
    }

    @Override
    protected boolean indexContains(String label) {
        return this.functors.hasId(clipArity(label));
    }

    private String clipArity(String label) {
        return label.substring(0, label.length() - GRAPH_ARITY.length());
    }

    @Override
    protected Collection<String> indexGet(String label) {
        label = clipArity(label);
        if (!functors.hasId(label)) { return DEFAULT_SRCLIST; }
        int id = functors.getId(label);
        if (!index.containsKey(id)) { return DEFAULT_SRCLIST; }
        return index.get(id).allSrc();
    }

    @Override
    protected TObjectDoubleMap<String> indexGet(String label, String src) {
        label = clipArity(label);
        if (!functors.hasId(label)) { return DEFAULT_DSTLIST; }
        TObjectDoubleMap<String> ret = index.get(functors.getId(label)).near(src);
        if (ret == null) { return DEFAULT_DSTLIST; }
        return ret;
    }

    @Override
    protected Map<Feature, Double> getFD() {
        return this.featureDict;
    }

    @Override
    public String about() {
        return this.getClass().getSimpleName() + ":" + this.name;
    }

}
