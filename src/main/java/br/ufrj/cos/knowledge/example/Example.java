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
import br.ufrj.cos.util.LanguageUtils;

import java.util.List;

/**
 * Created on 17/04/17.
 *
 * @author Victor Guimarães
 */
public class Example extends Atom {

    boolean positive = true;

    public Example(String name, List<Term> terms, boolean positive) {
        super(name, terms);
        this.positive = positive;
    }

    public Example(String name, boolean positive) {
        super(name);
        this.positive = positive;
    }

    public Example(String name, List<Term> terms) {
        super(name, terms);
    }

    public Example(String name) {
        super(name);
    }

    public Example(Atom atom, boolean positive) {
        super(atom.getName(), atom.getTerms());
        this.positive = positive;
    }

    public boolean isPositive() {
        return positive;
    }

    public Atom getAtom() {
        return new Atom(this);
    }

    @Override
    public boolean isFact() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Example)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        Example example = (Example) o;

        return positive == example.positive;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (positive ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return LanguageUtils.formatExampleToProPprString(this);
    }

}
