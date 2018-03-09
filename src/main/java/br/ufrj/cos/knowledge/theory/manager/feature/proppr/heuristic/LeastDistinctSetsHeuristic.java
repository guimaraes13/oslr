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

import br.ufrj.cos.logic.Term;

import java.util.Set;

/**
 * The opposite of the {@link MostDistinctSetsHeuristic}
 * <p>
 * Created on 17/10/2017.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("unused")
public class LeastDistinctSetsHeuristic extends MostDistinctSetsHeuristic {

    /**
     * Gets the heuristic value.
     *
     * @param union        the union of the sets of positive and negative substitutions variables
     * @param intersection the intersection of the sets of positive and negative substitutions variables
     * @return the heuristic value
     */
    @Override
    protected double getHeuristicValue(Set<Term> union, Set<Term> intersection) {
        if (normalized) {
//            return (double) (union.size() - intersection.size()) / union.size();
            return 1 - super.getHeuristicValue(union, intersection);
        } else {
            return intersection.size() - union.size();
        }
    }

}
