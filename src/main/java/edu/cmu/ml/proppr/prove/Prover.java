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

import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.prove.wam.*;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * abstract prover class - prove a goal, constructing a proof graph
 *
 * @author "William Cohen <wcohen@cs.cmu.edu>"
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 */
public abstract class Prover<P extends ProofGraph> {

    // necessary to avoid rounding errors when rescaling reset weight
    protected static final double ALPHA_BUFFER = 1e-16;
    public APROptions apr;
    protected FeatureDictWeighter weighter;

    public Prover() {
        this(new APROptions());
    }

    public Prover(APROptions apr) {
        this(new UniformWeighter(), apr);
    }

    public Prover(FeatureDictWeighter w, APROptions apr) {
        this.weighter = w;
        this.apr = apr;
    }

    /**
     * Return a threadsafe copy of the prover.
     */
    public abstract Prover<P> copy();

//	public Map<State, Double> prove(P pg) throws LogicProgramException {
//		return prove(pg, null);
//	}

    public Map<Query, Double> solvedQueries(P pg, StatusLogger status) throws LogicProgramException {
        Map<State, Double> ans = prove(pg, status);
        Map<Query, Double> solved = new HashMap<Query, Double>();
        double normalizer = 0;
        for (Map.Entry<State, Double> e : ans.entrySet()) {
            if (e.getKey().isCompleted()) {
                normalizer += e.getValue();
                solved.put(pg.fill(e.getKey()), e.getValue());
            }
        }
        for (Map.Entry<Query, Double> e : solved.entrySet()) { e.setValue(e.getValue() / normalizer); }
        return solved;
    }

    /**
     * Return unfiltered distribution of state associated with proving the start state.
     *
     * @throws LogicProgramException
     */
    public abstract Map<State, Double> prove(P pg, StatusLogger status) throws LogicProgramException;

    public Map<String, Double> solutions(P pg, StatusLogger status) throws LogicProgramException {
        Map<State, Double> proveOutput = this.prove(pg, status);
        Map<String, Double> filtered = new HashMap<String, Double>();
        double normalizer = 0;
        for (Map.Entry<State, Double> e : proveOutput.entrySet()) {
            if (e.getKey().isCompleted()) {
                normalizer += e.getValue();
                Map<Argument, String> d = pg.asDict(e.getKey());
                String dstr = "";
                if (!d.isEmpty()) { dstr = Dictionary.buildString(d, new StringBuilder(), " ").substring(1); }
                filtered.put(dstr, Dictionary.safeGet(filtered, dstr) + e.getValue());
            }
        }
        for (Map.Entry<String, Double> e : filtered.entrySet()) {
            e.setValue(e.getValue() / normalizer);
        }
        return filtered;
    }

    public FeatureDictWeighter getWeighter() {
        return this.weighter;
    }

    public void setWeighter(FeatureDictWeighter w) {
        this.weighter = w;
    }

    /* **************** factory ****************** */
    public P makeProofGraph(InferenceExample ex, APROptions apr, WamProgram program,
                            WamPlugin... plugins) throws LogicProgramException {
        return makeProofGraph(ex, apr, new SimpleSymbolTable<Feature>(), program, plugins);
    }

    public P makeProofGraph(InferenceExample ex, APROptions apr, SymbolTable<Feature> featureTab, WamProgram program,
                            WamPlugin... plugins) throws LogicProgramException {
        Class<P> clazz = this.getProofGraphClass();
        try {
            Constructor<P> construct = clazz.getConstructor(InferenceExample.class, APROptions.class, SymbolTable
                    .class, WamProgram.class, WamPlugin[].class);
            return construct.newInstance(ex, apr, featureTab, program, plugins);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Bad proof graph class specified by prover: needs constructor <? " +
                                                       "extends ProofGraph>(InferenceExample, APROptions, " +
                                                       "SymbolTable, WamProgram, WamPlugin[])", e);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Trouble instantiating proof graph: ", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Trouble instantiating proof graph: ", e);
        } catch (SecurityException e) {
            throw new IllegalStateException("Bizarre exception:", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Bizarre exception:", e);
        }
    }

    public abstract Class<P> getProofGraphClass();

    /**
     * Use for arbitrary command line prover configuration in subclasses
     **/
    public void configure(String param) {
    }
}
