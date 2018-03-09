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

package br.ufrj.cos.knowledge.theory.manager.feature.proppr.heuristic;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Term;

import java.util.*;

/**
 * Calculates the portion of distinct terms between the set of terms that appear for positive and negative examples
 * for the same variable.
 * <p>
 * <p>
 * Created on 14/10/2017.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("unused")
public class MostDistinctSetsHeuristic extends SubstitutionHeuristic {

    /**
     * The default value of the normalize option.
     */
    public static final boolean DEFAULT_NORMALIZE_OPTION = true;
    /**
     * If {@code true}, normalizes the heuristic to every value be in [0, 1].
     */
    public boolean normalized = DEFAULT_NORMALIZE_OPTION;

    @Override
    public Map<Term, Double> calculateHeuristic(List<Term> variables, Map<Example, Map<Atom, Double>> substitutions) {
        Map<Term, Double> answer = new HashMap<>(variables.size());
        for (int i = 0; i < variables.size(); i++) {
            final double heuristicValue = computeHeuristicForIndex(substitutions, i);
            answer.put(variables.get(i), heuristicValue);
        }
        return answer;
    }

    /**
     * Computes the heuristic for the variable at the index.
     *
     * @param inferredExamples the substitutions
     * @param index            the index
     * @return the heuristic for the index
     */
    protected double computeHeuristicForIndex(Map<Example, Map<Atom, Double>> inferredExamples, int index) {
        final Set<Term> positives = new HashSet<>();
        final Set<Term> negatives = new HashSet<>();
        for (Map.Entry<Example, Map<Atom, Double>> inferredExample : inferredExamples.entrySet()) {
            final Set<Term> inputSet = inferredExample.getKey().isPositive() ? positives : negatives;
            for (Atom atom : inferredExample.getValue().keySet()) {
                inputSet.add(atom.getTerms().get(index));
            }
        }
        Set<Term> union = new HashSet<>(positives);
        union.addAll(positives);
        union.addAll(negatives);
        Set<Term> intersection = new HashSet<>(positives);
        intersection.retainAll(negatives);
        return getHeuristicValue(union, intersection);
    }

    /**
     * Gets the heuristic value.
     *
     * @param union        the union of the sets of positive and negative substitutions variables
     * @param intersection the intersection of the sets of positive and negative substitutions variables
     * @return the heuristic value
     */
    protected double getHeuristicValue(Set<Term> union, Set<Term> intersection) {
        if (normalized) {
            return (double) (union.size() - intersection.size()) / union.size();
        } else {
            return union.size() - intersection.size();
        }
    }

}
