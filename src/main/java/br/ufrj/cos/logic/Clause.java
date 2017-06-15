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

/**
 * Represents a logic clause
 * <p>
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("ClassMayBeInterface")
public abstract class Clause {

    /**
     * Checks if the {@link Clause} is grounded, i.e. all its {@link Term}s are constants.
     *
     * @return true if the {@link Clause} is grounded, false otherwise
     */
    public abstract boolean isGrounded();

    /**
     * Checks if the {@link Clause} represents a fact. The clause can be considered a fact either if there is no body
     * or if
     * the body is the logic value true
     *
     * @return true if the {@link Clause} represents a fact, false otherwise
     */
    public abstract boolean isFact();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

}
