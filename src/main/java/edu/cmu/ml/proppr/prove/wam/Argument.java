/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2018 Victor Guimarães
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

package edu.cmu.ml.proppr.prove.wam;

public abstract class Argument implements Comparable<Argument> {

    public static final String VARIABLE_ATOM = "[A-Z_]";

    public static Argument fromString(String s) {
        return new ConstantArgument(s);
    }

    public boolean isVariable() {
        return !this.isConstant();
    }

    public abstract boolean isConstant();

    @Override
    public int compareTo(Argument a) {
        return this.getName().compareTo(a.getName());
    }

    public String getName() {
        return String.valueOf(this.getValue());
    }

    public int getValue() {
        return 0;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Argument)) { return false; }
        return ((Argument) o).getName().equals(getName());
    }

    public boolean isVariableAtom() {
        String n = getName().substring(0, 1);
        return n.matches(VARIABLE_ATOM);
    }
}
