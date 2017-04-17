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

import java.util.List;

/**
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class WeightedAtom extends Atom {

    protected double weight = 1.0;

    public WeightedAtom(String name, List<Term> terms) {
        super(name, terms);
    }

    public WeightedAtom(String name) {
        super(name);
    }

    public WeightedAtom(double weight, Atom atom) {
        super(atom.getName(), atom.getTerms());
        this.weight = weight;
    }

    public WeightedAtom(double weight, String name, List<Term> terms) {
        super(name, terms);
        this.weight = weight;
    }

    public WeightedAtom(double weight, String name) {
        super(name);
        this.weight = weight;
    }

    @Override
    public String toString() {
        return weight + " " + LanguageUtils.WEIGHT_SIGN + " " + super.toString();
    }

}
