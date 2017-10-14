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

package br.ufrj.cos.knowledge.theory.manager.feature.proppr.heuristic;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.util.Initializable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * A heuristic to sign a value for each variable of the query, given its substitutions.
 * <p>
 * Created on 13/10/2017.
 *
 * @author Victor Guimarães
 */
public abstract class SubstitutionHeuristic implements Comparator<Double>, Initializable {

    /**
     * Calculates the heuristic value of each variable.
     *
     * @param variables     the list of variables
     * @param substitutions the inferred substitutions
     * @return the map of values per variables.
     */
    public abstract Map<Term, Double> calculateHeuristic(List<Term> variables,
                                                         Map<Example, Map<Atom, Double>> substitutions);

    /**
     * By default, as higher the value better the variable. Override this method, otherwise.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public int compare(Double o1, Double o2) {
        return -Double.compare(o1, o2);
    }

}
