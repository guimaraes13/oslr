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

package edu.cmu.ml.proppr.learn.tools;

import edu.cmu.ml.proppr.util.math.ParamVector;

import java.util.ArrayList;

/**
 * A simple programmable procedure to determine if a feature is fixed
 * (i.e., the weight will not be changed in training) or not.  The
 * procedure is specified by a single string, which is a
 * colon-separated list of pairs SPEC[=DECISION] where SPEC is any
 * string (ending in * to specify a prefix) and DECISION is 'y' or 'n'
 * (default y).
 * <p>
 * To decide whether a particular feature S is of fixed weight, you go
 * through the rules, and take the DECISION associated with the first
 * spec that matches S.
 * <p>
 * <p>
 * For example:
 * <p>
 * "fixedWeight=y:*=n" means tune all features that aren't "fixedWeight".
 * <p>
 * "f(*=n:*=y" means tune only features that start with "f("
 * <p>
 * If no spec matches then the decision return is false.
 **/
public class FixedWeightRules {

    private final ArrayList<DecisionRule> ruleList;

    public FixedWeightRules(String[] init) {
        this();
        for (String r : init) {
            String[] opts = r.split("=", 2);
            ruleList.add(new DecisionRule(opts[0], opts.length == 1 || "y".equals(opts[1])));
        }
    }

    public FixedWeightRules() {
        ruleList = new ArrayList<DecisionRule>();
    }

    public boolean isFixed(String feature) {
        for (DecisionRule r : ruleList) {
            if (r.claim(feature)) { return r.fixed; }
        }
        return false;
    }

    public void addExact(String feature) {
        ruleList.add(new DecisionRule(feature, true));
    }

    public void initializeFixed(ParamVector<String, ?> params, String feature) {
        /*
		 * Future work: Could add syntax to fix features at arbitrary values here.
		 */
        params.put(feature, 1.0);
    }

    class DecisionRule {

        public String spec;
        public boolean fixed;
        public boolean prefix;

        public DecisionRule(String s, boolean fix) {
            prefix = s.endsWith("*");
            spec = prefix ? s.substring(0, s.length() - 1) : s;
            fixed = fix;
        }

        public boolean claim(String f) {
            return prefix ? f.startsWith(spec) : f.equals(spec);
        }

        @Override
        public String toString() {
            return "if feature" + (prefix ? " starts with " : " is ") + spec + " it is " + (fixed ? "" : "not") + " fixed";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (DecisionRule r : ruleList) {
            sb.append(r + "; ");
        }
        return sb.toString();
    }
}
