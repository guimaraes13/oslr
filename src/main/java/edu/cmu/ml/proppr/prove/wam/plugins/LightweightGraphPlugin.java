/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2018 Victor Guimarães
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

package edu.cmu.ml.proppr.prove.wam.plugins;

import com.skjegstad.utils.BloomFilter;
import edu.cmu.ml.proppr.prove.wam.Feature;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.ParsedFile;
import gnu.trove.map.TObjectDoubleMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Alpha is used to limit the minimum restart weight, when you
 * use a uniformWeighter (or something related, like a fdWeighter
 * with learned weights that are close to 1.0).
 * <p>
 * With unit feature weights, a graph node of degree n will lead
 * to an lpState with degree n+1, and have a restart weight that
 * is 1/(n+1).  With alpha set, a new feature (named
 * 'alphaBooster') is introduced with a non-unit VALUE of n *
 * (alpha/(1-alpha)) for the restart weight, which means that
 * unit weights will give that edge a total weight of alpha.
 *
 * @author "William Cohen <wcohen@cs.cmu.edu>"
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 */
public class LightweightGraphPlugin extends GraphlikePlugin {

    private static final Logger log = LogManager.getLogger(LightweightGraphPlugin.class);
    protected Map<String, Map<String, TObjectDoubleMap<String>>> graph = new HashMap<String, Map<String,
            TObjectDoubleMap<String>>>();
    protected Map<Feature, Double> fd = new HashMap<Feature, Double>();
    protected String name;

    public LightweightGraphPlugin(APROptions apr, String name) {
        super(apr);
        this.fd.put(WamPlugin.pluginFeature(this, name), 1.0);
        this.name = name;
    }

    public static WamPlugin load(APROptions apr, File f) {
        return load(apr, f, -1);
    }

    /**
     * Return a simpleGraphComponent with all the components loaded from
     * a file.  The format of the file is that each line is a tab-separated
     * triple of edgelabel, sourceNode, destNode.
     */
    public static WamPlugin load(APROptions apr, File f, int duplicates) {
        GraphlikePlugin p = new LightweightGraphPlugin(apr, f.getName());
        ParsedFile parsed = new ParsedFile(f);
        BloomFilter<String> lines = null;
        if (duplicates > 0) { lines = new BloomFilter<String>(1e-5, duplicates); }
        boolean exceeds = false;
        for (String line : parsed) {
            String[] parts = line.split("\t");
            if (parts.length < 3) { parsed.parseError("expected 3 tab-delimited fields; got " + parts.length); }
            if (duplicates > 0) {
                if (lines.contains(line)) {
                    log.warn("Skipping duplicate fact at " + f.getName() + ":" + parsed.getAbsoluteLineNumber() + ": " +
                                     "" + line);
                    continue;
                } else { lines.add(line); }

                if (!exceeds & parsed.getLineNumber() > duplicates) {
                    exceeds = true;
                    log.warn("Number of graph edges exceeds " + duplicates + "; duplicate detection may encounter " +
                                     "false positives. We should add a command line option to fix this.");
                }
            }
            if (parts.length == 3) {
                p.addEdge(parts[0].trim(), parts[1].trim(), parts[2].trim());
            } else if (parts.length == 4) {
                p.addEdge(parts[0].trim(), parts[1].trim(), parts[2].trim(), Double.parseDouble(parts[3].trim()));
            }
        }
        return p;
    }

    @Override
    protected void indexAdd(String label, String src, String dst) {
        Dictionary.safePut(graph, label, src, dst, DEFAULT_DSTWEIGHT);
    }

    @Override
    protected void indexAdd(String label, String src, String dst, double weight) {
        Dictionary.safePut(graph, label, src, dst, weight);
    }

    @Override
    protected boolean indexContains(String label) {
        return graph.containsKey(label);
    }

    @Override
    protected Collection<String> indexGet(String label) {
        if (!graph.containsKey(label)) { return DEFAULT_SRCLIST; }
        return graph.get(label).keySet();
    }

    @Override
    protected TObjectDoubleMap<String> indexGet(String label, String src) {
        return Dictionary.safeGetGet(graph, label, src, DEFAULT_DSTLIST);
    }

    @Override
    protected Map<Feature, Double> getFD() {
        return this.fd;
    }

    @Override
    public String about() {
        return this.getClass().getSimpleName() + ":" + this.name;
    }
}
