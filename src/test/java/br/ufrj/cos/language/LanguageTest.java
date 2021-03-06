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

package br.ufrj.cos.language;

import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.logic.*;
import br.ufrj.cos.util.LanguageUtils;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static br.ufrj.cos.util.LanguageUtils.*;

/**
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class LanguageTest {

    public static final String SIMPLE_CONSTANT_NAME = "simpleConstantName";
    public static final String SPECIAL_CONSTANT_NAME = "Constant Name With Special Characters: á, ß, ç.";
    public static final String SIMPLE_VARIABLE_NAME = "X1";
    public static final String ATOM_NAME = "atom";

    protected static Constant simpleConstant = new Constant(SIMPLE_CONSTANT_NAME);
    protected static Constant specialConstant = new Constant(SPECIAL_CONSTANT_NAME);

    protected static Variable simpleVariable = new Variable(SIMPLE_VARIABLE_NAME);

    protected static List<Term> terms = getTerms();

    public static List<Term> getTerms() {
        List<Term> terms = new ArrayList<>();
        terms.add(simpleConstant);
        terms.add(specialConstant);
        terms.add(simpleVariable);

        return terms;
    }

    @Test
    public void CONSTANT_NAME_FORMATTING_TEST() {
        Assert.assertEquals(SIMPLE_CONSTANT_NAME, simpleConstant.toString());
        Assert.assertEquals(LanguageUtils.surroundConstant(SPECIAL_CONSTANT_NAME), specialConstant.toString());
    }

    @Test
    public void ATOM_TEST() {
        Atom atom = new Atom(new Predicate(ATOM_NAME, terms.size()), terms);
        String atomString = getFormattedAtom(ATOM_NAME, terms);
        Assert.assertEquals(atomString, atom.toString());
    }

    protected String getFormattedHornClause(Atom head, Literal... body) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(head);
        stringBuilder.append(" ");
        stringBuilder.append(LanguageUtils.IMPLICATION_SIGN);
        stringBuilder.append(" ");
        for (Literal literal : body) {
            stringBuilder.append(literal);
            stringBuilder.append(LIST_ARGUMENTS_SEPARATOR);
        }
        stringBuilder.delete(stringBuilder.length() - LIST_ARGUMENTS_SEPARATOR.length(),
                             stringBuilder.length());

        stringBuilder.append(LanguageUtils.CLAUSE_END_OF_LINE);
        return stringBuilder.toString().trim();
    }

    @Test
    public void PROPOSITION_ATOM_TEST() {
        Atom atom = new Atom(new Predicate(ATOM_NAME));
        String atomString = getFormattedProposition(ATOM_NAME);
        Assert.assertEquals(atomString, atom.toString());
    }

    protected String getFormattedProposition(String atomName) {
        return String.format("%s", atomName);
    }

    @Test
    public void WEIGHTED_ATOM_TEST() {
        double weight = 0.8;
        Atom atom = new WeightedAtom(weight, new Predicate(ATOM_NAME, terms.size()), terms);
        String atomString = getWeightPrefix(weight) + getFormattedAtom(ATOM_NAME, terms);
        Assert.assertEquals(atomString, atom.toString());
    }

    protected String getWeightPrefix(double weight) {
        return weight + " " + LanguageUtils.WEIGHT_SIGN + " ";
    }

    @Test
    public void WEIGHTED_PROPOSITION_ATOM_TEST() {
        double weight = 0.8;
        Atom atom = new WeightedAtom(weight, new Predicate(ATOM_NAME));
        String atomString = getWeightPrefix(weight) + getFormattedProposition(ATOM_NAME);
        Assert.assertEquals(atomString, atom.toString());
    }

    @Test
    public void LITERAL_TEST() {
        Literal literal = new Literal(new Predicate(ATOM_NAME, terms.size()), terms, true);
        String literalString = LanguageUtils.NEGATION_PREFIX + " " + getFormattedAtom(ATOM_NAME, terms);
        Assert.assertEquals(literalString, literal.toString());
    }

    @Test
    public void PROPOSITION_LITERAL_TEST() {
        Literal literal = new Literal(new Predicate(ATOM_NAME), true);
        String literalString = LanguageUtils.NEGATION_PREFIX + " " + getFormattedProposition(ATOM_NAME);
        Assert.assertEquals(literalString, literal.toString());
    }

    @Test
    public void HORN_CLAUSE_TEST() {
        Atom head = new Atom(new Predicate(ATOM_NAME), terms);
        Literal literal1 = new Literal(new Predicate(ATOM_NAME, terms.size()), terms, true);
        Literal literal2 = new Literal(new Predicate(ATOM_NAME, terms.size()), terms, false);
        Conjunction body = new Conjunction(literal1, literal2);
        HornClause hornClause = new HornClause(head, body);

        String hornString = getFormattedHornClause(head, literal1, literal2);

        Assert.assertEquals(hornString, hornClause.toString());
    }

    @Test
    public void POSITIVE_EXAMPLE_TEST() {
        Atom atom = new Atom(new Predicate(ATOM_NAME, terms.size()), terms);
        String atomString = PROBLOG_EXAMPLE_PREDICATE +
                PREDICATE_OPEN_ARGUMENT_CHARACTER +
                getFormattedAtom(ATOM_NAME, terms) +
                LIST_ARGUMENTS_SEPARATOR +
                PROBLOG_POSITIVE_EXAMPLE_FLAG +
                PREDICATE_CLOSE_ARGUMENT_CHARACTER +
                CLAUSE_END_OF_LINE;
        AtomExample atomExample = new AtomExample(atom, true);
        Assert.assertEquals(atomString, atomExample.toString());
    }

    protected String getFormattedAtom(String atomName, List<Term> terms) {
        return String.format("%s%s%s%s",
                             atomName,
                             PREDICATE_OPEN_ARGUMENT_CHARACTER,
                             LanguageUtils.iterableToString(terms),
                             LanguageUtils.PREDICATE_CLOSE_ARGUMENT_CHARACTER);
    }

    @Test
    public void NEGATIVE_EXAMPLE_TEST() {
        Atom atom = new Atom(new Predicate(ATOM_NAME, terms.size()), terms);
        String atomString = PROBLOG_EXAMPLE_PREDICATE +
                PREDICATE_OPEN_ARGUMENT_CHARACTER +
                getFormattedAtom(ATOM_NAME, terms) +
                LIST_ARGUMENTS_SEPARATOR +
                PROBLOG_NEGATIVE_EXAMPLE_FLAG +
                PREDICATE_CLOSE_ARGUMENT_CHARACTER +
                CLAUSE_END_OF_LINE;
        AtomExample atomExample = new AtomExample(atom, false);
        Assert.assertEquals(atomString, atomExample.toString());
    }

    @Test
    public void WEIGHTED_HORN_CLAUSE_TEST() {
        double weight = 0.8;
        Atom head = new Atom(new Predicate(ATOM_NAME, terms.size()), terms);
        Literal literal1 = new Literal(new Predicate(ATOM_NAME, terms.size()), terms, true);
        Literal literal2 = new Literal(new Predicate(ATOM_NAME, terms.size()), terms, false);
        Conjunction body = new Conjunction(literal1, literal2);
        HornClause hornClause = new WeightedClause(weight, head, body);

        String hornString = getWeightPrefix(weight) + getFormattedHornClause(head, literal1, literal2);

        Assert.assertEquals(hornString, hornClause.toString());
    }

}
