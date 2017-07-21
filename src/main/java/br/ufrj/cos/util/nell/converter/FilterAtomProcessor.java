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
import br.ufrj.cos.logic.Predicate;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Set;

import static br.ufrj.cos.cli.NellBaseConverterCLI.getNegatives;
import static br.ufrj.cos.cli.NellBaseConverterCLI.getPositives;

/**
 * Class to filter the atom from the current iteration if it appears on older iterations.
 * <p>
 * Created on 21/07/17.
 *
 * @author Victor Guimarães
 */
public class FilterAtomProcessor implements AtomProcessor {

    protected final Pair<Map<Predicate, Set<Atom>>, Map<Predicate, Set<Atom>>> atomsPair;
    protected int numberOfFilteredAtoms = 0;

    /**
     * Default constructor with the maps of the filtering iterations.
     *
     * @param atomsPair the maps of the filtering iterations
     */
    public FilterAtomProcessor(Pair<Map<Predicate, Set<Atom>>, Map<Predicate, Set<Atom>>> atomsPair) {
        this.atomsPair = atomsPair;
    }

    /**
     * Filters the pair from this iteration
     *
     * @param pair the pair read from the file
     * @return {@code true} if the pair was removed from the previous iteration, {@code false} otherwise
     */
    @Override
    public boolean isAtomProcessed(Pair<Atom, Boolean> pair) {
        Map<Predicate, Set<Atom>> atoms = pair.getValue() ? getPositives(atomsPair) : getNegatives(atomsPair);
        Set<Atom> atomSet = atoms.get(pair.getKey().getPredicate());
        if (atomSet != null && atomSet.remove(pair.getKey())) {
            numberOfFilteredAtoms++;
            return true;
        }
        return false;
    }

    /**
     * Gets the number of filtered atoms.
     *
     * @return the number of filtered atoms
     */
    public int getNumberOfFilteredAtoms() {
        return numberOfFilteredAtoms;
    }
}
