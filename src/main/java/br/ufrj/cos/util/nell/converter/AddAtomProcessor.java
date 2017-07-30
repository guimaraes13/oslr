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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static br.ufrj.cos.cli.nell.NellBaseConverterCLI.getNegatives;
import static br.ufrj.cos.cli.nell.NellBaseConverterCLI.getPositives;

/**
 * Class to adds the read atoms to the current iteration maps.
 * <p>
 * Created on 21/07/17.
 *
 * @author Victor Guimarães
 */
public class AddAtomProcessor implements AtomProcessor {

    protected final Pair<Map<Predicate, Set<Atom>>, Map<Predicate, Set<Atom>>> previousAtoms;
    protected final Pair<Map<Predicate, Set<Atom>>, Map<Predicate, Set<Atom>>> currentAtoms;

    protected int numberOfSkippedAtoms = 0;
    protected int numberOfRemovedAtoms = 0;

    /**
     * Default constructor with the maps of the previous and current iterations.
     *
     * @param previousAtoms the previous iteration
     * @param currentAtoms  the current iteration
     */
    public AddAtomProcessor(
            Pair<Map<Predicate, Set<Atom>>, Map<Predicate, Set<Atom>>> previousAtoms,
            Pair<Map<Predicate, Set<Atom>>, Map<Predicate, Set<Atom>>> currentAtoms) {
        this.previousAtoms = previousAtoms;
        this.currentAtoms = currentAtoms;
    }

    /**
     * Adds the atom to the set of the current iteration.
     * <p>
     * If the atom already appears in this iteration, ignores it.
     * <p>
     * If the atom appears if same label in a previous iteration, ignores it.
     * <p>
     * if the atom appears if the opposite label on the iteration immediately before that, removes it from the
     * previous iteration.
     *
     * @param pair a pair of a atom and its label {{@code true}, {@code false}}
     * @return {@code true} if the atom was added, {@code false} otherwise.
     */
    @SuppressWarnings("OverlyLongMethod")
    @Override
    public boolean isAtomProcessed(Pair<Atom, Boolean> pair) {
        Map<Predicate, Set<Atom>> thisCurrent;  // current iteration, this label
        Map<Predicate, Set<Atom>> thatCurrent;  // current iteration, other label
        Map<Predicate, Set<Atom>> thisPrevious; // previous iteration, this label
        Map<Predicate, Set<Atom>> thatPrevious; // previous iteration, other label
        if (pair.getValue()) {
            thisCurrent = getPositives(this.currentAtoms);
            thatCurrent = getNegatives(this.currentAtoms);

            thisPrevious = getPositives(this.previousAtoms);
            thatPrevious = getNegatives(this.previousAtoms);
        } else {
            thisCurrent = getNegatives(this.currentAtoms);
            thatCurrent = getPositives(this.currentAtoms);

            thisPrevious = getNegatives(this.previousAtoms);
            thatPrevious = getPositives(this.previousAtoms);
        }

        Atom atom = pair.getKey();
        Set<Atom> atomSet;
        // if atom appears with another label in this iteration, ignore it.
        atomSet = thatCurrent.get(atom.getPredicate());
        if (atomSet != null && atomSet.contains(atom)) {
            numberOfSkippedAtoms++;
            return false;
        }

        // if atom appears with same label in the previous iteration, ignore it.
        atomSet = thisPrevious.get(atom.getPredicate());
        if (atomSet != null && atomSet.contains(atom)) {
            numberOfSkippedAtoms++;
            return false;
        }

        // if atom appears with other label in the previous iteration, removes it from there.
        atomSet = thatPrevious.get(atom.getPredicate());
        if (atomSet != null) {
            numberOfRemovedAtoms++;
            atomSet.remove(atom);
        }

        // adding the atom to this list
        if (thisCurrent.computeIfAbsent(atom.getPredicate(), p -> new LinkedHashSet<>()).add(atom)) {
            return true;
        } else {
            numberOfSkippedAtoms++;
            return false;
        }
    }

    /**
     * Gets the number of skipped atoms that was already present on the previous iteration.
     *
     * @return the number of skipped atoms
     */
    public int getNumberOfSkippedAtoms() {
        return numberOfSkippedAtoms;
    }

    /**
     * Gets the number of removed atoms from the previous iteration, that contradicts atoms from this iteration.
     *
     * @return the number of removed atoms from the previous iteration
     */
    public int getNumberOfRemovedAtoms() {
        return numberOfRemovedAtoms;
    }
}
