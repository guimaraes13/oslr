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

package br.ufrj.cos.engine.proppr;

import br.ufrj.cos.engine.EngineSystemTranslator;
import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.ExampleSet;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.logic.*;
import br.ufrj.cos.util.LanguageUtils;
import edu.cmu.ml.proppr.prove.wam.*;
import edu.cmu.ml.proppr.prove.wam.plugins.FactsPlugin;
import edu.cmu.ml.proppr.util.APROptions;

import java.util.*;

/**
 * Translator to convert the system's syntax to ProPPR, and vice versa
 * <p>
 * Created on 25/04/17.
 *
 * @author Victor Guimarães
 */
public class ProPprEngineSystemTranslator extends EngineSystemTranslator {

    /**
     * The character to separate the predicate name from the ist arity.
     */
    public static final String PREDICATE_ARITY_SEPARATOR = "/";

    protected final WamProgram program;
    protected final FactsPlugin factsPlugin;

    /**
     * Constructs the class if the minimum required parameters.
     *
     * @param knowledgeBase   the {@link KnowledgeBase}
     * @param theory          the {@link Theory}
     * @param examples        the {@link ExampleSet}
     * @param aprOptions      the {@link APROptions}
     * @param factsPluginName the facts plugin's name
     * @param useTernayIndex  if is to use ternay index, makes an more efficient cache for predicates with arity
     *                        bigger than two
     */
    public ProPprEngineSystemTranslator(KnowledgeBase knowledgeBase, Theory theory, ExampleSet examples,
                                        APROptions aprOptions, String factsPluginName, boolean useTernayIndex) {
        super(knowledgeBase, theory, examples);
        program = compileTheory(theory);
        factsPlugin = buildFactsPlugin(aprOptions, factsPluginName, useTernayIndex);

    }

    /**
     * Compiles a {@link Theory} into a {@link WamProgram}, the internal representation of ProPRR.
     *
     * @param theory the {@link Theory}
     * @return the {@link WamProgram}
     */
    protected static WamProgram compileTheory(Theory theory) {
        WamProgram wamProgram = new WamBaseProgram();
        Rule rule;
        for (HornClause hornClause : theory) {
            rule = convertClauseToRule(hornClause);
            rule.variabilize();
            wamProgram.append(new Instruction(Instruction.OP.comment, rule.toString()));
            wamProgram.insertLabel(getLabelForRule(rule));
            wamProgram.append(rule);
        }
        wamProgram.save();
        return wamProgram;
    }

    /**
     * Builds the {@link FactsPlugin}. It is the internal representation of facts used by ProPPR.
     *
     * @param aprOptions      the {@link APROptions}
     * @param factsPluginName the plugin's name
     * @param useTernayIndex  if it should spend more memory to create an optimized index for predicates with arity
     *                        bigger than two
     * @return the {@link FactsPlugin}
     */
    protected FactsPlugin buildFactsPlugin(APROptions aprOptions, String factsPluginName, boolean useTernayIndex) {
        FactsPlugin factsPlugin = new FactsPlugin(aprOptions, factsPluginName, useTernayIndex);

        for (Atom atom : knowledgeBase) {
            if (!atom.isGrounded()) {
                continue;
            }
            if (atom instanceof WeightedAtom) {
                factsPlugin.addWeightedFact(atom.getName(), ((WeightedAtom) atom).getWeight(), LanguageUtils
                        .toStringCollectionToArray(atom.getTerms()));
            } else {
                factsPlugin.addFact(atom.getName(), LanguageUtils.toStringCollectionToArray(atom.getTerms()));
            }
        }

        return factsPlugin;
    }

    /**
     * Converts a {@link Clause}, from the system representation, to a {@link Rule} in ProPPR's representation.
     *
     * @param clause the {@link Clause}
     * @return the {@link Rule}
     */
    protected static Rule convertClauseToRule(Clause clause) {
        Goal lhs = null;                //head
        Goal[] rhs = new Goal[0];       //body
        Goal[] features = new Goal[0];  //features

        if (clause instanceof Atom) {
            lhs = convertAtomToGoal((Atom) clause, null);
        } else if (clause instanceof HornClause) {
            HornClause hornClause = (HornClause) clause;
            Map<Term, Integer> variableMap = new HashMap<>();
            lhs = convertAtomToGoal(hornClause.getHead(), variableMap);
            rhs = convertPositiveLiteralsToGoals(hornClause.getBody(), variableMap);
            if (hornClause instanceof FeaturedClause) {
                FeaturedClause featuredClause = (FeaturedClause) hornClause;
                features = convertAtomsToGoals(featuredClause.getFeatures(), variableMap);
            }
        }

        return new Rule(lhs, rhs, features, new Goal[0]);
    }

    /**
     * Gets the label for the {@link Rule}, so it can be proper added to the {@link WamProgram}.
     *
     * @param rule the {@link Rule}
     * @return the label
     */
    protected static String getLabelForRule(Rule rule) {
        return rule.getLhs().getFunctor() + PREDICATE_ARITY_SEPARATOR + rule.getLhs().getArity();
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
    protected static Goal convertAtomToGoal(Atom atom, Map<Term, Integer> variableMap) {
        String functor = atom.getName();
        Argument[] arguments = new Argument[atom.getTerms().size()];
        for (int i = 0; i < arguments.length; i++) {
            if (atom.getTerms().get(i).isConstant()) {
                arguments[i] = new ConstantArgument(atom.getTerms().get(i).getName());
            } else {
                if (variableMap == null) {
                    arguments[i] = new VariableArgument(i + 1);
                } else {
                    arguments[i] = new VariableArgument(variableMap.computeIfAbsent(atom.getTerms().get(i), k ->
                            variableMap.size() + 1));
                }
            }
        }

        return new Goal(functor, arguments);
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
    protected static Goal[] convertPositiveLiteralsToGoals(Iterable<? extends Literal> literals,
                                                           Map<Term, Integer> variableMap) {
        List<Goal> goals = new ArrayList<>();
        for (Literal literal : literals) {
            if (!literal.isNegated()) {
                goals.add(convertAtomToGoal(literal, variableMap));
            }
        }

        return goals.toArray(new Goal[0]);
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
    protected static Goal[] convertAtomsToGoals(Iterable<? extends Atom> atoms, Map<Term, Integer> variableMap) {
        List<Goal> goals = new ArrayList<>();
        for (Atom atom : atoms) {
            goals.add(convertAtomToGoal(atom, variableMap));
        }

        return goals.toArray(new Goal[0]);
    }

    @Override
    public Set<Atom> groundingExamples(Example... examples) {
        //TODO: call the ProPPR grounding
        return null;
    }

    @Override
    public void trainParameters(Example... examples) {
        //TODO: call the ProPPR training
    }

    @Override
    public void inferExample(Example example) {
        //TODO: call the ProPPR query answer, return some inferred example class
    }

}
