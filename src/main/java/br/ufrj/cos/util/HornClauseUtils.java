/*
 * Online Structure Learner by Revision (OSLR) is an online relational
 * learning algorithm that can handle continuous, open-ended
 * streams of relational examples as they arrive. We employ
 * techniques from theory revision to take advantage of the already
 * acquired knowledge as a starting point, find where it should be
 * modified to cope with the new examples, and automatically update it.
 * We rely on the Hoeffding's bound statistical theory to decide if the
 * model must in fact be updated accordingly to the new examples.
 * The system is built upon ProPPR statistical relational language to
 * describe the induced models, aiming at contemplating the uncertainty
 * inherent to real data.
 *
 * Copyright (C) 2017-2018 Victor Guimarães
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

import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.logic.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class to centralize useful method with respect with to {@link HornClause}s.
 * <p>
 * Created on 29/04/17.
 *
 * @author Victor Guimarães
 */
public final class HornClauseUtils {

    /**
     * The substitution predicate name
     */
    public static final String SUBSTITUTION_NAME = "sub_";
    /**
     * The predicate of the query to find the substitution of the variables.
     */
    public static final Predicate SUBSTITUTION_PREDICATE = new Predicate(SUBSTITUTION_NAME, Predicate.VAR_ARGS_ARITY);

    private HornClauseUtils() {
    }

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
     * Gets, from a possibly safe {@link HornClause}, another {@link HornClause} with the minimal set of
     * {@link Literal} in the body that makes the clause safe. As this set may not be unique, returns all the sets
     * tied to be the minimal.
     *
     * @param bottomClause the bottom clause
     * @return a {@link Map} of {@link HornClause} where the clause has the minimal necessary {@link Literal} to be
     * safe.
     * @throws TheoryRevisionException in an error occurs during the revision
     */
    public static Map<HornClause, Map<Term, Term>> buildMinimalSafeRule(HornClause bottomClause)
            throws TheoryRevisionException {
        if (!HornClauseUtils.mayRuleBeSafe(bottomClause)) {
            throw new TheoryRevisionException(ExceptionMessages.GENERATED_RULE_NOT_SAVE.toString());
        }

        List<Literal> candidateLiterals = new ArrayList<>(getNonNegativeLiteralsWithHeadVariable(bottomClause));
        Queue<Pair<Set<Literal>, Map<Term, Term>>> queue = new ArrayDeque<>();
        queue.add(new ImmutablePair<>(new HashSet<>(), new HashMap<>()));
        Map<HornClause, Map<Term, Term>> safeClauses = null;
        for (int i = 0; i < candidateLiterals.size(); i++) {
            appendAllCandidatesToQueue(bottomClause.getHead(), queue, candidateLiterals);
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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
     * <p>
     * In addition, it skips equivalents sets, by checking if the free variables at the candidate atom can be renamed
     * to match the free variables of a previously selected one. If a equivalent atom {@code A} is detected, the
     * substitution map that makes it equals to another a previous atom {@code B} is stored along with {@code B}. In
     * this case, when a rule from a set of candidates is selected for further refinements, it stores a substitution map
     * that, if applied to the candidates, makes the relevants atoms of discarded equivalent atoms, also relevant to
     * the selected rule.
     *
     * @param head       the rule's head
     * @param queue      the {@link Queue} with the {@link Set}s of candidates
     * @param candidates the {@link Iterable} of candidates
     */
    @SuppressWarnings("OverlyLongMethod")
    public static void appendAllCandidatesToQueue(Atom head, Queue<Pair<Set<Literal>, Map<Term, Term>>> queue,
                                                  Iterable<Literal> candidates) {

        //Auxiliary variables
        Set<Literal> candidateSet;      // the current set of candidates to be added to the rule's body
        Set<Literal> currentSetBase;    // the current rule body to add another literal
        Set<Literal> currentSet;        // the current rule body with the additional literal

        Set<Term> fixedTerms;           // the terms already in the rule, to be ignored in the equivalent evaluation

        Pair<Set<Literal>, Map<Term, Term>> entry;  // the entry of the queue
        Pair<Set<Literal>, Map<Term, Term>> pair;   // the pair to be added to the queue containing the new body and ...
        // ... the substitution map to apply on the candidates, if the rule is chosen

        Map<EquivalentAtom, EquivalentAtom> skipAtom = new HashMap<>(); // map to save the previous atom equivalent ...
        // ... to the current
        Map<EquivalentAtom, Map<Term, Term>> skipMap = new HashMap<>(); // map to save the substitution map of the ...
        // ... chosen atom equivalent to the current, so substitution of variable of the current can be stored

        Map<Term, Term> substitutionMap; // the current substitution map, so the new literal relevant to the an atom ...
        // equivalent to a previously selected atom can be made relevant to the previous too
        Map<Term, Term> subMap; // auxiliary variable to the substitution map

        EquivalentAtom currentAtom;     // the current atom
        EquivalentAtom equivalentAtom;  // the previous selected atom equivalent to the current one

        int size = queue.size();    // the initial size of the queue

        for (int i = 0; i < size; i++) {
            entry = queue.remove();
            currentSetBase = entry.getLeft();
            fixedTerms = currentSetBase.stream().flatMap(l -> l.getTerms().stream()).collect(Collectors.toSet());
            fixedTerms.addAll(head.getTerms());
            candidateSet = unifyCandidates(candidates, entry.getRight());
            for (Literal candidate : candidateSet) {
                if (Collections.disjoint(fixedTerms, candidate.getTerms())) { continue; }
                currentAtom = new EquivalentAtom(candidate, fixedTerms);
                equivalentAtom = skipAtom.get(currentAtom);
                if (equivalentAtom == null) {
                    substitutionMap = new HashMap<>(entry.getRight());

                    currentSet = new LinkedHashSet<>(currentSetBase);
                    currentSet.add(candidate);

                    pair = new ImmutablePair<>(currentSet, substitutionMap);
                    skipAtom.put(currentAtom, currentAtom);
                    skipMap.put(currentAtom, substitutionMap);

                    queue.add(pair);
                } else {
                    substitutionMap = skipMap.get(equivalentAtom);
                    subMap = getSubstitutionMap(currentAtom, equivalentAtom, fixedTerms);
                    if (subMap != null) { substitutionMap.putAll(subMap); }
                }
            }
        }
    }

    /**
     * Build a {@link Map} of safe {@link HornClause}s from the candidate bodies, when possible. If no body would be
     * safe, an empty {@link Map} is returned.
     *
     * @param head            the head of the {@link HornClause}
     * @param candidateBodies a {@link Collection} of candidate bodies
     * @return the {@link Map} of safe {@link HornClause}s
     */
    protected static Map<HornClause, Map<Term, Term>> buildSafeClauses(Atom head,
                                                                       Collection<Pair<Set<Literal>, Map<Term,
                                                                               Term>>> candidateBodies) {
        Map<HornClause, Map<Term, Term>> hornClauses = new HashMap<>();
        for (Pair<Set<Literal>, Map<Term, Term>> entry : candidateBodies) {
            if (isRuleSafe(head, entry.getLeft())) {
                hornClauses.put(new HornClause(head, new Conjunction(entry.getLeft())), entry.getRight());
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

    /**
     * Unifies the equivalent {@link Atom}s of the candidates by replacing the variable names using the
     * element.
     *
     * @param candidate       the candidates
     * @param substitutionMap the substitution map
     * @param <T>             the {@link Atom}'s descendant class
     * @return the new set of candidates
     */
    public static <T extends Literal> Set<Literal> unifyCandidates(Iterable<T> candidate,
                                                                   Map<Term, Term> substitutionMap) {
//        if (element.isEmpty()) { return new HashSet<>(); }
        Set<Literal> candidateSet = new HashSet<>();
        for (T t : candidate) {
            candidateSet.add(LanguageUtils.applySubstitution(t, substitutionMap));
        }

        return candidateSet;
    }

    /**
     * Gets the substitution map that makes the currentAtom equals to the equivalentAtom.
     * <p>
     * As the currentAtom and the equivalentAtom have probably already been tested against each other, this method
     * tries to retrieve the stored map that was created during the call of the equals method on the
     * {@link EquivalentAtom}.
     *
     * @param currentAtom    the current atom
     * @param equivalentAtom the equivalent atom
     * @param fixedTerms     the fixed terms
     * @return the substitution map.
     */
    public static Map<Term, Term> getSubstitutionMap(EquivalentAtom currentAtom, EquivalentAtom equivalentAtom,
                                                     Set<Term> fixedTerms) {
        // if the currentAtom was tested for equal against the equivalentAtom, it saved the substitution map
        Map<Term, Term> subMap = currentAtom.getSubstitutionMap();
        if (subMap != null) {
            return subMap;
        } else {
            // if the equivalentAtom was tested for equal against the currentAtom, it saved the substitution map
            // we just need to invert it
            subMap = currentAtom.getSubstitutionMap();
            if (subMap != null) {
                return subMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
            } else {
                // if none of those has the map, build a new one
                return LanguageUtils.getUnifyMap(currentAtom, equivalentAtom, fixedTerms);
            }
        }
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
    public static Set<EquivalentHornClause> buildMinimalSafeEquivalentClauses(HornClause bottomClause)
            throws TheoryRevisionException {
        if (!HornClauseUtils.mayRuleBeSafe(bottomClause)) {
            throw new TheoryRevisionException(ExceptionMessages.GENERATED_RULE_NOT_SAVE.toString());
        }

        List<Literal> candidateLiterals = new ArrayList<>(getNonNegativeLiteralsWithHeadVariable(bottomClause));
        candidateLiterals.sort(Comparator.comparing(l -> l.getPredicate().toString()));
        Queue<EquivalentHornClause> queue = new ArrayDeque<>();
        queue.add(new EquivalentHornClause(bottomClause.getHead()));
        Set<EquivalentHornClause> safeClauses = null;
        for (int i = 0; i < candidateLiterals.size(); i++) {
            appendAllCandidatesToQueue(queue, candidateLiterals);
            safeClauses = queue.stream().filter(e -> isRuleSafe(e.getHead(), e.getClauseBody()))
                    .collect(Collectors.toSet());
            if (!safeClauses.isEmpty()) {
                return safeClauses;
            }
        }

        return safeClauses;
    }

    /**
     * Creates a list of {@link EquivalentHornClause} containing a {@link EquivalentHornClause} for each substitution
     * of each candidate, skipping equivalent clauses.
     * <p>
     * It skips equivalents clauses, by checking if the free variables at the candidate atom can be renamed
     * to match the free variables of a previously selected one. If a equivalent atom {@code A} is detected, the
     * substitution map that makes it equals to another a previous atom {@code B} is stored along with {@code B}. In
     * this case, when a rule from a set of candidates is selected for further refinements, it stores a substitution map
     * that, if applied to the candidates, makes the relevants atoms of discarded equivalent atoms, also relevant to
     * the selected rule.
     *
     * @param queue      the {@link Queue} with the initial clauses
     * @param candidates the {@link List} of candidates
     */
    public static void appendAllCandidatesToQueue(Queue<EquivalentHornClause> queue, List<Literal> candidates) {
        Map<EquivalentClauseAtom, EquivalentClauseAtom> skipAtom = new HashMap<>();
        Map<EquivalentClauseAtom, EquivalentHornClause> skipClause = new HashMap<>();

        int size = queue.size();    // the initial size of the queue

        EquivalentHornClause equivalentHornClause;
        for (int i = 0; i < size; i++) {
            equivalentHornClause = queue.remove();
            queue.addAll(equivalentHornClause.buildInitialClauseCandidates(candidates, skipAtom, skipClause));
        }
    }

    /**
     * Gets all the candidate clauses with one literal at the body.
     *
     * @param bottomClause  the bottom clause
     * @param maxClauseSize the max clause size
     * @return a {@link Set} of {@link HornClause}s.
     */
    public static Set<EquivalentHornClause> buildMinimalEquivalentClauses(HornClause bottomClause, int maxClauseSize) {
        List<Literal> candidateLiterals = new ArrayList<>(getNonNegativeLiteralsWithHeadVariable(bottomClause));
        candidateLiterals.sort(Comparator.comparing(l -> l.getPredicate().toString()));
        Queue<EquivalentHornClause> queue = new ArrayDeque<>();
        queue.add(new EquivalentHornClause(bottomClause.getHead()));
        for (int i = 0; i < maxClauseSize; i++) { appendAllCandidatesToQueue(queue, candidateLiterals); }
        return new HashSet<>(queue);
    }

    /**
     * Creates the new candidate rules, by adding on possible literal to the current rule's body. The current rule
     * is represented by the head and body parameters.
     * <p>
     * In addition, it skips equivalents sets, by checking if the free variables at the candidate atom can be renamed
     * to match the free variables of a previously selected one. If a equivalent atom {@code A} is detected, the
     * substitution map that makes it equals to another a previous atom {@code B} is stored along with {@code B}. In
     * this case, when a rule from a set of candidates is selected for further refinements, it stores a substitution map
     * that, if applied to the candidates, makes the relevants atoms of discarded equivalent atoms, also relevant to
     * the selected rule.
     *
     * @param head       the rule's head
     * @param body       the body of the rule
     * @param candidates the {@link Iterable} of candidates
     * @return the set of candidate clauses
     */
    @SuppressWarnings("OverlyLongMethod")
    public static Set<Pair<HornClause, Map<Term, Term>>> buildAllCandidatesFromClause(Atom head,
                                                                                      Set<Literal> body,
                                                                                      Iterable<Literal> candidates) {

        //Auxiliary variables
        Set<Literal> currentSet;        // the current rule body with the additional literal

        Set<Term> fixedTerms;           // the terms already in the rule, to be ignored in the equivalent evaluation

        Pair<HornClause, Map<Term, Term>> pair;   // the pair to be added to the queue containing the new body and ...
        // ... the substitution map to apply on the candidates, if the rule is chosen

        Map<EquivalentAtom, EquivalentAtom> skipAtom = new HashMap<>(); // map to save the previous atom equivalent ...
        // ... to the current
        Map<EquivalentAtom, Map<Term, Term>> skipMap = new HashMap<>(); // map to save the substitution map of the ...
        // ... chosen atom equivalent to the current, so substitution of variable of the current can be stored

        Map<Term, Term> substitutionMap; // the current substitution map, so the new literal relevant to the an atom ...
        // equivalent to a previously selected atom can be made relevant to the previous too
        Map<Term, Term> subMap; // auxiliary variable to the substitution map

        EquivalentAtom currentAtom;     // the current atom
        EquivalentAtom equivalentAtom;  // the previous selected atom equivalent to the current one

        Set<Pair<HornClause, Map<Term, Term>>> candidateRules = new HashSet<>(); // the answer

        fixedTerms = body.stream().flatMap(l -> l.getTerms().stream()).collect(Collectors.toSet());
        fixedTerms.addAll(head.getTerms());
        for (Literal candidate : candidates) {
            if (Collections.disjoint(fixedTerms, candidate.getTerms()) ||
                    !HornClauseUtils.willRuleBeSafe(head, body, candidate)) {
                continue;
            }
            currentAtom = new EquivalentAtom(candidate, fixedTerms);
            equivalentAtom = skipAtom.get(currentAtom);
            if (equivalentAtom == null) {
                substitutionMap = new HashMap<>();

                currentSet = new HashSet<>(body);
                currentSet.add(candidate);

                pair = new ImmutablePair<>(new HornClause(head, new Conjunction(currentSet)), substitutionMap);
                skipAtom.put(currentAtom, currentAtom);
                skipMap.put(currentAtom, substitutionMap);

                candidateRules.add(pair);
            } else {
                substitutionMap = skipMap.get(equivalentAtom);
                subMap = getSubstitutionMap(currentAtom, equivalentAtom, fixedTerms);
                if (subMap != null) { substitutionMap.putAll(subMap); }
            }
        }

        return candidateRules;
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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean willRuleBeSafe(Atom head, Iterable<Literal> body, Literal candidate) {
        Set<Term> nonSafe = getNonSafeTerms(head, body);
        if (candidate.isNegated()) {
            appendNonConstantTerms(candidate, nonSafe);
        }
        return getSafeTerms(body).containsAll(nonSafe);
    }

    /**
     * Creates the new candidate rules, by adding on possible literal to the current rule's body. The current rule
     * is represented by the head and body parameters.
     * <p>
     * In addition, it skips equivalents sets, by checking if the free variables at the candidate atom can be renamed
     * to match the free variables of a previously selected one. If a equivalent atom {@code A} is detected, the
     * substitution map that makes it equals to another a previous atom {@code B} is stored along with {@code B}. In
     * this case, when a rule from a set of candidates is selected for further refinements, it stores a substitution map
     * that, if applied to the candidates, makes the relevants atoms of discarded equivalent atoms, also relevant to
     * the selected rule.
     *
     * @param initialClause  the initial clause
     * @param candidates     the {@link Iterable} of candidates
     * @param answerLiterals the set of candidate literals as the answer of the method
     * @param skipCandidates the atoms to skip
     * @param connected      if {@code true} only literals connected to the rules will be returned.
     */
    @SuppressWarnings("MethodWithTooManyParameters")
    public static void buildAllLiteralFromClause(HornClause initialClause, Iterable<Literal> candidates,
                                                 Set<Literal> answerLiterals, Set<EquivalentAtom> skipCandidates,
                                                 boolean connected) {
        Set<Term> fixedTerms;           // the terms already in the rule, to be ignored in the equivalent evaluation
        EquivalentAtom currentAtom;     // the current atom
        Atom head = initialClause.getHead();
        fixedTerms = initialClause.getBody().stream().flatMap(l -> l.getTerms().stream()).collect(Collectors.toSet());
        fixedTerms.addAll(head.getTerms());

        for (Literal candidate : candidates) {
            if (connected && Collections.disjoint(fixedTerms, candidate.getTerms())) { continue; }
            currentAtom = new EquivalentAtom(candidate, fixedTerms);
            if (!skipCandidates.contains(currentAtom)) {
                skipCandidates.add(currentAtom);
                answerLiterals.add(candidate);
            }
        }
    }

    /**
     * Creates the clause to find the substitution of variable instantiated by the initial clause.
     *
     * @param initialClause the initial clause
     * @return the clause to find the substitutions
     */
    public static HornClause buildSubstitutionClause(HornClause initialClause) {
        List<Term> terms = new ArrayList<>();
        appendVariablesFromAtom(initialClause.getHead(), terms);

        Conjunction body;
        if (initialClause.getBody().isEmpty()) {
            body = new Conjunction(Literal.TRUE_LITERAL);
        } else {
            for (Literal literal : initialClause.getBody()) {
                appendVariablesFromAtom(literal, terms);
            }
            body = initialClause.getBody();
        }

        return new HornClause(new Atom(SUBSTITUTION_PREDICATE, terms), body);
    }

    /**
     * Appends the variables from the atom to the append set.
     *
     * @param atom   the atom
     * @param append the append set
     */
    protected static void appendVariablesFromAtom(Atom atom, List<Term> append) {
        for (Term term : atom.getTerms()) {
            if (!term.isConstant() && !append.contains(term)) {
                append.add(term);
            }
        }
    }

    /**
     * Builds the queries from the positive examples to make possible find the substitution of each proved example.
     *
     * @param examples          the examples
     * @param initialClauseHead the initial clause head
     * @param substitutionHead  the substitution clause head
     * @param positiveOnly      if {@code true} only positive examples will be considered
     * @return the queries of examples
     */
    public static Set<Example> buildQueriesFromExamples(Iterable<? extends Example> examples,
                                                        Atom initialClauseHead, Atom substitutionHead,
                                                        boolean positiveOnly) {
        Set<Example> querySet = new HashSet<>();
        for (Example example : examples) {
            if (!LanguageUtils.isAtomUnifiableToGoal(example.getGoalQuery(), initialClauseHead)) { continue; }
            for (AtomExample atomExample : example.getGroundedQuery()) {
                if (positiveOnly && !atomExample.isPositive()) { continue; }
                querySet.add(buildQueryFromExample(substitutionHead, atomExample));
            }
        }
        return querySet;
    }

    /**
     * Builds a query from the example, in order to find the substitution map for all variables in the initial clause
     * to the constants that satisfies the example.
     *
     * @param head    the head of the initial clause
     * @param example the example
     * @return the query to find the substitutions
     */
    protected static Example buildQueryFromExample(Atom head, AtomExample example) {
        List<Term> terms = new ArrayList<>(head.getArity());
        int i;
        for (i = 0; i < example.getArity(); i++) {
            terms.add(example.getTerms().get(i));
        }
        for (; i < head.getArity(); i++) {
            terms.add(head.getTerms().get(i));
        }
        return new AtomExample(SUBSTITUTION_PREDICATE, terms, example.isPositive());
    }

    /**
     * Finds the index in the list where is the biggest gap between the evaluation of the function in the elements.
     *
     * @param elements the list of elements, must be sorted
     * @param function the evaluation function
     * @return the index of the biggest gap, i.e. the number of elements that the first list must have to break in
     * the biggest gap
     */
    public static <E> int findBiggestGap(List<E> elements, Function<? super E, Double> function) {
        final int size = elements.size() - 1;
        int maxIndex = size;
        double maxValue = 0;
        double auxiliary;
        for (int i = 0; i < size; i++) {
            auxiliary = Math.abs(function.apply(elements.get(i)) - function.apply(elements.get(i + 1)));
            if (auxiliary > maxValue) {
                maxValue = auxiliary;
                maxIndex = i;
            }
        }

        return maxIndex + 1;
    }

}
