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

package br.ufrj.cos.util;

import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Constant;
import br.ufrj.cos.logic.Predicate;
import br.ufrj.cos.logic.Term;

import java.util.*;

/**
 * A factory that creates Atom, keeping the same predicate and constants pointing out to the same object.
 * <p>
 * This may dramatically improve the memory use on large knowledge bases.
 * <p>
 * Created on 19/07/17.
 *
 * @author Victor Guimarães
 */
public class AtomFactory {

    /**
     * The arity of a proposition.
     */
    public static final int PROPOSITION_ARITY = 0;
    protected Map<String, Constant> constantMap = new HashMap();
    protected final Map<String, Predicate> predicateMap = new HashMap();

    /**
     * Creates an Atom with the predicate and the terms.
     *
     * @param predicate the predicate
     * @param terms     the terms
     * @return the Atom
     */
    public Atom createAtom(String predicate, String... terms) {
        if (terms == null) {
            return createAtom(predicate);
        }

        return new Atom(getPredicate(predicate, terms.length), buildTerms(terms));
    }

    /**
     * Creates an Atom with the predicate.
     *
     * @param predicate the predicate
     * @return the Atom
     */
    public Atom createAtom(String predicate) {
        return new Atom(getPredicate(predicate, PROPOSITION_ARITY));
    }

    /**
     * Gets the predicate by its name and arity.
     *
     * @param name  the name
     * @param arity the arity
     * @return the predicate
     */
    public Predicate getPredicate(final String name, final int arity) {
        return predicateMap.computeIfAbsent(LanguageUtils.formatPredicate(name, arity),
                                            k -> new Predicate(name, arity));
    }

    /**
     * Builds the terms by the values.
     *
     * @param values the values
     * @return the list of terms.
     */
    protected List<Term> buildTerms(String[] values) {
        List<Term> terms = new ArrayList<>(values.length);
        for (String value : values) {
            terms.add(constantMap.computeIfAbsent(value, Constant::new));
        }
        return terms;
    }

    /**
     * Gets a Constant by its string name.
     *
     * @param name the name
     * @return the Constant
     */
    public Constant getConstant(String name) {
        return constantMap.computeIfAbsent(name, Constant::new);
    }

    /**
     * Clears the constant map.
     */
    public void clearConstantMap() {
        this.constantMap = new HashMap<>();
    }

    /**
     * Gets the predicated read by this factory.
     *
     * @return the predicated read by this factory
     */
    public Collection<Predicate> getPredicates() {
        return predicateMap.values();
    }

}
