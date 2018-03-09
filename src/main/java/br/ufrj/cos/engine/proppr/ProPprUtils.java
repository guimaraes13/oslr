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

package br.ufrj.cos.engine.proppr;

import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.logic.*;
import br.ufrj.cos.util.LanguageUtils;
import edu.cmu.ml.proppr.prove.wam.*;

import java.util.*;

/**
 * Centralizes the utility methods to deal with ProPPR theories.
 * <p>
 * Created on 14/10/2017.
 *
 * @author Victor Guimarães
 */
public final class ProPprUtils {

    /**
     * The default empty goal array.
     */
    public static final Goal[] EMPTY_GOAL = new Goal[0];
    /**
     * The default feature literal prefix.
     */
    public static final String DEFAULT_FEATURE_LITERAL_PREFIX = "l_";

    private ProPprUtils() {
    }

    /**
     * Compiles a {@link Theory} into a {@link WamProgram}, the internal representation of ProPRR.
     *
     * @param theory the {@link Theory}
     * @return the {@link WamProgram}
     */
    protected static WamProgram compileTheory(Iterable<? extends HornClause> theory) {
        WamProgram wamProgram = new WamBaseProgram();
        appendRuleToProgram(theory, wamProgram);
        wamProgram.save();
        return wamProgram;
    }

    /**
     * Appends the {@link HornClause}s to the {@link WamProgram}.
     *
     * @param hornClauses the {@link HornClause}s
     * @param wamProgram  the {@link WamProgram}
     * @return the feature rules generates by the theory
     */
    protected static Collection<Rule> appendRuleToProgram(Iterable<? extends HornClause> hornClauses,
                                                          WamProgram wamProgram) {

        Collection<Rule> featureRules = new HashSet<>();
        for (HornClause hornClause : hornClauses) {
            final Rule rule = clauseToRule(hornClause, featureRules);
            rule.variabilize();
            wamProgram.append(new Instruction(Instruction.OP.comment, rule.toString()));
            wamProgram.insertLabel(getLabelForRule(rule));
            wamProgram.append(rule);
        }

        for (Rule rule : featureRules) {
            rule.variabilize();
            wamProgram.append(new Instruction(Instruction.OP.comment, rule.toString()));
            wamProgram.insertLabel(getLabelForRule(rule));
            wamProgram.append(rule);
        }

        return featureRules;
    }

    /**
     * Converts a {@link Clause}, from the system representation, to a {@link Rule} in ProPPR's representation.
     *
     * @param clause       the {@link Clause}
     * @param featureRules the possible feature rules from the clause
     * @return the {@link Rule}
     */
    protected static Rule clauseToRule(Clause clause, Collection<Rule> featureRules) {
        Goal lhs;                      //head
        Goal[] rhs = EMPTY_GOAL;       //body
        Goal[] features = EMPTY_GOAL;  //features
        Rule outputRule = null;
        if (clause instanceof Atom) {
            lhs = atomToGoal((Atom) clause, new HashMap<>());
            outputRule = new Rule(lhs, rhs, features, EMPTY_GOAL);
        } else if (clause instanceof HornClause) {
            HornClause hornClause = (HornClause) clause;
            Map<Term, Integer> variableMap = new HashMap<>();
            lhs = atomToGoal(hornClause.getHead(), variableMap);
            rhs = positiveLiteralsToGoals(hornClause.getBody(), variableMap);
            if (hornClause instanceof FeaturedClause) {
                FeaturedClause featuredClause = (FeaturedClause) hornClause;
                features = atomsToGoals(featuredClause.getFeatures(), variableMap);
                outputRule = buildFeatureRules(lhs, rhs, features, featureRules);
            } else {
                features = new Goal[]{new Goal(hornClause.getHead().getName() + hornClause.getBody().size())};
                outputRule = new Rule(lhs, rhs, features, EMPTY_GOAL);
            }
        }
        return outputRule;
    }

    /**
     * Converts a {@link Clause}, from the system representation, to a {@link Rule} in ProPPR's representation.
     *
     * @param clause the {@link Clause}
     * @return the {@link Rule}
     */
    protected static Rule clauseToRule(Clause clause) {
        Goal lhs = null;               //head
        Goal[] rhs = EMPTY_GOAL;       //body
        Goal[] features = EMPTY_GOAL;  //features
        if (clause instanceof Atom) {
            lhs = atomToGoal((Atom) clause, new HashMap<>());
        } else if (clause instanceof HornClause) {
            HornClause hornClause = (HornClause) clause;
            Map<Term, Integer> variableMap = new HashMap<>();
            lhs = atomToGoal(hornClause.getHead(), variableMap);
            rhs = positiveLiteralsToGoals(hornClause.getBody(), variableMap);
            if (hornClause instanceof FeaturedClause) {
                FeaturedClause featuredClause = (FeaturedClause) hornClause;
                features = atomsToGoals(featuredClause.getFeatures(), variableMap);
            }
        }

        return new Rule(lhs, rhs, features, EMPTY_GOAL);
    }

    /**
     * Given a primary rule with variable features, modifies this rule by putting variable features into their body
     * and creates the feature rules to the theory.
     *
     * @param head     the head of the primary rule
     * @param rhs      the body of the primary rule
     * @param features the features of the primary rule
     * @param rules    the output features rules
     * @return the modified primary rule.
     */
    protected static Rule buildFeatureRules(Goal head, Goal[] rhs, Goal[] features, Collection<Rule> rules) {
        Goal[] propositionalFeatures = new Goal[features.length];
        final List<Goal> bodyFeatures = new ArrayList<>();
        for (int i = 0; i < features.length; i++) {
            if (features[i].getArity() == 0) {
                propositionalFeatures[i] = features[i];
            } else {
                propositionalFeatures[i] = new Goal(features[i].getFunctor());
                final Goal literalGoal = new Goal(DEFAULT_FEATURE_LITERAL_PREFIX + features[i].getFunctor(),
                                                  features[i].getArgs());
                bodyFeatures.add(literalGoal);
                rules.add(new Rule(literalGoal, EMPTY_GOAL, new Goal[]{features[i]}, EMPTY_GOAL));
            }
        }
        Goal[] body = new Goal[rhs.length + bodyFeatures.size()];
        System.arraycopy(rhs, 0, body, 0, rhs.length);
        for (int i = 0; i < bodyFeatures.size(); i++) { body[rhs.length + i] = bodyFeatures.get(i); }
        return new Rule(head, body, propositionalFeatures, EMPTY_GOAL);
    }

    /**
     * Gets the label for the {@link Rule}, so it can be proper added to the {@link WamProgram}.
     *
     * @param rule the {@link Rule}
     * @return the label
     */
    protected static String getLabelForRule(Rule rule) {
        return rule.getLhs().getFunctor() + LanguageUtils.PREDICATE_ARITY_SEPARATOR + rule.getLhs().getArity();
    }

    /**
     * Converts an {@link Iterable} of {@link Literal}s, from the system representation, to {@link Goal}s, from ProPPR's
     * representation; Using a variableMap to map variable names into int number. In addition, filters the negated
     * {@link Literal}s, converting only the non-negated ones. If two variable in a {@link Clause} represents the
     * same logic variable, they must be mapped to the same number, so, use the same variableMap to the whole
     * {@link Clause}.
     *
     * @param literals    {@link Iterable} of {@link Literal}s
     * @param variableMap the variable {@link Map}
     * @return the {@link Goal}s
     */
    protected static Goal[] positiveLiteralsToGoals(Iterable<? extends Literal> literals,
                                                    Map<Term, Integer> variableMap) {
        List<Goal> goals = new ArrayList<>();
        for (Literal literal : literals) {
            if (!literal.isNegated()) {
                goals.add(atomToGoal(literal, variableMap));
            }
        }

        return goals.toArray(EMPTY_GOAL);
    }

    /**
     * Converts an {@link Iterable} of {@link Atom}s, from the system representation, to {@link Goal}s, from ProPPR's
     * representation; Using a variableMap to map variable names into int number. If two variable in a {@link Clause}
     * represents the same logic variable, they must be mapped to the same number, so, use the same variableMap to
     * the whole {@link Clause}.
     *
     * @param atoms       {@link Iterable} of {@link Atom}s
     * @param variableMap the variable {@link Map}
     * @return the {@link Goal}s
     */
    protected static Goal[] atomsToGoals(Iterable<? extends Atom> atoms, Map<Term, Integer> variableMap) {
        List<Goal> goals = new ArrayList<>();
        for (Atom atom : atoms) {
            goals.add(atomToGoal(atom, variableMap));
        }

        return goals.toArray(EMPTY_GOAL);
    }

    /**
     * Converts an {@link Atom}, from the system representation, to a {@link Goal}, from ProPPR's representation;
     * Using a variableMap to map variable names into int number. If two variable in a {@link Clause} represents the
     * same logic variable, they must be mapped to the same number, so, use the same variableMap to the whole
     * {@link Clause}.
     *
     * @param atom        the {@link Atom}
     * @param variableMap the variable {@link Map}
     * @return the {@link Goal}
     */
    protected static Goal atomToGoal(Atom atom, Map<Term, Integer> variableMap) {
        return atomToGoal(atom.getName(), atom.getTerms(), variableMap);
    }

    /**
     * Converts an {@link Atom}, from the system representation, to a {@link Goal}, from ProPPR's representation;
     * Using a variableMap to map variable names into int number. If two variable in a {@link Clause} represents the
     * same logic variable, they must be mapped to the same number, so, use the same variableMap to the whole
     * {@link Clause}.
     *
     * @param name        the {@link Atom}'s name
     * @param terms       the {@link Atom}'s {@link Term}s
     * @param variableMap the variable {@link Map}
     * @return the {@link Goal}
     */
    protected static Goal atomToGoal(String name, List<Term> terms, Map<Term, Integer> variableMap) {
        if (terms == null || terms.isEmpty()) { return new Goal(name); }
        Argument[] arguments = new Argument[terms.size()];
        for (int i = 0; i < arguments.length; i++) {
            if (terms.get(i).isConstant()) {
                arguments[i] = new ConstantArgument(terms.get(i).getName());
            } else {
                if (variableMap == null) {
                    arguments[i] = new VariableArgument(i + 1);
                } else {
                    arguments[i] = new VariableArgument(variableMap.computeIfAbsent(terms.get(i), k ->
                            variableMap.size() + 1));
                }
            }
        }

        return new Goal(name, arguments);
    }

    /**
     * Compiles a {@link Theory} into a {@link WamProgram}, the internal representation of ProPRR.
     *
     * @param theory       the {@link Theory}
     * @param featureRules the collection to append the feature rules generated by the theory.
     * @return the {@link WamProgram}
     */
    protected static WamProgram compileTheory(Iterable<? extends HornClause> theory, Collection<Rule> featureRules) {
        WamProgram wamProgram = new WamBaseProgram();
        final Collection<Rule> rules = appendRuleToProgram(theory, wamProgram);
        wamProgram.save();
        featureRules.addAll(rules);
        return wamProgram;
    }

}
