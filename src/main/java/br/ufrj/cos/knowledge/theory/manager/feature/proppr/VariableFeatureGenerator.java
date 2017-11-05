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

package br.ufrj.cos.knowledge.theory.manager.feature.proppr;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.manager.feature.FeatureGenerator;
import br.ufrj.cos.knowledge.theory.manager.feature.proppr.heuristic.SubstitutionHeuristic;
import br.ufrj.cos.knowledge.theory.manager.feature.proppr.heuristic.ZeroHeuristic;
import br.ufrj.cos.logic.*;
import br.ufrj.cos.util.HornClauseUtils;
import br.ufrj.cos.util.InitializationException;

import java.util.*;
import java.util.stream.Collectors;

import static br.ufrj.cos.util.HornClauseUtils.buildQueriesFromExamples;
import static br.ufrj.cos.util.HornClauseUtils.buildSubstitutionClause;

/**
 * Creates variable features, based on heuristics on the substitution of the variable from the rule.
 * <p>
 * Created on 13/10/2017.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("unused")
public class VariableFeatureGenerator extends FeatureGenerator {

    /**
     * The default value of the flag to add target variables.
     */
    public static final boolean DEFAULT_ADD_TARGET_VARIABLES = false;
    /**
     * The default substitutions heuristic.
     */
    public static final ZeroHeuristic DEFAULT_SUBSTITUTION_HEURISTIC = new ZeroHeuristic();
    /**
     * The default value of the flag to delete target constant.
     */
    public static final boolean DEFAULT_DELETE_TARGET_CONSTANT = false;
    /**
     * The heuristic to be applied to the substitution of the variables in order to decide which variables are the
     * best ones.
     */
    public SubstitutionHeuristic substitutionHeuristic = DEFAULT_SUBSTITUTION_HEURISTIC;
    /**
     * If the target variables of the examples must be always added as a feature variable.
     * <p>
     * The target variables are the ones that are not instantiated in an example in ProPPR's format.
     */
    public boolean addTargetVariables = DEFAULT_ADD_TARGET_VARIABLES;
    /**
     * If the target constants of the examples must be always added as a feature variable.
     * <p>
     * The target constants are the ones that are instantiated in an example in ProPPR's format.
     */
    public boolean deleteTargetConstant = DEFAULT_DELETE_TARGET_CONSTANT;
    protected UniqueRuleFeatureGenerator uniqueRuleFeatureGenerator = new UniqueRuleFeatureGenerator();

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        uniqueRuleFeatureGenerator.setLearningSystem(learningSystem);
        uniqueRuleFeatureGenerator.initialize();
        substitutionHeuristic.initialize();
    }

    @Override
    public HornClause createFeatureForRule(HornClause rule, Collection<? extends Example> examples) {
        if (examples.isEmpty()) { return rule; }
        HornClause substitutionClause = buildSubstitutionClause(rule);
        final Atom head = substitutionClause.getHead();
        Set<Example> querySet = buildQueriesFromExamples(examples, rule.getHead(), head,
                                                         false);
        if (querySet.isEmpty()) { return rule; }
        Map<Example, Map<Atom, Double>> substitutions =
                learningSystem.inferExamples(Collections.singleton(substitutionClause), querySet);

        List<Term> featureVariables = getFeatureVariables(head, substitutions);
        if (addTargetVariables) { appendTargetVariables(head, examples, featureVariables); }
        if (deleteTargetConstant) { removeTargetConstant(head, examples, featureVariables); }

        final Features features = new Features(1);
        features.add(new Atom(uniqueRuleFeatureGenerator.createFeatureName(rule), featureVariables));
        return new FeaturedClause(rule.getHead(), rule.getBody(), features);
    }

    /**
     * Gets the feature variables.
     *
     * @param head          the head of the substitution rule
     * @param substitutions the substitutions of the variables
     * @return the feature variables
     */
    protected List<Term> getFeatureVariables(Atom head, Map<Example, Map<Atom, Double>> substitutions) {
        Map<Term, Double> heuristics = substitutionHeuristic.calculateHeuristic(head.getTerms(), substitutions);
        if (heuristics == null || heuristics.isEmpty()) { return new ArrayList<>(); }
        List<Term> sortedTerms = heuristics.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue, substitutionHeuristic))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        final int biggestGap = HornClauseUtils.findBiggestGap(sortedTerms, heuristics::get);
        return sortedTerms.subList(0, biggestGap);
    }

    /**
     * Appends the target variables to the feature variables.
     *
     * @param head             the head of the substitution rule
     * @param examples         the examples
     * @param variableFeatures the feature variables
     */
    protected static void appendTargetVariables(Atom head, Collection<? extends Example> examples,
                                                List<Term> variableFeatures) {
        List<Integer> targetIndexes = new ArrayList<>();
        Iterator<? extends Example> iterator = examples.iterator();
        Example example = iterator.next();
        final List<Term> goalTerms = example.getGoalQuery().getTerms();
        for (int i = 0; i < goalTerms.size(); i++) {
            if (!goalTerms.get(i).isConstant()) { targetIndexes.add(i); }
        }
        for (Integer index : targetIndexes) {
            if (!variableFeatures.contains(head.getTerms().get(index))) {
                variableFeatures.add(head.getTerms().get(index));
            }
        }
    }

    /**
     * Removes the target constants from the feature variables.
     *
     * @param head             the head of the substitution rule
     * @param examples         the examples
     * @param variableFeatures the feature variables
     */
    protected static void removeTargetConstant(Atom head, Collection<? extends Example> examples,
                                               List<Term> variableFeatures) {
        List<Integer> targetIndexes = new ArrayList<>();
        Iterator<? extends Example> iterator = examples.iterator();
        Example example = iterator.next();
        final List<Term> goalTerms = example.getGoalQuery().getTerms();
        for (int i = 0; i < goalTerms.size(); i++) {
            if (goalTerms.get(i).isConstant()) { targetIndexes.add(i); }
        }
        for (Integer index : targetIndexes) {
            variableFeatures.remove(head.getTerms().get(index));
        }
    }

}
