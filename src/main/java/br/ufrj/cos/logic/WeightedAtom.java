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
 * Represents a WeightedAtom.
 * <p>
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class WeightedAtom extends Atom {

    protected double weight = 1.0;

    /**
     * Constructs a {@link WeightedAtom} by its fields with default {@link #weight}.
     *
     * @param predicate the predicate
     * @param terms     the {@link Term}s
     */
    public WeightedAtom(Predicate predicate, List<Term> terms) {
        super(predicate, terms);
    }

    /**
     * Constructs a proposition representation of {@link WeightedAtom} with default {@link #weight}.
     *
     * @param predicate the predicate
     */
    public WeightedAtom(Predicate predicate) {
        super(predicate);
    }

    /**
     * Constructs a {@link WeightedAtom} from an {@link Atom}.
     *
     * @param weight the weight
     * @param atom   the {@link Atom}
     */
    public WeightedAtom(double weight, Atom atom) {
        super(atom.getPredicate(), atom.getTerms());
        this.weight = weight;
    }

    /**
     * Constructs a {@link WeightedAtom} by its fields.
     *
     * @param weight the weight
     * @param predicate   the predicate
     * @param terms  the {@link Term}s
     */
    public WeightedAtom(double weight, Predicate predicate, List<Term> terms) {
        super(predicate, terms);
        this.weight = weight;
    }

    /**
     * Constructs a proposition representation of {@link WeightedAtom} with {@link #weight}.
     *
     * @param weight the weight
     * @param predicate   the predicate
     */
    public WeightedAtom(double weight, Predicate predicate) {
        super(predicate);
        this.weight = weight;
    }

    /**
     * Gets the weight of the {@link Atom}.
     *
     * @return the weight of the {@link Atom}
     */
    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return weight + " " + LanguageUtils.WEIGHT_SIGN + " " + super.toString();
    }

}
