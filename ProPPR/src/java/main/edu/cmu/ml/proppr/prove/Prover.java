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

package edu.cmu.ml.proppr.prove;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.prove.wam.Argument;
import edu.cmu.ml.proppr.prove.wam.CachingIdProofGraph;
import edu.cmu.ml.proppr.prove.wam.Feature;
import edu.cmu.ml.proppr.prove.wam.LogicProgramException;
import edu.cmu.ml.proppr.prove.wam.Outlink;
import edu.cmu.ml.proppr.prove.wam.ProofGraph;
import edu.cmu.ml.proppr.prove.wam.Query;
import edu.cmu.ml.proppr.prove.wam.State;
import edu.cmu.ml.proppr.prove.wam.StateProofGraph;
import edu.cmu.ml.proppr.prove.wam.VariableArgument;
import edu.cmu.ml.proppr.prove.wam.WamProgram;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.SimpleSymbolTable;
import edu.cmu.ml.proppr.util.StatusLogger;
import edu.cmu.ml.proppr.util.SymbolTable;

/**
 * abstract prover class - prove a goal, constructing a proof graph
 * @author "William Cohen <wcohen@cs.cmu.edu>"
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 *
 */
public abstract class Prover<P extends ProofGraph> {
	// necessary to avoid rounding errors when rescaling reset weight
	protected static final double ALPHA_BUFFER = 1e-16;
	protected FeatureDictWeighter weighter;
	public APROptions apr;
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
	
	public abstract Class<P> getProofGraphClass();
	
//	public Map<State, Double> prove(P pg) throws LogicProgramException {
//		return prove(pg, null);
//	}
	/** Return unfiltered distribution of state associated with proving the start state. 
	 * @throws LogicProgramException */
	public abstract Map<State,Double> prove(P pg, StatusLogger status) throws LogicProgramException;
	
	/** Return a threadsafe copy of the prover.
	 */
	public abstract Prover<P> copy();
	
	public void setWeighter(FeatureDictWeighter w) {
		this.weighter = w;
	}
	public Map<Query,Double> solvedQueries(P pg, StatusLogger status) throws LogicProgramException {
		Map<State,Double> ans = prove(pg, status);
		Map<Query,Double> solved = new HashMap<Query,Double>();
		double normalizer = 0;
		for (Map.Entry<State,Double> e : ans.entrySet()) {
			if (e.getKey().isCompleted()) {
				normalizer += e.getValue();
				solved.put(pg.fill(e.getKey()),e.getValue());
			}
		}
		for (Map.Entry<Query,Double> e : solved.entrySet()) { e.setValue(e.getValue()/normalizer); }
		return solved;
	}
	public Map<String,Double> solutions(P pg, StatusLogger status) throws LogicProgramException {
		Map<State,Double> proveOutput = this.prove(pg,status);
		Map<String,Double> filtered = new HashMap<String,Double>();
		double normalizer = 0;
		for (Map.Entry<State, Double> e : proveOutput.entrySet()) {
			if (e.getKey().isCompleted()) {
				normalizer += e.getValue();
				Map<Argument,String> d = pg.asDict(e.getKey());
				String dstr = "";
				if (!d.isEmpty()) dstr = Dictionary.buildString(d,new StringBuilder()," ").substring(1);
				filtered.put(dstr, Dictionary.safeGet(filtered,dstr)+e.getValue());
			}
		}
		for (Map.Entry<String,Double> e : filtered.entrySet()) {
			e.setValue(e.getValue()/normalizer);
		}
		return filtered;
	}
	public FeatureDictWeighter getWeighter() {
		return this.weighter;
	}
	
	/* **************** factory ****************** */
	public P makeProofGraph(InferenceExample ex, APROptions apr, WamProgram program, WamPlugin ... plugins) throws LogicProgramException {
		return makeProofGraph(ex, apr, new SimpleSymbolTable<Feature>(), program, plugins);
	}
	public P makeProofGraph(InferenceExample ex, APROptions apr, SymbolTable<Feature> featureTab, WamProgram program, WamPlugin ... plugins) throws LogicProgramException {
		Class<P> clazz = this.getProofGraphClass();
		try {
			Constructor<P> construct = clazz.getConstructor(InferenceExample.class, APROptions.class, SymbolTable.class, WamProgram.class, WamPlugin[].class);
			return construct.newInstance(new Object[] {ex, apr, featureTab, program, plugins});
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException ("Bad proof graph class specified by prover: needs constructor <? extends ProofGraph>(InferenceExample, APROptions, SymbolTable, WamProgram, WamPlugin[])",e);
		} catch (InstantiationException e) {
			throw new IllegalStateException("Trouble instantiating proof graph: ",e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException("Trouble instantiating proof graph: ",e);
		} catch (SecurityException e) {
			throw new IllegalStateException("Bizarre exception:",e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Bizarre exception:",e);
		}
	}
	/** Use for arbitrary command line prover configuration in subclasses **/
	public void configure(String param) {}
}
