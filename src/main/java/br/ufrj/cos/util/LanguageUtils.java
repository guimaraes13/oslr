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

import br.ufrj.cos.engine.EngineSystemTranslator;
import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.ProPprExample;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.logic.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Class to centralize useful method with respect with to the logic language.
 * <p>
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public final class LanguageUtils {

    /**
     * ProbLog example predicate name.
     */
    public static final String PROBLOG_EXAMPLE_PREDICATE = "evidence";
    /**
     * The ProbLog positive examples flag.
     */
    public static final String PROBLOG_POSITIVE_EXAMPLE_FLAG = "true";
    /**
     * The ProbLog negative examples flag.
     */
    public static final String PROBLOG_NEGATIVE_EXAMPLE_FLAG = "false";
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
     * The negation PREFIX.
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
     * Pattern to simplify the class name keeping only the upper case letters.
     */
    public static final String SIMPLE_CLASS_NAME_PATTERN = "(.*\\.|[a-z]*)";
    /**
     * The name formatter separator.
     */
    public static final String NAME_FORMATTER_SEPARATOR = "_";
    /**
     * Arguments separator.
     */
    public static final String ARGUMENTS_SEPARATOR = " ";
    /**
     * The parameter mark from the string's format
     */
    public static final String STRING_PARAMETER_MARK = "%s";
    /**
     * The character to separate the predicate name from the ist arity.
     */
    public static final String PREDICATE_ARITY_SEPARATOR = "/";
    /**
     * The true predicate to simulate the true boolean value.
     */
    public static final String TRUE_ARGUMENT = "true";
    /**
     * The true argument to simulate the true boolean value.
     */
    public static final Predicate TRUE_PREDICATE = new Predicate(TRUE_ARGUMENT, 1);
    /**
     * The arity of the true and false predicates.
     */
    public static final int PREDICATE_ARITY = 1;
    /**
     * The tabulation size.
     */
    public static final int TABULATION_SIZE = 4;
    /**
     * The false argument to simulate the false boolean value.
     */
    private static final String FALSE_ARGUMENT = "false";
    /**
     * The false predicate to simulate the false boolean value.
     */
    public static final Predicate FALSE_PREDICATE = new Predicate(FALSE_ARGUMENT, PREDICATE_ARITY);
    private static final Pattern COMPILED_SIMPLE_CLASS_NAME_PATTERN = Pattern.compile(SIMPLE_CLASS_NAME_PATTERN);

    private LanguageUtils() {
    }

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
        return formatHornClause(hornClause.getHead(), hornClause.getBody());
    }

    /**
     * Formats a {@link HornClause} to {@link String}.
     *
     * @param head the {@link HornClause}'s head
     * @param body the {@link HornClause}'s body
     * @return the formatted {@link String}
     */
    public static String formatHornClause(Atom head, Collection<?> body) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(head);
        stringBuilder.append(" ");
        stringBuilder.append(IMPLICATION_SIGN);
        stringBuilder.append(" ");
        if (body != null && !body.isEmpty()) {
            stringBuilder.append(LanguageUtils.iterableToString(body));
        } else {
            stringBuilder.append(Literal.TRUE_LITERAL);
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
     * Formats the {@link AtomExample} to {@link String} in the ProbLog format.
     *
     * @param atomExample the {@link AtomExample}
     * @return the formatted {@link String}
     */
    public static String formatExampleToProbLogString(AtomExample atomExample) {
        return PROBLOG_EXAMPLE_PREDICATE +
                PREDICATE_OPEN_ARGUMENT_CHARACTER +
                formatAtomToString(atomExample) +
                LIST_ARGUMENTS_SEPARATOR +
                (atomExample.isPositive() ? PROBLOG_POSITIVE_EXAMPLE_FLAG : PROBLOG_NEGATIVE_EXAMPLE_FLAG) +
                PREDICATE_CLOSE_ARGUMENT_CHARACTER +
                CLAUSE_END_OF_LINE;
    }

    /**
     * Formats a {@link Iterable} of {@link Object}s to {@link String}, separated by
     * {@link #LIST_ARGUMENTS_SEPARATOR}. It calls the {@link #toString} method for each {@link Object}.
     *
     * @param objects the {@link Iterable} of {@link Object}s
     * @return the formatted {@link String}
     */
    public static String iterableToString(Iterable<?> objects) {
        StringBuilder stringBuilder = new StringBuilder();
        objects.forEach(o -> stringBuilder.append(o).append(LIST_ARGUMENTS_SEPARATOR));
        stringBuilder.delete(stringBuilder.length() - LIST_ARGUMENTS_SEPARATOR.length(), stringBuilder.length());
        return stringBuilder.toString().trim();
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
            stringBuilder.append(iterableToString(terms));
            stringBuilder.append(PREDICATE_CLOSE_ARGUMENT_CHARACTER);
        }

        return stringBuilder.toString().trim();
    }

    /**
     * Formats the {@link Atom} to a fact {@link String}.
     *
     * @param atom the {@link Atom}
     * @return the formatted {@link String}
     */
    public static String formatFactToString(Atom atom) {
        List<Term> terms = atom.getTerms();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(atom.getName());
        if (terms != null && !terms.isEmpty()) {
            stringBuilder.append(PREDICATE_OPEN_ARGUMENT_CHARACTER);
            stringBuilder.append(iterableToString(terms));
            stringBuilder.append(PREDICATE_CLOSE_ARGUMENT_CHARACTER);
        }
        stringBuilder.append(CLAUSE_END_OF_LINE);
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
    public static Atom toVariableAtom(Atom atom, Map<Term, Term> variableMap,
                                      VariableGenerator generator) throws IllegalAccessException,
            InstantiationException {
        List<Term> terms = atom.getTerms().getClass().newInstance();
        for (Term term : atom.getTerms()) {
            if (term.isConstant()) {
                terms.add(variableMap.computeIfAbsent(term, k -> generator.next()));
            } else {
                terms.add(term);
            }
        }

        return new Atom(atom.getPredicate(), terms);
    }

    /**
     * Turn into variable the {@link Term}s of the given {@link Atom} and returns as {@link Literal}.
     *
     * @param atom        the {@link Atom}
     * @param variableMap the {@link Map} of {@link Term}s to {@link Variable}s
     * @param generator   the {@link VariableGenerator}
     * @return the new {@link Literal}
     * @throws IllegalAccessException if an error occurs when instantiating a new list of {@link Term}s
     * @throws InstantiationException if an error occurs when instantiating a new list of {@link Term}s
     */
    public static Literal toVariableLiteral(Atom atom, Map<Term, Term> variableMap,
                                            VariableGenerator generator) throws IllegalAccessException,
            InstantiationException {
        List<Term> terms = atom.getTerms().getClass().newInstance();
        for (Term term : atom.getTerms()) {
            if (term.isConstant()) {
                terms.add(variableMap.computeIfAbsent(term, k -> generator.next()));
            } else {
                terms.add(term);
            }
        }

        return new Literal(atom.getPredicate(), terms);
    }

    /**
     * Creates an {@link Atom} to be the root of a new tree.
     *
     * @param atom the {@link Atom}
     * @return the {@link Atom}
     */
    public static Atom toVariableAtom(Atom atom) {
        return toVariableAtom(atom.getPredicate());
    }

    /**
     * Creates an {@link Atom} to be the root of a new tree.
     *
     * @param predicate the {@link Atom}'s predicate
     * @return the {@link Atom}
     */
    public static Atom toVariableAtom(Predicate predicate) {
        List<Term> terms = new ArrayList<>(predicate.getArity());
        VariableGenerator variableGenerator = new VariableGenerator();
        for (int i = 0; i < predicate.getArity(); i++) {
            terms.add(variableGenerator.next());
        }

        return new Atom(predicate, terms);
    }

    /**
     * Checks if the given atom unifies with the goal. An atom unifies with the goal if exists a
     * substitution of variables that makes the atom equals to the goal.
     * <p>
     * This method is not symmetric, i.e. {@code isAtomUnifiableToGoal(a, b) != isAtomUnifiableToGoal(b, a)}.
     *
     * @param atom the atom
     * @param goal the goal
     * @return {@code true} if the atom unifies, {@code false} otherwise
     */
    public static boolean isAtomUnifiableToGoal(Atom atom, Atom goal) {
        return unifyAtomToGoal(atom, goal) != null;
    }

    /**
     * Unifies the given atom to the given goal and returns the substitution {@link Map} of the {@link Term}s. If the
     * unification is not possibly, returns null.
     *
     * @param atom the atom
     * @param goal the goal
     * @return a substitution {@link Map} of the {@link Term}s if the unification is possible, {@code null} otherwise
     */
    public static Map<Term, Term> unifyAtomToGoal(Atom atom, Atom goal) {
        return checkPredicates(goal, atom) ? getUnifyMap(atom, goal) : null;
    }

    /**
     * Checks if the predicates match in name and arity.
     *
     * @param goal the goal
     * @param atom the atom
     * @return {@code true} if they match, {@code false} otherwise
     */
    protected static boolean checkPredicates(Atom goal, Atom atom) {
        return goal.getName().equals(atom.getName()) && goal.getArity() == atom.getArity();
    }

    /**
     * Unifies the given atom to the given goal and returns the substitution {@link Map} of the {@link Term}s. If the
     * unification is not possibly, returns null.
     * <p>
     * This method is not symmetric, i.e. {@code isAtomUnifiableToGoal(a, b) != isAtomUnifiableToGoal(b, a)}.
     *
     * @param atom the atom
     * @param goal the goal
     * @return a substitution {@link Map} of the {@link Term}s if the unification is possible, {@code null} otherwise
     */
    public static Map<Term, Term> getUnifyMap(Atom atom, Atom goal) {
        return getUnifyMap(atom, goal, null);
    }

    /**
     * Unifies the given atom to the given goal and returns the substitution {@link Map} of the {@link Term}s. If the
     * unification is not possibly, returns null.
     * <p>
     * This method is not symmetric, i.e. {@code isAtomUnifiableToGoal(a, b) != isAtomUnifiableToGoal(b, a)}.
     *
     * @param atom       the atom
     * @param goal       the goal
     * @param fixedTerms {@link Term}s to be treated as constant (e.g. consolidated variables from the rule).
     * @return a substitution {@link Map} of the {@link Term}s if the unification is possible, {@code null} otherwise
     */
    public static Map<Term, Term> getUnifyMap(Atom atom, Atom goal, Set<Term> fixedTerms) {
        Term goalTerm;
        Term atomTerm;
        Map<Term, Term> variableMap = new HashMap<>();
        for (int i = 0; i < goal.getArity(); i++) {
            goalTerm = goal.getTerms().get(i);
            atomTerm = atom.getTerms().get(i);
            if (goalTerm.isConstant() || (fixedTerms != null && fixedTerms.contains(goalTerm))) {
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

    /**
     * Checks if the constants and variables from the goal match the ones from the atom.
     *
     * @param goal       the goal
     * @param atom       the atom
     * @param fixedTerms {@link Term}s to be treated as constant (e.g. consolidated variables from the rule)
     * @return the map to makes the goal equals to the atom, if the goad matches, or {@code null} otherwise
     */
    public static Map<Term, Term> doesTermMatch(Atom goal, Atom atom, Set<Term> fixedTerms) {
        if (checkPredicates(goal, atom) && getUnifyMap(atom, goal, fixedTerms) != null) {
            return getUnifyMap(goal, atom, fixedTerms);
        }

        return null;
    }

    /**
     * Applies the substitution of term in the {@link Literal} and returns a new one.
     *
     * @param literal      the {@link Literal}
     * @param substitution the substitution
     * @return the new {@link Literal} with the substituted terms
     */
    public static Literal applySubstitution(Literal literal, Map<Term, Term> substitution) {
        List<Term> terms = new ArrayList<>(literal.getArity());
        for (Term term : literal.getTerms()) {
            terms.add(substitution.getOrDefault(term, term));
        }

        return new Literal(literal.getPredicate(), terms, literal.isNegated());
    }

    /**
     * Substitutes the atom's variables, in place, using the substitution map.
     *
     * @param atom            the source to unify
     * @param substitutionMap the substitution map
     */
    public static void substituteVariablesInPlace(Atom atom, Map<Term, Term> substitutionMap) {
        if (substitutionMap == null) {
            return;
        }
        Term substitute;
        for (int i = 0; i < atom.getArity(); i++) {
            if (!atom.getTerms().get(i).isConstant()) { continue; }
            substitute = substitutionMap.get(atom.getTerms().get(i));
            if (substitute != null) { atom.getTerms().set(i, substitute); }
        }
    }

    /**
     * Creates a directory name by combining the upper case letters from the class name with the suffix, separated by
     * the {@link #NAME_FORMATTER_SEPARATOR}.
     *
     * @param object the object
     * @param suffix the suffix
     * @return the formatted name
     */
    public static String formatDirectoryName(Object object, String suffix) {
        String className = object.getClass().getName();
        className = formatClassName(className);

        return className + NAME_FORMATTER_SEPARATOR + (suffix == null ? "" : suffix);
    }

    /**
     * Formats the class name by getting only the upper case letters.
     *
     * @param className the class name
     * @return the formatted name
     */
    public static String formatClassName(String className) {
        return COMPILED_SIMPLE_CLASS_NAME_PATTERN.matcher(className).replaceAll("");
    }

    /**
     * Formats the class name by getting only the upper case letters.
     *
     * @param instance       the instance of the object
     * @param suppressSuffix suppresses the suffix of the formatted name
     * @return the formatted name
     */
    public static String formatClassName(Object instance, String suppressSuffix) {
        String className = formatClassName(instance.getClass().getSimpleName());
        if (className.endsWith(suppressSuffix)) {
            className = className.substring(0, className.length() - suppressSuffix.length());
        }
        return className;
    }

    /**
     * Formats the class name by getting only the upper case letters.
     *
     * @param instance the instance of the object
     * @return the formatted name
     */
    public static String formatClassName(Object instance) {
        return formatClassName(instance.getClass().getSimpleName());
    }

    /**
     * Writes the {@link Theory} to a {@link String}
     *
     * @param theory the {@link Theory}
     * @return the string
     */
    public static String theoryToString(Theory theory) {
        StringBuilder stringBuilder = new StringBuilder();
        for (HornClause clause : theory) {
            stringBuilder.append(clause).append("\n");
        }
        return stringBuilder.toString().trim();
    }

    /**
     * Gets the formatted predicate in the form p/n where p is the name of the predicate and n is the arity.
     *
     * @param name  the name
     * @param arity the arity
     * @return the predicate
     */
    public static String formatPredicate(String name, int arity) {
        return name + PREDICATE_ARITY_SEPARATOR + arity;
    }

    /**
     * Builds the logic true predicate.
     * <p>
     * Use this method to get the true predicate, do not try to creates it be yourself since it may be incompatible
     * to some {@link EngineSystemTranslator}.
     *
     * @return the logic true predicate
     * @see #buildFalseLiteral() for the false literal
     */
    public static Literal buildTrueLiteral() {
        List<Term> terms = new ArrayList<>(1);
        terms.add(new Constant(TRUE_ARGUMENT));
        return new Literal(TRUE_PREDICATE, terms);
    }

    /**
     * Builds the logic true predicate.
     * <p>
     * Use this method to get the true predicate, do not try to creates it be yourself since it may be incompatible
     * to some {@link EngineSystemTranslator}.
     *
     * @return the logic true predicate
     * @see #buildTrueLiteral() for the true literal
     */
    public static Literal buildFalseLiteral() {
        List<Term> terms = new ArrayList<>(1);
        terms.add(new Constant(FALSE_ARGUMENT));
        return new Literal(FALSE_PREDICATE, terms);
    }

    /**
     * Gets the right number of tabulations to format the log message
     *
     * @param name        the name to be in the log
     * @param maxNameSize the size of the longest name to be in the log
     * @return the right tabulations to be after the name
     */
    public static String getTabulation(String name, int maxNameSize) {
        return StringUtils.repeat("\t", Math.round((float) (maxNameSize - name.length() + 1) / TABULATION_SIZE) + 1);
    }

    /**
     * Splits the objects by some attribute
     *
     * @param collection the objects
     * @param appendMap  the map to append the objects
     * @param function   function to get the key from the object
     */
    public static <K, V> void splitAtomsByPredicate(Collection<? extends V> collection,
                                                    Map<K, Set<V>> appendMap, Function<V, K> function) {
        for (V v : collection) {
            appendMap.computeIfAbsent(function.apply(v), a -> new HashSet<>()).add(v);
        }
    }

}
