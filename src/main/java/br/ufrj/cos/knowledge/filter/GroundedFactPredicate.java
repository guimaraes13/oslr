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

package br.ufrj.cos.knowledge.filter;

import br.ufrj.cos.logic.Clause;

/**
 * Accepts for grounded facts. Use its negation to filter {@link Clause} that is not a grounded fact.
 * <p>
 * Created on 25/04/17.
 *
 * @author Victor Guimarães
 */
public class GroundedFactPredicate implements ClausePredicate {

    @Override
    public boolean test(Clause o) {
        return o.isFact() && o.isGrounded();
    }

    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() + ": Accepts only grounded facts]";
    }
}
