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

import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.logic.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to centralize useful method with respect with to {@link HornClause}s.
 * <p>
 * Created on 29/04/17.
 *
 * @author Victor Guimarães
 */
public class HornClauseUtils {

    /**
     * Checks if a {@link HornClause} is safe. A {@link HornClause} is safe when all the variable of the clause
     * appear, at least once, in a non-negated literal of the body, including the variables in the head.
     *
     * @param hornClause the {@link HornClause}
     * @return {@code true} if the {@link HornClause} is safe, {@code false} otherwise
     */
    public static boolean isRuleSafe(HornClause hornClause) {
        return isRuleSafe(hornClause.getHead(), hornClause.getBody());
    }

    /**
     * Checks if a {@link HornClause} is safe. A {@link HornClause} is safe when all the variable of the clause
     * appear, at least once, in a non-negated literal of the body, including the variables in the head.
     *
     * @param head the {@link HornClause}'s head
     * @param body the {@link HornClause}'s body
     * @return {@code true} if the {@link HornClause} is safe, {@code false} otherwise
     */
    public static boolean isRuleSafe(Atom head, Iterable<Literal> body) {
        return getSafeTerms(body).containsAll(getNonSafeTerms(head, body));
    }

    /**
     * Gets the safe {@link Term}s from the {@link HornClause}'s body. The safe {@link Term}s are those which appear in
     * non-negated {@link Literal}s.
     *
     * @param clauseBody the {@link HornClause}'s body
     * @return the safe {@link Term}s
     */
    public static Set<Term> getSafeTerms(Iterable<? extends Literal> clauseBody) {
        Set<Term> terms = new HashSet<>();
        for (Literal literal : clauseBody) {
            if (!literal.isNegated()) {
                terms.addAll(literal.getTerms());
            }
        }

        return terms;
    }

    /**
     * Gets the non-safe {@link Term}s of the {@link HornClause}. The non-safe {@link Term}s are the non-constants
     * which appear in negated {@link Literal}s or in the head of the {@link HornClause}.
     *
     * @param head the {@link HornClause}'s head
     * @param body the {@link HornClause}'s body
     * @return the non-safe {@link Term}s
     */
    public static Set<Term> getNonSafeTerms(Atom head, Iterable<Literal> body) {
        Set<Term> terms = new HashSet<>();
        for (Literal literal : body) {
            if (literal.isNegated()) {
                appendNonConstantTerms(literal, terms);
            }
        }

        appendNonConstantTerms(head, terms);

        return terms;
    }

    /**
     * Appends the constant {@link Term}s into the {@link Collection}.
     *
     * @param atom  the atom
     * @param terms the {@link Collection}
     */
    public static void appendNonConstantTerms(Atom atom, Collection<Term> terms) {
        for (Term term : atom.getTerms()) {
            if (!term.isConstant()) {
                terms.add(term);
            }
        }
    }

    /**
     * Checks if a {@link HornClause} with an additional candidate {@link Literal} will be safe. A {@link HornClause}
     * is safe when all the variable of the clause appear, at least once, in a non-negated literal of the body,
     * including the variables in the head.
     *
     * @param head      the {@link HornClause}'s head
     * @param body      the {@link HornClause}'s body
     * @param candidate the additional literal in the body
     * @return {@code true} if the {@link HornClause} is safe, {@code false} otherwise
     */
    public static boolean willBeRuleSafe(Atom head, Iterable<Literal> body, Literal candidate) {
        Set<Term> nonSafe = getNonSafeTerms(head, body);
        if (candidate.isNegated()) {
            appendNonConstantTerms(candidate, nonSafe);
        }
        return getSafeTerms(body).containsAll(nonSafe);
    }

    /**
     * Gets, from a possibly safe {@link HornClause}, another {@link HornClause} with the minimal set of
     * {@link Literal} in the body that makes the clause safe. As this set may not be unique, returns all the sets
     * tied to be the minimal.
     *
     * @param bottomClause the bottom clause
     * @return a {@link Set} of {@link HornClause} where the clause has the minimal necessary {@link Literal} to be
     * safe.
     * @throws TheoryRevisionException in an error occurs during the revision
     */
    public static Set<HornClause> buildMinimalSafeRule(HornClause bottomClause) throws TheoryRevisionException {
        if (!HornClauseUtils.mayRuleBeSafe(bottomClause)) {
            throw new TheoryRevisionException(ExceptionMessages.GENERATED_RULE_NOT_SAVE.toString());
        }

        List<Literal> candidateLiterals = new ArrayList<>(getNonNegativeLiteralsWithHeadVariable(bottomClause));
        Queue<Set<Literal>> queue = new ArrayDeque<>();
        queue.add(new HashSet<>());
        Set<HornClause> safeClauses = null;
        for (int i = 0; i < candidateLiterals.size(); i++) {
            appendAllCandidatesToQueue(candidateLiterals, queue);
            safeClauses = buildSafeClauses(bottomClause.getHead(), queue);
            if (!safeClauses.isEmpty()) {
                return safeClauses;
            }
        }
        return safeClauses;
    }

    /**
     * Checks if a {@link HornClause} can become safe by removing {@link Literal} from its body. A {@link HornClause}
     * is safe when all the variables of the clause appear, at least once, in a non-negated literal of the body.
     * Including the variables in the head.
     * <p>
     * If there are variables on negated {@link Literal}s on the body that do
     * not appear on the non-negated ones the rule can be made safe by removing those {@link Literal}s.
     * <p>
     * If there is, at least one, variable on the head of the rule that does not appears on a non-negated
     * {@link Literal} on its body, the rule can not become safe, and this method return {@code false}.
     * It would be necessary to gather more non-negated {@link Literal} to its body.
     *
     * @param hornClause the {@link HornClause}
     * @return {@code true} if the {@link HornClause} can be safe, {@code false} otherwise
     */
    public static boolean mayRuleBeSafe(HornClause hornClause) {
        return mayRuleBeSafe(hornClause.getHead(), hornClause.getBody());
    }

    /**
     * Gets the {@link Literal} from the body of the {@link HornClause} that has, at least one, variable that appears
     * in the head.
     *
     * @param hornClause the {@link HornClause}
     * @return the {@link Literal} that has variables that appear in the head
     */
    protected static Collection<Literal> getNonNegativeLiteralsWithHeadVariable(HornClause hornClause) {
        final Set<Term> headVariables = new HashSet<>();
        appendNonConstantTerms(hornClause.getHead(), headVariables);

        return hornClause.getBody().stream().filter(
                literal -> !literal.isNegated() && !Collections.disjoint(headVariables, literal.getTerms()))
                .collect(Collectors.toSet());
    }

    /**
     * For each {@link Set} of candidates in the {@link Queue}, removes the {@link Set} and add a new one for each
     * candidate at the {@link Iterable}.
     *
     * @param candidates the {@link Iterable} of candidates
     * @param queue      the {@link Queue} if {@link Set}s of candidates
     * @param <T>        the candidate's type
     */
    protected static <T> void appendAllCandidatesToQueue(Iterable<T> candidates, Queue<Set<T>> queue) {
        Set<T> currentSetBase;
        Set<T> currentSet;
        for (int i = 0; i < queue.size(); i++) {
            currentSetBase = queue.poll();
            for (T t : candidates) {
                currentSet = new HashSet<>(currentSetBase);
                currentSet.add(t);
                queue.add(currentSet);
            }
        }
    }

    /**
     * Build a {@link Set} of safe {@link HornClause}s from the candidate bodies, when possible. If no body would be
     * safe, an empty {@link Set} is returned.
     *
     * @param head            the head of the {@link HornClause}
     * @param candidateBodies a {@link Iterable} of candidate bodies
     * @return the {@link Set} of safe {@link HornClause}s
     */
    protected static Set<HornClause> buildSafeClauses(Atom head, Iterable<Set<Literal>> candidateBodies) {
        Set<HornClause> hornClauses = new HashSet<>();
        for (Set<Literal> candidateBody : candidateBodies) {
            if (isRuleSafe(head, candidateBody)) {
                hornClauses.add(new HornClause(head, new Conjunction(candidateBody)));
            }
        }
        return hornClauses;
    }

    /**
     * Checks if a {@link HornClause} can become safe by removing {@link Literal} from its body. A {@link HornClause}
     * is safe when all the variables of the clause appear, at least once, in a non-negated literal of the body.
     * Including the variables in the head.
     * <p>
     * If there are variables on negated {@link Literal}s on the body that do
     * not appear on the non-negated ones the rule can be made safe by removing those {@link Literal}s.
     * <p>
     * If there is, at least one, variable on the head of the rule that does not appears on a non-negated
     * {@link Literal} on its body, the rule can not become safe, and this method return {@code false}.
     * It would be necessary to gather more non--negated {@link Literal} to its body.
     *
     * @param head the {@link HornClause}'s head
     * @param body the {@link HornClause}'s body
     * @return {@code true} if the {@link HornClause} can be safe, {@code false} otherwise
     */
    public static boolean mayRuleBeSafe(Atom head, Iterable<Literal> body) {
        return getSafeTerms(body).containsAll(head.getTerms());
    }

}
