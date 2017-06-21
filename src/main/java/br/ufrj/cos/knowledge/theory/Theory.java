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

package br.ufrj.cos.knowledge.theory;

import br.ufrj.cos.knowledge.Knowledge;
import br.ufrj.cos.knowledge.KnowledgeException;
import br.ufrj.cos.logic.Conjunction;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.util.ExceptionMessages;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Responsible for holding the theory.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
public class Theory extends Knowledge<HornClause> {

    /**
     * Constructs the {@link Theory} from a {@link Collection} of {@link HornClause}s.
     *
     * @param clauses the {@link Collection} of {@link HornClause}s.
     */
    public Theory(Collection<HornClause> clauses) {
        super(clauses);
    }

    /**
     * Constructs the {@link Theory} from a {@link HornClause}s.
     *
     * @param clause the {@link HornClause}s.
     */
    public Theory(HornClause clause) {
        super(clause);
    }

    /**
     * Constructs the {@link Theory} from a {@link Collection} of {@link HornClause}s with a filter {@link Predicate}.
     *
     * @param clauses         the {@link Collection} of {@link HornClause}s.
     * @param acceptPredicate filter {@link Predicate}.
     */
    public Theory(Collection<HornClause> clauses, Predicate<? super HornClause> acceptPredicate) {
        super(clauses, acceptPredicate);
    }

    /**
     * Constructs the {@link Theory} from a {@link HornClause} and a filter {@link Predicate}.
     *
     * @param clause          the {@link Collection} of {@link HornClause}s.
     * @param acceptPredicate filter {@link Predicate}.
     */
    public Theory(HornClause clause, Predicate<? super HornClause> acceptPredicate) {
        super(clause, acceptPredicate);
    }

    /**
     * Copies the theory.
     *
     * @return a copy of this {@link Theory}
     * @throws KnowledgeException in case of reflection error
     */
    public Theory copy() throws KnowledgeException {
        try {
            Collection<HornClause> copy = collection.getClass().newInstance();
            copy.addAll(collection);
            return new Theory(copy, acceptPredicate);
        } catch (Exception e) {
            throw new KnowledgeException(ExceptionMessages.ERROR_DURING_THEORY_COPY.toString(), e);
        }
    }

    /**
     * Copies the theory creating new instance of the inner rules and its bodies. With this copy, any changes to the
     * body of a rule will not change the original theory, although changes in the atoms and literal will do.
     *
     * @return a copy of this {@link Theory}
     * @throws KnowledgeException in case of reflection error
     */
    public Theory deepCopy() throws KnowledgeException {
        try {
            Collection<HornClause> copy = collection.getClass().newInstance();
            for (HornClause hornClause : this) {
                copy.add(new HornClause(hornClause.getHead(), new Conjunction(hornClause.getBody())));
            }
            return new Theory(copy, acceptPredicate);
        } catch (Exception e) {
            throw new KnowledgeException(ExceptionMessages.ERROR_DURING_THEORY_COPY.toString(), e);
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (HornClause hornClause : this) {
            stringBuilder.append(hornClause).append("\n");
        }
        return stringBuilder.toString().trim();
    }
}
