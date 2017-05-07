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

import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.ProPprExample;
import br.ufrj.cos.logic.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Class to centralize useful method with respect with to the logic language.
 * <p>
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class LanguageUtils {

    /**
     * The default encode of the input.
     */
    public static final String DEFAULT_INPUT_ENCODE = "UTF8";

    /**
     * The predicate opening argument character.
     */
    public static final String PREDICATE_OPEN_ARGUMENT_CHARACTER = "(";
    /**
     * The predicate closing argument character.
     */
    public static final String PREDICATE_CLOSE_ARGUMENT_CHARACTER = ")";

    /**
     * The list separator character.
     */
    public static final String LIST_ARGUMENTS_SEPARATOR = ", ";

    /**
     * The end of line/clause character.
     */
    public static final String CLAUSE_END_OF_LINE = ".";

    /**
     * The implication sign.
     */
    public static final String IMPLICATION_SIGN = ":-";

    /**
     * The negation prefix.
     */
    public static final String NEGATION_PREFIX = "not";

    /**
     * The weight sign.
     */
    public static final String WEIGHT_SIGN = "::";

    /**
     * The feature opening character.
     */
    public static final String FEATURES_OPEN_ARGUMENT_CHARACTER = "{";
    /**
     * The feature closing character.
     */
    public static final String FEATURES_CLOSE_ARGUMENT_CHARACTER = "}";

    /**
     * The constant surrounding character, to allow special characters within the constant.
     */
    public static final String CONSTANT_SURROUNDING_CHARACTER = "\"";
    /**
     * The positive iterator sign.
     */
    public static final String POSITIVE_EXAMPLE_SIGN = "+";
    /**
     * The negative iterator sign.
     */
    public static final String NEGATIVE_EXAMPLE_SIGN = "-";

    /**
     * The iterator separator character.
     */
    public static final String EXAMPLE_SEPARATOR_CHARACTER = "\t";
    /**
     * The pattern of simple constant, the ones without the {@link #CONSTANT_SURROUNDING_CHARACTER}.
     */
    public static final Pattern SIMPLE_CONSTANT_PATTERN = Pattern.compile("[a-z][a-zA-Z0-9_]*");

    /**
     * Checks if the name can be a simple constant ou need the {@link #CONSTANT_SURROUNDING_CHARACTER}, i.e. there is
     * no special characters.
     *
     * @param name the name
     * @return {@code true} if it is a simple constant name, {@code false} otherwise
     */
    public static boolean doesNameContainsSpecialCharacters(String name) {
        return !SIMPLE_CONSTANT_PATTERN.matcher(name).matches();
    }

    /**
     * Surrounds the constant name with the {@link #CONSTANT_SURROUNDING_CHARACTER}.
     *
     * @param name the name
     * @return the surrounded name
     */
    public static String surroundConstant(String name) {
        return CONSTANT_SURROUNDING_CHARACTER + name + CONSTANT_SURROUNDING_CHARACTER;
    }

    /**
     * Formats a {@link HornClause} to {@link String}.
     *
     * @param hornClause the {@link HornClause}
     * @return the formatted {@link String}
     */
    public static String formatHornClause(HornClause hornClause) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(hornClause.getHead());
        stringBuilder.append(" ");
        stringBuilder.append(IMPLICATION_SIGN);
        stringBuilder.append(" ");
        Conjunction body = hornClause.getBody();
        if (body != null && !body.isEmpty()) {
            stringBuilder.append(body.toString());
        } else {
            stringBuilder.append(Atom.TRUE_ATOM.toString());
        }
        stringBuilder.append(CLAUSE_END_OF_LINE);

        return stringBuilder.toString();
    }

    /**
     * Formats the {@link ProPprExample} to {@link String}.
     *
     * @param proPprExample the{@link ProPprExample}
     * @return the formatted {@link String}
     */
    public static String formatExampleToProPprString(ProPprExample proPprExample) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(proPprExample.getGoal());
        for (AtomExample atomExample : proPprExample.getAtomExamples()) {
            stringBuilder.append(EXAMPLE_SEPARATOR_CHARACTER);
            stringBuilder.append(formatExampleToProPprString(atomExample));
        }

        return stringBuilder.toString();
    }

    /**
     * Formats the {@link AtomExample} to {@link String} in the ProPPR format.
     *
     * @param atomExample the {@link AtomExample}
     * @return the formatted {@link String}
     */
    public static String formatExampleToProPprString(AtomExample atomExample) {
        return (atomExample.isPositive() ? POSITIVE_EXAMPLE_SIGN : NEGATIVE_EXAMPLE_SIGN) + formatAtomToString
                (atomExample);
    }

    /**
     * Formats the {@link Atom} to {@link String}.
     *
     * @param atom the {@link Atom}
     * @return the formatted {@link String}
     */
    public static String formatAtomToString(Atom atom) {
        List<Term> terms = atom.getTerms();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(atom.getName());
        if (terms != null && !terms.isEmpty()) {
            stringBuilder.append(PREDICATE_OPEN_ARGUMENT_CHARACTER);
            stringBuilder.append(listToString(terms));
            stringBuilder.append(PREDICATE_CLOSE_ARGUMENT_CHARACTER);
        }

        return stringBuilder.toString().trim();
    }

    /**
     * Formats a {@link Iterable} of {@link Object}s to {@link String}, separated by
     * {@link #LIST_ARGUMENTS_SEPARATOR}. It calls the {@link #toString} method for each {@link Object}.
     *
     * @param objects the {@link Iterable} of {@link Object}s
     * @return the formatted {@link String}
     */
    public static String listToString(Iterable<?> objects) {
        StringBuilder stringBuilder = new StringBuilder();
        objects.forEach(o -> stringBuilder.append(o.toString()).append(LIST_ARGUMENTS_SEPARATOR));
        stringBuilder.delete(stringBuilder.length() - LIST_ARGUMENTS_SEPARATOR.length(), stringBuilder.length());
        return stringBuilder.toString().trim();
    }

    /**
     * Builds an array of {@link String} by calling the {@link #toString()} method of each object in objects.
     *
     * @param objects the objects
     * @return the array of {@link String}
     */
    public static String[] toStringCollectionToArray(Collection<?> objects) {
        String[] strings = new String[objects.size()];
        int counter = 0;
        for (Object object : objects) {
            strings[counter] = object.toString();
            counter++;
        }
        return strings;
    }

    /**
     * Turn into variable the {@link Term}s of the given {@link Atom}
     *
     * @param atom        the {@link Atom}
     * @param variableMap the {@link Map} of {@link Term}s to {@link Variable}s
     * @param generator   the {@link VariableGenerator}
     * @return the new {@link Atom}
     * @throws IllegalAccessException if an error occurs when instantiating a new list of {@link Term}s
     * @throws InstantiationException if an error occurs when instantiating a new list of {@link Term}s
     */
    public static Atom toVariableAtom(Atom atom, Map<Term, Variable> variableMap,
                                      VariableGenerator generator) throws IllegalAccessException,
            InstantiationException {
        List<Term> terms = atom.getTerms().getClass().newInstance();
        for (Term term : atom.getTerms()) {
            terms.add(variableMap.computeIfAbsent(term, k -> generator.next()));
        }

        return new Atom(atom.getName(), terms);
    }

    /**
     * Checks if the given atom unifies with the goal. An atom unifies with the goal if exists a
     * substitution of variables that makes the atom equals to the goal.
     *
     * @param atom the atom
     * @param goal the goal
     * @return {@code true} if the atom unifies, {@code false} otherwise
     */
    public static boolean isAtomUnifiableToGoal(Atom atom, Atom goal) {
        return unifyAtomToGoal(atom, goal) != null;
    }

    /**
     * Unifies the given goal to the given atom and returns the substitution {@link Map} of the {@link Term}s. If the
     * unification is not possibly, returns null.
     *
     * @param atom the atom
     * @param goal the goal
     * @return a substitution {@link Map} of the {@link Term}s if the unification is possible, {@code null} otherwise
     */
    public static Map<Term, Term> unifyAtomToGoal(Atom atom, Atom goal) {
        if (!goal.getName().equals(atom.getName())) {
            // different predicate name, is not unifiable
            return null;
        }
        if (goal.getTerms().size() != atom.getTerms().size()) {
            // different predicate size, is not unifiable
            return null;
        }

        Term goalTerm;
        Term atomTerm;
        Map<Term, Term> variableMap = new HashMap<>();
        for (int i = 0; i < goal.getTerms().size(); i++) {
            goalTerm = goal.getTerms().get(i);
            atomTerm = atom.getTerms().get(i);
            if (goalTerm.isConstant()) {
                // the goal's term is a constant, the atom term must match exactly
                if (!goalTerm.equals(atomTerm)) {
                    return null;
                }
            } else {
                // the goal's term is a variable
                if (variableMap.containsKey(atomTerm)) {
                    // the atom's term has been already mapped to another term
                    // the mapped term must match the goal's term exactly
                    if (!goalTerm.equals(variableMap.get(atomTerm))) {
                        return null;
                    }
                } else {
                    // the atom's term has not yet been mapped, map to the goal's variable
                    variableMap.put(atomTerm, goalTerm);
                }
            }
        }

        return variableMap;
    }
}
