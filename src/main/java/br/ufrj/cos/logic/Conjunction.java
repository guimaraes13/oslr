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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class Conjunction extends ArrayList<Literal> {

    public Conjunction(int initialCapacity) {
        super(initialCapacity);
    }

    public Conjunction() {
        super();
    }

    public Conjunction(Collection<? extends Literal> c) {
        super(c);
    }

    public Conjunction(Literal... literals) {
        super(literals.length);

        for (Literal literal : literals) {
            add(literal);
        }
    }

    public boolean isGrounded() {
        for (Literal literal : this) {
            if (!literal.isGrounded()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return LanguageUtils.listToString(this);
    }

}
