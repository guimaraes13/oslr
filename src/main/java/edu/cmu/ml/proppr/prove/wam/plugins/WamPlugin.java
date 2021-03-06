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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract extension to a WAM program.
 * <p>
 * When making new plugins, be sure to use WamPlugin.pluginFeature() to generate the feature for your plugin, so that
 * we can monitor the minalpha projection assumption correctly.
 *
 * @author "William Cohen <wcohen@cs.cmu.edu>"
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 */
public abstract class WamPlugin {

    public static final String FACTS_FUNCTOR = "db(";
    public static final String WEIGHTED_SUFFIX = "#";
    protected static final double DEFAULT_DSTWEIGHT = 1.0;
    protected APROptions apr;

    public WamPlugin(APROptions apr) {
        this.apr = apr;
    }

    public static Feature pluginFeature(WamPlugin plugin, String identifier) {
        return new Feature("db(" + plugin.getClass().getSimpleName() + "," + identifier + ")");
    }

    public boolean claim(String rawJumpto) {
        if (rawJumpto.indexOf(WamInterpreter.WEIGHTED_JUMPTO_DELIMITER) < 0) { return _claim(rawJumpto); }
        return _claim(unweightedJumpto(rawJumpto));
    }

    /**
     * Return True if this plugin should be called to implement this predicate/arity pair.
     *
     * @param jumpto
     * @return
     */
    public abstract boolean _claim(String jumpto);

    /**
     * Convert from a string like "foo#/3" to "foo/2"
     **/
    public static String unweightedJumpto(String jumpto) {
        int n = jumpto.length();
        String[] parts = jumpto.split(WamInterpreter.WEIGHTED_JUMPTO_DELIMITER, 2);
        // String stem = jumpto.substring(0,n-WEIGHTED_GRAPH_SUFFIX_PLUS_ARITY.length());
        // return stem + GRAPH_ARITY;
        return parts[0] + WamInterpreter.JUMPTO_DELIMITER + (Integer.parseInt(parts[1]) - 1);
    }

    /**
     * Yield a list of successor states, not including the restart state.
     *
     * @param state
     * @param wamInterp
     * @param computeFeatures
     * @return
     * @throws LogicProgramException
     */
    public abstract List<Outlink> outlinks(State state, WamInterpreter wamInterp,
                                           boolean computeFeatures) throws LogicProgramException;

    /**
     * True if the subclass implements a degree() function that's quicker than computing the outlinks.
     *
     * @return
     */
    public boolean implementsDegree() {
        return false;
    }

    /**
     * Return the number of outlinks, or else throw an error if implementsDegree is false.
     *
     * @param jumpto
     * @param state
     * @param wamInterp
     * @return
     */
    public int degree(String jumpto, State state, WamInterpreter wamInterp) {
        throw new UnsupportedOperationException("degree method not implemented");
    }

    @Override
    public String toString() {
        return this.about();
    }

    public abstract String about();

    protected Map<Feature, Double> scaleFD(Map<Feature, Double> fd, double wt) {
        if (wt == 1.0) { return fd; }
        Map<Feature, Double> ret = new HashMap<Feature, Double>();
        ret.putAll(fd);
        for (Map.Entry<Feature, Double> val : ret.entrySet()) {
            val.setValue(val.getValue() * wt);
        }
        return ret;
    }
}
