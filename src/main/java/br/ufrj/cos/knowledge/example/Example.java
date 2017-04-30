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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a training examples. This training examples might have more than one positive examples (e.g. in the
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
     * Gets the {@link Term}s of the positive examples.
     *
     * @return the {@link Term}
     */
    public Collection<Term> getPositiveTerms();

    /**
     * Gets a {@link Map} to map the {@link Term}s in the example into variables. Specially useful when mapping a set
     * of {@link Term}s in the same variable, as the ProPPR example semantics does.
     *
     * @return the {@link Map}
     */
    public default Map<Term, Variable> getVariableMap() {
        return new HashMap<>();
    }

    @Override
    public int hashCode();

    @Override
    public boolean equals(Object o);

    @Override
    public String toString();

}
