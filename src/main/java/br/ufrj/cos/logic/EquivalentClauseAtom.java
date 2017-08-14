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

import java.util.Set;

/**
 * Clause to extend the concept of equivalent atom also comparing the the clause body.
 * <p>
 * Created on 12/08/17.
 *
 * @author Victor Guimarães
 */
public class EquivalentClauseAtom extends EquivalentAtom {

    protected final Set<Literal> body;

    /**
     * Constructor with all the needed parameters.
     *
     * @param body       the body of the clause
     * @param atom       the candidate atom to be added
     * @param fixedTerms the fixed terms of the clause
     */
    public EquivalentClauseAtom(Set<Literal> body, Atom atom, Set<Term> fixedTerms) {
        super(atom, fixedTerms);
        this.body = body;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + body.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof EquivalentClauseAtom)) { return false; }
        if (!super.equals(o)) { return false; }

        EquivalentClauseAtom that = (EquivalentClauseAtom) o;

        return body.equals(that.body);
    }

}
