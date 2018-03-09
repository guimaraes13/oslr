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

package edu.cmu.ml.proppr.prove.wam.plugins.builtin;

import edu.cmu.ml.proppr.prove.wam.Feature;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.Dictionary;

import java.util.HashMap;
import java.util.Map;

/**
 * """Used for collections of simple built-in plugins."""
 * <p>
 * def __init__(self):
 * self.registery = {}
 * self.helpText = {}
 * self.fd = {'builtin':1}
 * def register(self,jumpTo,fun,help='no help available'):
 * self.registery[jumpTo] = fun
 * self.helpText[jumpTo] = help
 * def claim(self,jumpTo):
 * return (jumpTo in self.registery)
 * def outlinks(self,state,wamInterp,computeFeatures=True):
 * assert False,'abstract method called'
 *
 * @author William Cohen <wcohen@cs.cmu.edu>
 * @author Kathryn Mazaitis <krivard@cs.cmu.edu>
 **/
public abstract class PluginCollection extends WamPlugin {

    protected Map<String, PluginFunction> registry;
    protected Map<Feature, Double> fd;
    // TODO: helpText

    public PluginCollection(APROptions apr) {
        super(apr);
        this.fd = new HashMap<Feature, Double>();
        this.fd.put(new Feature("builtin"), 1.0);
    }

    @Override
    public String about() {
        return Dictionary.buildString(registry.keySet(), new StringBuilder(), ", ").toString();
    }

    @Override
    public boolean _claim(String jumpto) {
        return registry.containsKey(jumpto);
    }

    public void register(String jumpTo, PluginFunction fun) {
        if (registry == null) { registry = new HashMap<String, PluginFunction>(); }
        registry.put(jumpTo, fun);
    }

}
