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
     * Constant that represents a true logic value
     */
    public static final String TRUE_VALUE = "true";

    /**
     * Constant that represents a false logic value
     */
    public static final String FALSE_VALUE = "false";

    /**
     * {@link Atom} that represents the true logic value
     */
    public static final Atom TRUE_ATOM = new Atom(TRUE_VALUE);

    /**
     * {@link Atom} that represents the false logic value
     */
    public static final Atom FALSE_ATOM = new Atom(FALSE_VALUE);

    protected final String name;
    protected final List<Term> terms;

    /**
     * Constructs a new {@link Atom} from coping the references from another one
     *
     * @param atom the other {@link Atom}
     */
    public Atom(Atom atom) {
        this.name = atom.name;
        this.terms = atom.terms;
    }

    /**
     * Constructs a new {@link Atom}
     *
     * @param name  the {@link Atom}'s predicate name
     * @param terms the {@link Atom}'s {@link Term}s
     */
    public Atom(String name, List<Term> terms) {
        this.name = name;
        this.terms = terms;
    }

    /**
     * Constructs a proposition form of an {@link Atom}
     *
     * @param name the proposition name
     */
    public Atom(String name) {
        this.name = name;
        this.terms = null;
    }

    /**
     * Gets the {@link Atom}'s predicate name
     *
     * @return the name
     */
    public String getName() {
        return name;
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
        int result = name.hashCode();
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

        return name.equals(atom.name) && (terms != null ? terms.equals(atom.terms) : atom.terms == null);
    }

    @Override
    public String toString() {
        return formatAtomToString(this);
    }

}
