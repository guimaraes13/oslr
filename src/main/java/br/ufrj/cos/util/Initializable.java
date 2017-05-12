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

package br.ufrj.cos.util;

/**
 * Interface to allow objects to have default constructors and be latter initialized. This allows the objects to be
 * loaded by configuration files.
 * <p>
 * Created on 12/05/17.
 *
 * @author Victor Guimarães
 */
public interface Initializable {

    /**
     * Method to allow the implementation of this interface to initializes itself.
     *
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    public default void initialize() throws InitializationException {

    }

}
