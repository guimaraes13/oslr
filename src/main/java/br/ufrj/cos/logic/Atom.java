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

import java.util.List;

import static br.ufrj.cos.util.LanguageUtils.formatAtomToString;

/**
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class Atom extends Clause {

    public static final String TRUE_VALUE = "true";
    public static final String FALSE_VALUE = "false";
    public static final Atom TRUE_ATOM = new Atom(TRUE_VALUE);
    public static final Atom FALSE_ATOM = new Atom(FALSE_VALUE);

    protected String name;
    protected List<Term> terms;

    public Atom(Atom atom) {
        this.name = atom.name;
        this.terms = atom.terms;
    }

    public Atom(String name, List<Term> terms) {
        this.name = name;
        this.terms = terms;
    }

    public Atom(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Term> getTerms() {
        return terms;
    }

    @Override
    public boolean isGrounded() {
        for (Term term : terms) {
            if (!(term instanceof Constant)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isFact() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Atom)) {
            return false;
        }

        Atom atom = (Atom) o;

        if (!name.equals(atom.name)) {
            return false;
        }
        return terms != null ? terms.equals(atom.terms) : atom.terms == null;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (terms != null ? terms.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return formatAtomToString(this);
    }

}
