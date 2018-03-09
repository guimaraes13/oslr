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

package br.ufrj.cos.logic;

import br.ufrj.cos.util.LanguageUtils;

import java.util.List;

/**
 * Represents a logic literal. A literal is an {@link Atom} or a negation of one.
 * <p>
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class Literal extends Atom {

    /**
     * {@link Literal} that represents the true logic value
     */
    @SuppressWarnings("unused")
    public static final Literal TRUE_LITERAL = LanguageUtils.buildTrueLiteral();
    /**
     * {@link Literal} that represents the false logic value
     */
    public static final Literal FALSE_LITERAL = LanguageUtils.buildFalseLiteral();

    protected final boolean negated;

    /**
     * Constructs a {@link Literal} by with fields.
     *
     * @param predicate    the predicate
     * @param terms   the {@link Term}s
     * @param negated if it is negated
     */
    public Literal(Predicate predicate, List<Term> terms, boolean negated) {
        super(predicate, terms);
        this.negated = negated;
    }

    /**
     * Constructs a propositional version of a {@link Literal}
     *
     * @param predicate the proposition
     * @param negated if it is negated
     */
    public Literal(Predicate predicate, boolean negated) {
        super(predicate);
        this.negated = negated;
    }

    /**
     * Constructs a positive literal, by omitting the {@link #negated} field.
     *
     * @param predicate the predicate
     * @param terms the {@link Term}s
     */
    public Literal(Predicate predicate, List<Term> terms) {
        super(predicate, terms);
        negated = false;
    }

    /**
     * Constructs a positive propositional form of a {@link Literal}
     *
     * @param predicate the proposition predicate
     */
    public Literal(Predicate predicate) {
        super(predicate);
        negated = false;
    }

    /**
     * Constructs a {@link Literal} based on an {@link Atom}
     *
     * @param atom    the {@link Atom}
     * @param negated if it is negated
     */
    public Literal(Atom atom, boolean negated) {
        super(atom.getPredicate(), atom.getTerms());
        this.negated = negated;
    }

    /**
     * Constructs a positive {@link Literal} based on an {@link Atom}
     *
     * @param atom the {@link Atom}
     */
    public Literal(Atom atom) {
        super(atom);
        this.negated = false;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        if (negated) {
            result = 31 * result + 1;
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Literal)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        Literal literal = (Literal) o;

        return negated == literal.negated;
    }

    @Override
    public String toString() {
        if (negated) {
            return LanguageUtils.NEGATION_PREFIX + " " + super.toString();
        }

        return super.toString();
    }

    /**
     * Gets if the it is negated.
     *
     * @return {@code true} if it is, {@code false} otherwise
     */
    public boolean isNegated() {
        return negated;
    }
}
