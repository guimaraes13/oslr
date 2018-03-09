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

package br.ufrj.cos.knowledge.theory.manager.revision.heuristic;

import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.manager.Node;
import br.ufrj.cos.logic.HornClause;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Calculates the number of repairable examples in the leaf. The number of repairable examples is the number of
 * positive (negative) examples in a negative (positive) leaf.
 * <p>
 * Created on 14/08/17.
 *
 * @author Victor Guimarães
 */
public class RepairableHeuristic extends RevisionHeuristic {

    protected final Predicate<? super AtomExample> positiveFilter = AtomExample::isPositive;
    protected final Predicate<? super AtomExample> negativeFilter = positiveFilter.negate();

    @Override
    public double evaluate(Collection<? extends Example> examples, Node<HornClause> revisionNode) {
        if (revisionNode.isDefaultChild()) {
            return examples.stream().flatMap(e -> e.getGroundedQuery().stream()).filter(positiveFilter).count();
        } else {
            return examples.stream().flatMap(e -> e.getGroundedQuery().stream()).filter(negativeFilter).count();
        }
    }

}
