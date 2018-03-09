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

package br.ufrj.cos.knowledge.example;

import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.logic.Variable;
import br.ufrj.cos.util.LanguageUtils;

import java.util.*;

/**
 * Represents a ProPPR example. It is the full line from the ProPPR input, i.e. the goal and the list of positive and
 * negative grounds.
 * <p>
 * Created on 17/04/17.
 *
 * @author Victor Guimarães
 */
public class ProPprExample implements Example {

    protected final Atom goal;
    protected final List<AtomExample> atomExamples;

    /**
     * Constructs the ProPPR iterator
     *
     * @param goal         the goal
     * @param atomExamples the positive and negative grounded atoms
     */
    public ProPprExample(Atom goal, List<AtomExample> atomExamples) {
        this.goal = goal;
        this.atomExamples = atomExamples;
    }

    /**
     * Looks for positive iterator into the {@link AtomExample}s.
     *
     * @param atomExamples the {@link AtomExample}s
     * @return {@code true} if at least one {@link AtomExample} is positive, {@code false} otherwise
     */
    protected static boolean hasPositiveExamples(List<AtomExample> atomExamples) {
        for (AtomExample atomExample : atomExamples) {
            if (atomExample.isPositive()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Atom getAtom() {
        return goal;
    }

    @Override
    public Collection<Term> getPositiveTerms() {
        Collection<Term> positiveTerms = new HashSet<>();
        appendConstantFromAtom(goal, positiveTerms);
        for (AtomExample atom : atomExamples) {
            if (atom.isPositive()) {
                appendConstantFromAtom(atom, positiveTerms);
            }
        }

        return positiveTerms;
    }

    @Override
    public boolean isPositive() {
        return hasPositiveExamples(atomExamples);
    }

    @Override
    public Map<Term, Term> getVariableMap() {
        Map<Term, Term> variableMap = new HashMap<>();

        Term term;
        for (int i = 0; i < goal.getArity(); i++) {
            term = goal.getTerms().get(i);
            if (term instanceof Variable) {
                getTermOnIndex(i, (Variable) term, variableMap);
            }
        }
        clearConstantGoalFromVariableMap(variableMap);
        return variableMap;
    }

    @Override
    public Atom getGoalQuery() {
        return goal;
    }

    @Override
    public Collection<? extends AtomExample> getGroundedQuery() {
        return atomExamples;
    }

    /**
     * Puts the {@link Term}s at index i from all the {@link #atomExamples} into the variableMap as keys with the
     * variable parameter as value
     *
     * @param i           the index of the {@link Term}
     * @param variable    the value {@link Variable}
     * @param variableMap the {@link Map}
     */
    protected void getTermOnIndex(int i, Variable variable, Map<Term, Term> variableMap) {
        for (AtomExample atomExample : atomExamples) {
            variableMap.put(atomExample.getTerms().get(i), variable);
        }
    }

    /**
     * Cleans the constants from the goal of the variable map. It is necessary to allow the constant of the goal to
     * be mapped to a different variable. This is specially useful when the predicate represents a symmetric relation.
     *
     * @param variableMap the variable to clean
     */
    protected void clearConstantGoalFromVariableMap(Map<Term, Term> variableMap) {
        for (Term term : goal.getTerms()) {
            if (term.isConstant()) {
                variableMap.remove(term);
            }
        }
    }

    /**
     * Appends the constants founded on the given {@link Atom} to the {@link Collection}
     *
     * @param atom      the given {@link Atom}
     * @param constants the {@link Collection}
     */
    protected static void appendConstantFromAtom(Atom atom, Collection<Term> constants) {
        for (Term term : atom.getTerms()) {
            if (term.isConstant()) {
                constants.add(term);
            }
        }
    }

    /**
     * Gets the goal of the example
     *
     * @return the goal
     */
    public Atom getGoal() {
        return goal;
    }

    /**
     * Gets the grounded {@link Atom}s of the example
     *
     * @return the grounded {@link Atom}s
     */
    public List<AtomExample> getAtomExamples() {
        return atomExamples;
    }

    @Override
    public int hashCode() {
        int result = goal.hashCode();
        result = 31 * result + atomExamples.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProPprExample)) {
            return false;
        }

        ProPprExample that = (ProPprExample) o;

        return goal.equals(that.goal) && atomExamples.equals(that.atomExamples);
    }

    @Override
    public String toString() {
        return LanguageUtils.formatExampleToProPprString(this);
    }

}
