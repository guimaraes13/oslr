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

package br.ufrj.cos.language;

import br.ufrj.cos.util.LanguageUtils;

import java.util.List;

/**
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class Literal extends Atom {

    protected boolean negated = false;

    public Literal(String name, List<Term> terms, boolean negated) {
        super(name, terms);
        this.negated = negated;
    }

    public Literal(String name, boolean negated) {
        super(name);
        this.negated = negated;
    }

    public Literal(String name, List<Term> terms) {
        super(name, terms);
    }

    public Literal(String name) {
        super(name);
    }

    @Override
    public String toString() {
        if (negated) {
            return LanguageUtils.NEGATION_PREFIX + " " + super.toString();
        }

        return super.toString();
    }
}
