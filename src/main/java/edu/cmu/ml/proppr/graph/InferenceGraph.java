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

package edu.cmu.ml.proppr.graph;

import edu.cmu.ml.proppr.prove.wam.Outlink;
import edu.cmu.ml.proppr.prove.wam.State;

import java.util.List;

/**
 * Template for a weighted graph.
 *
 * @author krivard
 */
public interface InferenceGraph {

    public abstract State getState(int id);

    public abstract int nodeSize();

    public abstract int edgeSize();

    /**
     * Serialization format: tab-delimited fields
     * 1: node count
     * 2: edge count
     * 3: featurename1:featurename2:featurename3:...:featurenameN
     * 4..N: srcId->dstId:fId_1,fId_2,...,fId_k
     * <p>
     * All IDs are 1-indexed.
     *
     * @return
     */
    public abstract String serialize();

    public abstract String serialize(boolean includeFeatureIndex);

    /**
     * only used for unit tests
     */
    public abstract void setOutlinks(int id, List<Outlink> outlinks);

    public abstract int getId(State s);

}
