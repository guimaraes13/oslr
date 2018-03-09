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

package edu.cmu.ml.proppr.learn.tools;

import edu.cmu.ml.proppr.examples.PosNegRWExample;
import edu.cmu.ml.proppr.graph.GraphFormatException;
import edu.cmu.ml.proppr.graph.LearningGraph;
import edu.cmu.ml.proppr.graph.LearningGraphBuilder;
import edu.cmu.ml.proppr.learn.SRW;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RWExampleParser {

    //public static final String MAJOR_DELIM="\t";
    public static final char MAJOR_DELIM = '\t';
    public static final char MINOR_DELIM = ',';
    private static final Logger log = LogManager.getLogger(RWExampleParser.class);

    public PosNegRWExample parse(String line, LearningGraphBuilder builder, SRW learner) throws GraphFormatException {
        //String[] parts = line.trim().split(MAJOR_DELIM,5);
        // first parse the query metadata
        String[] parts = new String[4];//LearningGraphBuilder.split(line,'\t',4);
        int last = 0, i = 0;
        for (int next = last; i < parts.length; last = next + 1, i++) {
            if (next == -1) {
                throw new GraphFormatException("Need 8 distinct tsv fields in the grounded example:" + line);
            }
            next = line.indexOf(MAJOR_DELIM, last);
            parts[i] = next < 0 ? line.substring(last) : line.substring(last, next);
        }

        TIntDoubleMap queryVec = new TIntDoubleHashMap();
        //for(String u : parts[1].split(MINOR_DELIM)) queryVec.put(Integer.parseInt(u), 1.0);
        for (int u : parseNodes(parts[1])) { queryVec.put(u, 1.0); }

        int[] posList, negList;
        if (parts[2].length() > 0) {
            posList = parseNodes(parts[2]); //stringToInt(parts[2].split(MINOR_DELIM));
        } else { posList = new int[0]; }
        if (parts[3].length() > 0) {
            negList = parseNodes(parts[3]);//stringToInt(parts[3].split(MINOR_DELIM));
        } else { negList = new int[0]; }

        LearningGraph g = builder.deserialize(line.substring(last));
        return learner.makeExample(parts[0], g, queryVec, posList, negList);
    }

    private int[] parseNodes(String string) {
        String[] nodeStrings = LearningGraphBuilder.split(string, MINOR_DELIM);
        int[] nodes = new int[nodeStrings.length];
        for (int i = 0; i < nodeStrings.length; i++) {
            nodes[i] = Integer.parseInt(nodeStrings[i]);
        }
        return nodes;
    }
}
