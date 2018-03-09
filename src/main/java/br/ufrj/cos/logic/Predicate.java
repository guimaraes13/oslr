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

/**
 * Represents a predicate.
 * <p>
 * Created on 17/07/17.
 *
 * @author Victor Guimarães
 */
public class Predicate {

    /**
     * Arity for a predicate with a variable number of arguments, if this is the arity of the predicate, the size of
     * the terms within the atom should not be checked against the predicate's arity.
     */
    public static final int VAR_ARGS_ARITY = -1;

    protected final String name;
    protected final int arity;

    /**
     * Constructor with name and arity.
     *
     * @param name  the name
     * @param arity the arity
     */
    public Predicate(String name, int arity) {
        this.name = name;
        this.arity = arity;
    }

    /**
     * Constructor for proposition, assumes zero arity.
     *
     * @param name the name
     */
    public Predicate(String name) {
        this.name = name;
        this.arity = 0;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the arity.
     *
     * @return the arity
     */
    public int getArity() {
        return arity;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + arity;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Predicate)) { return false; }

        Predicate predicate = (Predicate) o;

        if (arity != predicate.arity) { return false; }
        return name.equals(predicate.name);
    }

    @Override
    public String toString() {
        return name + LanguageUtils.PREDICATE_ARITY_SEPARATOR + arity;
    }

}
