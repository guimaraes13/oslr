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

import br.ufrj.cos.util.LanguageUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A container for an {@link Atom} with improved {@link #equals(Object)} and {@link #hashCode()} to detect
 * equivalent {@link Atom}.
 * <p>
 * Created on 02/06/17.
 *
 * @author Victor Guimarães
 */
public class EquivalentAtom extends Atom {

    protected final Set<Term> fixedTerms;
    protected int hashCode;
    protected Map<Term, Term> substitutionMap;

    /**
     * Constructor with the {@link Atom}.
     *
     * @param atom       the {@link Atom}
     * @param fixedTerms {@link Term}s to be treated as constant (e.g. consolidated variables from the rule).
     */
    public EquivalentAtom(Atom atom, Set<Term> fixedTerms) {
        super(atom);
        this.fixedTerms = fixedTerms;
        computeHashCode();
    }

    /**
     * Computes the {@link Atom}'s hash code.
     */
    protected void computeHashCode() {
        hashCode = 1;
        hashCode = 31 * hashCode + getName().hashCode();
        List<Term> terms = getTerms();
        if (terms != null && !terms.isEmpty()) {
            hashCode = 31 * hashCode + terms.size();
            for (int i = 0; i < terms.size(); i++) {
                hashCode = 31 * hashCode + getItemHash(terms, i);
            }
        }
    }

    /**
     * Gets the hash of the item.
     *
     * @param terms the terms
     * @param i     the item's index
     * @return the hash
     */
    protected int getItemHash(List<Term> terms, int i) {
        int hash;
        if (terms.get(i) == null) {
            hash = 0;
        } else if (terms.get(i).isConstant()) {
            hash = terms.get(i).hashCode();
        } else {
            hash = i + 1;
        }
        return hash;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Atom other;
        if (o instanceof Atom) {
            other = (Atom) o;
        } else {
            return false;
        }

        substitutionMap = LanguageUtils.doesTermMatch(this, other, fixedTerms);
        return substitutionMap != null;
    }

    /**
     * Gets the substitution map created during the invocation of the {@link #equals(Object)} method.
     *
     * @return the substitution map
     */
    public Map<Term, Term> getSubstitutionMap() {
        return substitutionMap;
    }

}
