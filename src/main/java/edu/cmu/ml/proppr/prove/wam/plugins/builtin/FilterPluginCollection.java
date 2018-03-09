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

package edu.cmu.ml.proppr.prove.wam.plugins.builtin;

import edu.cmu.ml.proppr.prove.wam.LogicProgramException;
import edu.cmu.ml.proppr.prove.wam.Outlink;
import edu.cmu.ml.proppr.prove.wam.State;
import edu.cmu.ml.proppr.prove.wam.WamInterpreter;
import edu.cmu.ml.proppr.util.APROptions;

import java.util.Collections;
import java.util.List;

/**
 * """Used for built-ins that may or may not succeed, like 'neq'."""
 * def __init__(self):
 * PlugInCollection.__init__(self)
 * def implementsDegree(self,state):
 * return False
 * def outlinks(self,state,wamInterp,computeFeatures=True):
 * jumpTo = state.jumpTo
 * filterFun = self.registery[jumpTo]
 * if not filterFun(wamInterp):
 * wamInterp.restoreState(state)
 * wamInterp.returnp()
 * wamInterp.state.failed = True
 * else:
 * wamInterp.restoreState(state)
 * wamInterp.returnp()
 * wamInterp.executeWithoutBranching()
 * if computeFeatures:
 * yield (self.fd,wamInterp.saveState())
 * else:
 * yield wamInterp.saveState()
 *
 * @author "William Cohen <wcohen@cs.cmu.edu>"
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 */
public class FilterPluginCollection extends PluginCollection {

    public FilterPluginCollection(APROptions apr) {
        super(apr);
    }

    @Override
    public List<Outlink> outlinks(State state, WamInterpreter wamInterp,
                                  boolean computeFeatures) throws LogicProgramException {
        String jumpTo = state.getJumpTo();
        PluginFunction fun = this.registry.get(jumpTo);
        wamInterp.restoreState(state);
        wamInterp.returnp();
        if (!fun.run(wamInterp)) {
            wamInterp.getState().setFailed(true);
        } else {
            wamInterp.executeWithoutBranching();
        }
        if (computeFeatures) {
            return Collections.singletonList(new Outlink(this.fd, wamInterp.saveState()));
        }
        return Collections.singletonList(new Outlink(Outlink.EMPTY_FD, wamInterp.saveState()));
    }

}
