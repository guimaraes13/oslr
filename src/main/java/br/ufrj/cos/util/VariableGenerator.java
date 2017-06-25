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

import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Variable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Generates variable from an arbitrary set of names.
 * <p>
 * Created on 29/04/17.
 *
 * @author Victor Guimarães
 */
public class VariableGenerator implements Iterator<Variable> {

    /**
     * The prefix of the {@link Variable}s' names
     */
    public static final String PREFIX = "X";
    protected int counter = 1;

    protected Collection<String> usedNames;

    /**
     * Constructs this class.
     */
    public VariableGenerator() {
        usedNames = new HashSet<>();
    }

    /**
     * Constructs this class without repeating names from the atom.
     *
     * @param atom the atom
     */
    public VariableGenerator(Atom atom) {
        usedNames = new HashSet<>();
        appendUsedNamesFromAtom(atom);
        this.counter = usedNames.size();
    }

    /**
     * Appends the terms from the atom to the used terms
     *
     * @param atom the atom
     */
    protected void appendUsedNamesFromAtom(Atom atom) {
        atom.getTerms().forEach(t -> usedNames.add(t.getName()));
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Variable next() {
        String name = getName();
        while (usedNames.contains(name)) {
            name = getName();
        }

        return new Variable(name);
    }

    /**
     * Gets the {@link Variable}'s name
     *
     * @return the {@link Variable}'s name
     */
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    protected String getName() {
        return PREFIX + counter++;
    }

    /**
     * Get the used names that will not be generated by this class. Those names do not include the ones that this
     * class already creates or might create
     *
     * @return the used names
     */
    public Collection<String> getUsedNames() {
        return usedNames;
    }

    /**
     * Sets variable names that are already in use, those names will not be generated by this class
     *
     * @param usedNames the used names
     */
    public void setUsedNames(Collection<String> usedNames) {
        if (usedNames != null) {
            this.usedNames = usedNames;
            this.counter = usedNames.size();
        }
    }

}
