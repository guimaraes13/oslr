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

package br.ufrj.cos.util.nell.converter;

import br.ufrj.cos.logic.Atom;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Interface to process the atom read from the input file.
 * <p>
 * Created on 21/07/17.
 *
 * @author Victor Guimarães
 */
public interface AtomProcessor {

    /**
     * Process the atom represented by the line of the read file.
     *
     * @param pair the atom pair with its label
     * @return depends of the implementation
     */
    public boolean isAtomProcessed(Pair<Atom, Boolean> pair);

}
