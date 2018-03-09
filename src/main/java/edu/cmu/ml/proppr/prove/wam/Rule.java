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

package edu.cmu.ml.proppr.prove.wam;

import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.SimpleSymbolTable;
import edu.cmu.ml.proppr.util.SymbolTable;

import java.util.Arrays;

/**
 * A prolog rule.  The lhs is a goal, the rhs a list of goals, so the
 * rule's format is "lhs :- rhs."  The features for a rule are, in
 * general, of the form "features : findall", where 'findall' and
 * 'features' are lists of goals.  Features are produced as follows:
 * after binding the head of the rule, you find all solutions to the
 * 'findall' part (the "generator"), and for each solution, create a
 * feature corresponding to a bound version of each goal g in
 * 'features'.
 *
 * @author "William Cohen <wcohen@cs.cmu.edu>"
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 */
public class Rule {

    protected Goal lhs;
    protected Goal[] body;
    protected Goal[] features;
    protected Goal[] findall;
    protected int nvars;

    //	protected List<Argument> variableList;
    public Rule(Goal lhs, Goal[] rhs, Goal[] features, Goal[] findall) {
        this.lhs = lhs;
        this.body = rhs;
        this.features = features;
        this.findall = findall;
    }

    public SymbolTable<Argument> variabilize() {
        return variabilize(new SimpleSymbolTable<Argument>());
    }

    /**
     * Convert the variables to integer indices, -1,-2, ... and save their
     * original names in "variableList", and the number of distinct
     * variables in 'nvars'.
     */
    public SymbolTable<Argument> variabilize(SimpleSymbolTable<Argument> varTab) {
        if (this.nvars > 0) { throw new IllegalStateException("Rule already variabilized! " + this); }
        int before = varTab.getSymbolList().size();
        if (this.lhs != null) {
            this.lhs = convertGoal(this.lhs, varTab);
        }
        convertGoals(this.body, varTab);
        convertGoals(this.features, varTab);
        convertGoals(this.findall, varTab);
//		this.variableList = varTab.getSymbolList();
//		this.nvars = this.variableList.size();
        this.nvars = varTab.size() - before;
        return varTab;
    }

    public static Goal convertGoal(Goal g, SymbolTable<Argument> varTab) {
        return new Goal(g.getFunctor(), convertArgs(g.getArgs(), varTab));
    }

    public static void convertGoals(Goal[] goals, SymbolTable<Argument> varTab) {
        for (int i = 0; i < goals.length; i++) {
            goals[i] = convertGoal(goals[i], varTab);
        }
    }

    public static Argument[] convertArgs(Argument[] args, SymbolTable<Argument> varTab) {
        Argument[] ret = new Argument[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i].isVariableAtom()) { ret[i] = new VariableArgument(-varTab.getId(args[i])); } else {
                ret[i] = args[i];
            }
        }
        return ret;
    }

    public Goal getLhs() {
        return lhs;
    }

    public Goal[] getRhs() {
        return body;
    }

    public Goal[] getFeatures() {
        return features;
    }

    public Goal[] getFindall() {
        return findall;
    }

    public int getNvars() {
        return nvars;
    }

    @Override
    public int hashCode() {
        int result = lhs != null ? lhs.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(body);
        result = 31 * result + Arrays.hashCode(features);
        result = 31 * result + Arrays.hashCode(findall);
        result = 31 * result + nvars;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Rule)) { return false; }

        Rule rule = (Rule) o;

        if (nvars != rule.nvars) { return false; }
        if (lhs != null ? !lhs.equals(rule.lhs) : rule.lhs != null) { return false; }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(body, rule.body)) { return false; }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(features, rule.features)) { return false; }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(findall, rule.findall);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.lhs != null) { sb.append(this.lhs).append(" :- "); }
        Dictionary.buildString(this.body, sb, ",");
        if (this.features.length > 0) {
            sb.append(" {");
            Dictionary.buildString(features, sb, ",");
            if (this.findall.length > 0) {
                sb.append(" : ");
                Dictionary.buildString(findall, sb, ",");
            }
            sb.append("}");
        }
//		if (this.nvars>0) {
//			sb.append("  #v:[");
////			Dictionary.buildString(this.variableList, sb, ",");
//			sb.append("?");
//			sb.append("]");
//		}
        sb.append(".");
        return sb.toString();
    }

}
