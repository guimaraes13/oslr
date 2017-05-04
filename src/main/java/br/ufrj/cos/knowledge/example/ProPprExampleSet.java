/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2017 Victor Guimarães
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
public class ProPprExampleSet implements Example {

    protected final Atom goal;
    protected final List<AtomExample> atomExamples;

    protected final boolean hasPositivePart;

    /**
     * Constructs the ProPPR examples
     *
     * @param goal         the goal
     * @param atomExamples the positive and negative grounded atoms
     */
    public ProPprExampleSet(Atom goal, List<AtomExample> atomExamples) {
        this.goal = goal;
        this.atomExamples = atomExamples;
        this.hasPositivePart = hasPositiveExamples(atomExamples);
    }

    /**
     * Looks for positive examples into the {@link AtomExample}s.
     *
     * @param atomExamples the {@link AtomExample}s
     * @return {@code true} if at least one {@link AtomExample} is positive, {@code false} otherwise
     */
    protected boolean hasPositiveExamples(List<AtomExample> atomExamples) {
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
        return hasPositivePart;
    }

    @Override
    public Map<Term, Variable> getVariableMap() {
        Map<Term, Variable> variableMap = new HashMap<>();

        Term term;
        for (int i = 0; i < goal.getTerms().size(); i++) {
            term = goal.getTerms().get(i);
            if (term instanceof Variable) {
                getTermOnIndex(i, (Variable) term, variableMap);
            }
        }

        return variableMap;
    }

    /**
     * Puts the {@link Term}s at index i from the all the {@link #atomExamples} into the variableMap as keys with the
     * variable parameter as value
     *
     * @param i           the index of the {@link Term}
     * @param variable    the value {@link Variable}
     * @param variableMap the {@link Map}
     */
    protected void getTermOnIndex(int i, Variable variable, Map<Term, Variable> variableMap) {
        for (AtomExample atomExample : atomExamples) {
            variableMap.put(atomExample.getTerms().get(i), variable);
        }
    }

    /**
     * Appends the constants founded on the given {@link Atom} to the {@link Collection}
     *
     * @param atom      the given {@link Atom}
     * @param constants the {@link Collection}
     */
    protected void appendConstantFromAtom(Atom atom, Collection<Term> constants) {
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
        if (!(o instanceof ProPprExampleSet)) {
            return false;
        }

        ProPprExampleSet that = (ProPprExampleSet) o;

        return goal.equals(that.goal) && atomExamples.equals(that.atomExamples);
    }

    @Override
    public String toString() {
        return LanguageUtils.formatExampleToProPprString(this);
    }

}
