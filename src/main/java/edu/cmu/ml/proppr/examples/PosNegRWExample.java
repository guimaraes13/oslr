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

package edu.cmu.ml.proppr.examples;

import edu.cmu.ml.proppr.graph.LearningGraph;
import edu.cmu.ml.proppr.util.Dictionary;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.procedure.TIntProcedure;

/**
 * A supervised random walk example which specifies a list of positive examples and a list of negative examples.
 *
 * @author krivard
 */
public class PosNegRWExample extends RWExample {

    public double[] p;
    public TIntDoubleMap[] dp;
    protected int[] posList;
    protected int[] negList;

//	// wwc add, kmm port
//	public PosNegRWExample<F> posOnly() {
//		PosNegRWExample<F> result = new PosNegRWExample<T>(this.graph,this.queryVec);
//		result.posList = this.posList;
//		//System.out.println("posOnly() for "+this+" is "+result);
//		return result;
//	}
//	// wwc add, kmm port
//	public PosNegRWExample<F> negOnly() {
//		PosNegRWExample<F> result = new PosNegRWExample<T>(this.graph,this.queryVec);
//		result.negList = this.negList;
//		//System.out.println("negOnly() for "+this+" is "+result);
//		return result;
//	}

    private PosNegRWExample(String name, LearningGraph graph, TIntDoubleMap queryVec) {
        super(name, graph, queryVec);
        this.allocate();
    }

    protected void allocate() {
        this.p = new double[graph.node_hi];
        this.dp = new TIntDoubleMap[graph.node_hi];
    }

    public PosNegRWExample(LearningGraph graph, TIntDoubleMap queryVec, int[] pos, int[] neg) {
        this("n/a", graph, queryVec, pos, neg);
    }

    public PosNegRWExample(String name, LearningGraph graph, TIntDoubleMap queryVec,
                           int[] pos, int[] neg) {
        super(name, graph, queryVec);
        this.posList = pos;
        this.negList = neg;
        this.allocate();
    }

//	public PosNegRWExample(InferenceLearningGraph<T> g, Map<T, Double> queryVec,
//			Iterable<T> pos, Iterable<T> neg) {
//		super(g,queryVec);
//		for (T p : pos) this.posList.add(p);
//		for (T n : neg) this.negList.add(n);
//	}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PosNegRWExample[");
        sb.append(this.name).append(" ");
        sb.append(graph.nodeSize()).append("/").append(graph.edgeSize()).append("; [");
        queryVec.forEachKey(new TIntProcedure() {
            @Override
            public boolean execute(int q) {
                sb.append(q).append(",");
                return true;
            }
        });
        if (sb.length() > 0) { sb.deleteCharAt(sb.length() - 1); }
        sb.append("] -> +[");
        if (posList.length > 0) { Dictionary.buildString(posList, sb, ",", true); }
        sb.append("]; -[");
        if (negList.length > 0) { Dictionary.buildString(negList, sb, ",", true); }
        sb.append("]]");
        return sb.toString();
    }

    @Override
    public int length() {
        return posList.length + negList.length;
    }

    public int[] getPosList() {
        return posList;
    }

    public int[] getNegList() {
        return negList;
    }

    public String serialize() {
        StringBuilder serialized = new StringBuilder("?")
                .append("\t");
        Dictionary.buildString(this.queryVec.keys(), serialized, ","); //query
        serialized.append("\t");
        Dictionary.buildString(this.posList, serialized, ","); // pos
        serialized.append("\t");
        Dictionary.buildString(this.negList, serialized, ","); //neg
        serialized.append("\t");
        this.graph.serialize(serialized);
        return serialized.toString();
    }
}
