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

package br.ufrj.cos.knowledge.theory.manager.revision.point;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;

/**
 * Class to select if a example is relevant to be evaluated or not. All the examples of a revision point must be
 * passed to this class, and the relevance of one example might depends on a previously evaluated example.
 * <p>
 * Created on 09/07/17.
 *
 * @author Victor Guimarães
 */
public interface RelevantSampleSelector extends Initializable {

    /**
     * Checks if a example is relevant in a revision point.
     *
     * @param example the example
     * @return {@code true} if it is, {@code false} otherwise
     */
    public boolean isRelevant(Example example);

    /**
     * Creates another instance of this class with the same parameters of this instance, which are independents from
     * the examples. The new instance must behave exactly as this one, except by the part that is based on the
     * previously evaluated examples.
     *
     * @return
     */
    public RelevantSampleSelector copy() throws InitializationException;

}
