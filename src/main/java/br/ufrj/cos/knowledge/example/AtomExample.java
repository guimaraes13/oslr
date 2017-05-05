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

package br.ufrj.cos.knowledge.example;

import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.logic.Variable;
import br.ufrj.cos.util.LanguageUtils;
import br.ufrj.cos.util.LogMessages;
import br.ufrj.cos.util.VariableGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Represents an atom example.
 * <p>
 * Created on 17/04/17.
 *
 * @author Victor Guimarães
 */
public class AtomExample extends Atom implements Example {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    protected final boolean positive;
    protected final Atom goalQuery;

    /**
     * Constructs a positive {@link AtomExample}
     *
     * @param name  the {@link Atom}'s predicate name
     * @param terms the {@link Atom}'s {@link Term}s
     */
    public AtomExample(String name, List<Term> terms) {
        this(name, terms, true);
    }

    /**
     * Constructs an {@link AtomExample}
     *
     * @param name     the predicate name
     * @param terms    the {@link Atom}'s {@link Term}s
     * @param positive the value of the example, true for positive; false for negative
     */
    public AtomExample(String name, List<Term> terms, boolean positive) {
        super(name, terms);
        this.positive = positive;

        if (isGrounded()) {
            try {
                this.goalQuery = LanguageUtils.toVariableAtom(this, new HashMap<>(), new VariableGenerator());
            } catch (IllegalAccessException | InstantiationException e) {
                logger.error(LogMessages.ERROR_BUILDING_ATOM.toString(), e);
                this.goalQuery = null;
            }
        } else {
            this.goalQuery = null;
        }
    }

    /**
     * Constructs a positive proposition like {@link AtomExample}
     *
     * @param name the proposition name
     */
    public AtomExample(String name) {
        this(name, true);
    }

    /**
     * Constructs a proposition like {@link AtomExample}
     *
     * @param name     the proposition name
     * @param positive the value of the example, true for positive; false for negative
     */
    public AtomExample(String name, boolean positive) {
        this(name, null, positive);
    }

    /**
     * Constructs an {@link AtomExample} from another {@link Atom}
     *
     * @param atom     the atom
     * @param positive the value of the example, true for positive; false for negative
     */
    public AtomExample(Atom atom, boolean positive) {
        this(atom.getName(), atom.getTerms(), true);
    }

    /**
     * Gets the {@link Atom} from the {@link AtomExample}
     *
     * @return the {@link Atom}
     */
    public Atom getAtom() {
        return new Atom(this);
    }

    @Override
    public Collection<Term> getPositiveTerms() {
        return (isPositive() ? getTerms() : null);
    }

    @Override
    public boolean isPositive() {
        return positive;
    }

    @Override
    public Map<Term, Variable> getVariableMap() {
        return new HashMap<>();
    }

    @Override
    public Atom getGoalQuery() {
        return goalQuery;
    }

    @Override
    public Iterable<? extends AtomExample> getGroundedQuery() {
        List<AtomExample> atoms = new ArrayList<>();
        if (isGrounded()) {
            atoms.add(this);
        }
        return atoms;
    }

    @Override
    public boolean isFact() {
        return false;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (positive ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AtomExample)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        AtomExample atomExample = (AtomExample) o;

        return positive == atomExample.positive;
    }

    @Override
    public String toString() {
        return LanguageUtils.formatExampleToProPprString(this);
    }

}
