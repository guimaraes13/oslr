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

package br.ufrj.cos.logic;

import java.util.List;

import static br.ufrj.cos.util.LanguageUtils.formatAtomToString;

/**
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class Atom extends Clause {

    /**
     * The true proposition.
     */
    public static final String TRUE_PROPOSITION = "true";
    /**
     * The false proposition.
     */
    public static final String FALSE_PROPOSITION = "false";
    /**
     * Constant that represents a true logic value
     */
    public static final Predicate TRUE_VALUE = new Predicate(TRUE_PROPOSITION);
    /**
     * Constant that represents a false logic value
     */
    public static final Predicate FALSE_VALUE = new Predicate(FALSE_PROPOSITION);
    /**
     * {@link Atom} that represents the true logic value
     */
    @SuppressWarnings("unused")
    public static final Atom TRUE_ATOM = new Atom(TRUE_VALUE);

    /**
     * {@link Atom} that represents the false logic value
     */
    @SuppressWarnings("unused")
    public static final Atom FALSE_ATOM = new Atom(FALSE_VALUE);

    protected final Predicate predicate;
    protected final List<Term> terms;

    /**
     * Constructs a new {@link Atom} from coping the references from another one
     *
     * @param atom the other {@link Atom}
     */
    public Atom(Atom atom) {
        this.predicate = atom.predicate;
        this.terms = atom.terms;
    }

    /**
     * Constructs a new {@link Atom}
     *
     * @param predicate the {@link Atom}'s predicate
     * @param terms     the {@link Atom}'s {@link Term}s
     */
    public Atom(Predicate predicate, List<Term> terms) {
        this.predicate = predicate;
        this.terms = terms;
    }

    /**
     * Constructs a proposition form of an {@link Atom}
     *
     * @param predicate the proposition
     */
    public Atom(Predicate predicate) {
        this.predicate = predicate;
        this.terms = null;
    }

    /**
     * Constructs a proposition form of an {@link Atom}
     *
     * @param predicate the proposition
     */
    public Atom(String predicate) {
        this(new Predicate(predicate));
    }

    /**
     * Constructs a proposition form of an {@link Atom}
     *
     * @param predicate the proposition
     * @param terms     the {@link Atom}'s {@link Term}s
     */
    public Atom(String predicate, List<Term> terms) {
        this(new Predicate(predicate, terms.size()), terms);
    }

    /**
     * Gets the {@link Atom}'s predicate name
     *
     * @return the name
     */
    public String getName() {
        return predicate.getName();
    }

    /**
     * Gets the {@link Atom}'s {@link Term}s, or null if it is a proposition
     *
     * @return the {@link Term}s, or null if it is a proposition
     */
    public List<Term> getTerms() {
        return terms;
    }

    @Override
    public boolean isGrounded() {
        if (terms != null) {
            for (Term term : terms) {
                if (!term.isConstant()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isFact() {
        return true;
    }

    @Override
    public int hashCode() {
        int result = predicate.hashCode();
        result = 31 * result + (terms != null ? terms.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Atom)) {
            return false;
        }

        Atom atom = (Atom) o;

        return predicate.equals(atom.predicate) && (terms != null ? terms.equals(atom.terms) : atom.terms == null);
    }

    @Override
    public String toString() {
        return formatAtomToString(this);
    }

    /**
     * Gets the arity of the atom
     *
     * @return the arity
     */
    public int getArity() {
        return terms != null ? terms.size() : 0;
    }

    /**
     * Gets the predicate.
     *
     * @return the predicate
     */
    public Predicate getPredicate() {
        return predicate;
    }

}
