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
import br.ufrj.cos.logic.Predicate;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.util.LanguageUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents an atom example.
 * <p>
 * Created on 17/04/17.
 *
 * @author Victor Guimarães
 */
public class AtomExample extends Atom implements Example {

    protected final boolean positive;
    protected final Atom atom;

    /**
     * Constructs a positive {@link AtomExample}
     *
     * @param predicate the {@link Atom}'s predicate
     * @param terms     the {@link Atom}'s {@link Term}s
     */
    @SuppressWarnings("SameParameterValue")
    public AtomExample(Predicate predicate, List<Term> terms) {
        this(predicate, terms, true);
    }

    /**
     * Constructs an {@link AtomExample}
     *
     * @param predicate the {@link Atom}'s predicate
     * @param terms     the {@link Atom}'s {@link Term}s
     * @param positive  the value of the example, true for positive; false for negative
     */
    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public AtomExample(Predicate predicate, List<Term> terms, boolean positive) {
        super(predicate, terms);
        this.positive = positive;
        this.atom = new Atom(this);
    }

    /**
     * Constructs a positive proposition like {@link AtomExample}
     *
     * @param predicate the proposition predicate
     */
    public AtomExample(Predicate predicate) {
        this(predicate, true);
    }

    /**
     * Constructs a proposition like {@link AtomExample}
     *
     * @param predicate the {@link Atom}'s predicate
     * @param positive  the value of the example, true for positive; false for negative
     */
    @SuppressWarnings("SameParameterValue")
    public AtomExample(Predicate predicate, boolean positive) {
        this(predicate, null, positive);
    }

    /**
     * Constructs an {@link AtomExample} from another {@link Atom}
     *
     * @param atom     the atom
     * @param positive the value of the example, true for positive; false for negative
     */
    public AtomExample(Atom atom, boolean positive) {
        this(atom.getPredicate(), atom.getTerms(), positive);
    }

    /**
     * Gets the {@link Atom} from the {@link AtomExample}
     *
     * @return the {@link Atom}
     */
    @Override
    public Atom getAtom() {
        return atom;
    }

    @Override
    public Collection<Term> getPositiveTerms() {
        return isPositive() ? getTerms() : null;
    }

    @Override
    public boolean isPositive() {
        return positive;
    }

    @Override
    public Atom getGoalQuery() {
        return this;
    }

    @Override
    public Collection<? extends AtomExample> getGroundedQuery() {
        List<AtomExample> atoms = new ArrayList<>(1);
        if (isGrounded()) {
            atoms.add(this);
        }
        return atoms;
    }

    @Override
    public boolean isFact() {
        return false;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        if (positive) {
            result = 31 * result + 1;
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AtomExample)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        AtomExample atomExample = (AtomExample) o;

        return positive == atomExample.positive;
    }

    @Override
    public String toString() {
        return LanguageUtils.formatExampleToProbLogString(this);
    }

}
