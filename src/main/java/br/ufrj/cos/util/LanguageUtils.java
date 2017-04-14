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

import br.ufrj.cos.language.Atom;
import br.ufrj.cos.language.HornClause;
import br.ufrj.cos.language.Term;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class LanguageUtils {

    public static final String PREDICATE_OPEN_ARGUMENT_CHARACTER = "(";
    public static final String PREDICATE_CLOSE_ARGUMENT_CHARACTER = ")";
    public static final String LIST_ARGUMENTS_SEPARATOR = ", ";
    public static final String CLAUSE_END_OF_LINE = ".";
    public static final String CONSTANT_SURROUNDING_CHARACTER = "\"";
    public static final String IMPLICATION_SIGN = ":-";
    public static final String NEGATION_PREFIX = "not";
    public static final String WEIGHT_SIGN = "::";
    public static final String FEATURES_OPEN_ARGUMENT_CHARACTER = "{";
    public static final String FEATURES_CLOSE_ARGUMENT_CHARACTER = "}";
    public static Pattern SIMPLE_CONSTANT_PATTERN = Pattern.compile("[a-z][a-zA-Z0-9_]*");

    public static String formatAtomToString(Atom atom) {
        List<Term> terms = atom.getTerms();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(atom.getName());
        if (terms != null && !terms.isEmpty()) {
            stringBuilder.append(PREDICATE_OPEN_ARGUMENT_CHARACTER);
            stringBuilder.append(getListToString(terms));
            stringBuilder.append(PREDICATE_CLOSE_ARGUMENT_CHARACTER);
        }

        return stringBuilder.toString().trim();
    }

    public static String getListToString(List<?> objects) {
        StringBuilder stringBuilder = new StringBuilder();
        objects.forEach(o -> stringBuilder.append(o.toString()).append(LIST_ARGUMENTS_SEPARATOR));
        stringBuilder.delete(stringBuilder.length() - LIST_ARGUMENTS_SEPARATOR.length(), stringBuilder.length());
        return stringBuilder.toString().trim();
    }

    public static boolean doesNameContainsSpecialCharacters(String name) {
        return !SIMPLE_CONSTANT_PATTERN.matcher(name).matches();
    }

    public static String surroundConstant(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CONSTANT_SURROUNDING_CHARACTER);
        stringBuilder.append(name);
        stringBuilder.append(CONSTANT_SURROUNDING_CHARACTER);
        return stringBuilder.toString().trim();
    }

    public static String formatHornClause(HornClause hornClause) {
        return hornClause.getHead() + " " + IMPLICATION_SIGN +
                " " + hornClause.getBody().toString() + CLAUSE_END_OF_LINE;
    }

}
