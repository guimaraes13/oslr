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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.cmu.ml.proppr.prove.wam.LogicProgramException;
import edu.cmu.ml.proppr.prove.wam.Outlink;
import edu.cmu.ml.proppr.prove.wam.State;
import edu.cmu.ml.proppr.prove.wam.WamInterpreter;
import edu.cmu.ml.proppr.util.APROptions;

/** 
 * Manage multiple .cfacts files, supporting splitting a functor across multiple files
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 *
 */
public class SplitFactsPlugin extends WamPlugin {
	protected List<FactsPlugin> plugins = new LinkedList<FactsPlugin>();
	public SplitFactsPlugin(APROptions apr) {
		super(apr);
	}

	@Override
	public String about() {
		StringBuilder sb = new StringBuilder("union(");
		for (FactsPlugin p : plugins) sb.append(p.about()).append(",");
		sb.setCharAt(sb.length()-1, ')');
		return sb.toString();
	}
	
	public void add(FactsPlugin p) {
		this.plugins.add(p);
	}

	@Override
	public boolean _claim(String jumpto) {
		for (FactsPlugin p : plugins)
			if (p._claim(jumpto)) return true;
		return false;
	}

	@Override
	public List<Outlink> outlinks(State state, WamInterpreter wamInterp,
			boolean computeFeatures) throws LogicProgramException {
		List<Outlink> ret = new ArrayList<Outlink>();
		for (FactsPlugin p : plugins) {
			if (p._claim(state.getJumpTo())) {
				wamInterp.restoreState(state);
				List<Outlink> outs = p.outlinks(state, wamInterp, computeFeatures);
				ret.addAll(outs);
			}
		}
		return ret;
	}
}
