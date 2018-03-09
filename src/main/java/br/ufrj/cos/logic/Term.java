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

/**
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public abstract class Term {

    protected final String name;

    /**
     * Constructs a {@link Term}
     *
     * @param name the {@link Term}'s name
     */
    protected Term(String name) {
        this.name = name;
    }

    /**
     * Gets the {@link Term}'s name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if the {@link Term} is a constant
     *
     * @return true if it is a constant, false otherwise
     */
    public abstract boolean isConstant();

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

}
