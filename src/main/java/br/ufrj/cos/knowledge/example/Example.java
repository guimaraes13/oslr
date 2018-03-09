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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a training iterator. This training iterator might have more than one positive iterator (e.g. in the
 * case it is in the ProPPR form)
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 */
public interface Example {

    /**
     * Gets the {@link Atom} representation of the example. In the case of the ProPPR example, get the goal
     *
     * @return the {@link Atom} representation of the example
     */
    public Atom getAtom();

    /**
     * Gets the {@link Term}s of the positive iterator.
     *
     * @return the {@link Term}
     */
    public Collection<Term> getPositiveTerms();

    /**
     * Checks if the example is positive. In the case of the ProPPR example, check if it at least has a positive part.
     *
     * @return {@code true} if the example is positive, {@code false} otherwise
     */
    public abstract boolean isPositive();

    /**
     * Gets a {@link Map} to map the {@link Term}s in the example into variables. Specially useful when mapping a set
     * of {@link Term}s in the same variable, as the ProPPR example semantics does.
     *
     * @return the {@link Map}
     */
    public default Map<Term, Term> getVariableMap() {
        return new HashMap<>();
    }

    /**
     * Gets the goal query from an example.
     *
     * @return the goal query
     */
    public Atom getGoalQuery();

    /**
     * Gets the grounded queries from a example, i.e. the grounds for the goal query.
     *
     * @return the grounded queries
     */
    public Collection<? extends AtomExample> getGroundedQuery();

    @Override
    public int hashCode();

    @Override
    public boolean equals(Object o);

    @Override
    public String toString();

}
