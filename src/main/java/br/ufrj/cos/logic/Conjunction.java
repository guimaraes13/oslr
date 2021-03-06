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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Represents a conjunction of {@link Literal}s.
 * <p>
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class Conjunction extends LinkedHashSet<Literal> {

    /**
     * Constructs with initial capacity
     *
     * @param initialCapacity the initial capacity
     */
    public Conjunction(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Default constructor
     */
    public Conjunction() {
        super();
    }

    /**
     * Constructs from another {@link Collection}
     *
     * @param c the {@link Collection}
     */
    public Conjunction(Collection<? extends Literal> c) {
        super(c);
    }

    /**
     * Constructs from an arbitrary number of {@link Literal}s
     *
     * @param literals the {@link Literal}s
     */
    public Conjunction(Literal... literals) {
        super(Arrays.asList(literals));
    }

    /**
     * Checks if the conjunction is grounded. A conjunction is grounded if all its {@link Term}s are grounded
     *
     * @return {@code true} if it is grounded, {@code false} otherwise
     */
    public boolean isGrounded() {
        for (Literal literal : this) {
            if (!literal.isGrounded()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the last element of the conjunction.
     *
     * @return the last element of the conjunction
     */
    public Literal getLastElement() {
        Literal literal = null;
        for (Literal iterated : this) {
            literal = iterated;
        }
        return literal;
    }

    @Override
    public String toString() {
        return LanguageUtils.iterableToString(this);
    }

}
