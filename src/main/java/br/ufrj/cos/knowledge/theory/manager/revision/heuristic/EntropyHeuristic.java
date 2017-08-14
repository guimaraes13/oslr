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

package br.ufrj.cos.knowledge.theory.manager.revision.heuristic;

import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.manager.Node;
import br.ufrj.cos.logic.HornClause;

import java.util.Collection;

/**
 * Calculates the Entropy heuristic.
 * <p>
 * Created on 26/06/17.
 *
 * @author Victor Guimarães
 */
public class EntropyHeuristic extends RevisionHeuristic {

    /**
     * The base of the logarithmic function of the entropy.
     */
    public static final int LOG_BASE = 2;

    @Override
    public double evaluate(Collection<? extends Example> examples, Node<HornClause> revisionNode) {
        int positives = countExamples(examples, true);
        int negatives = countExamples(examples, false);
        int total = positives + negatives;

        double sum = 0.0;
        sum += calculateSumTerm((double) positives / total);
        sum += calculateSumTerm((double) negatives / total);
        return -sum;
    }

    /**
     * Counts the positive (true) or negative (false) examples from the collection.
     *
     * @param examples the collection of examples
     * @param positive {@code true} to count the positive examples, {@code false} to count the negative ones.
     * @return the count of positive or negative examples
     */
    protected static int countExamples(Collection<? extends Example> examples, boolean positive) {
        int count = 0;
        for (Example example : examples) {
            for (AtomExample ground : example.getGroundedQuery()) {
                if (ground.isPositive() == positive) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Calculates the term of the entropy's summation.
     *
     * @param portion the portion of the class
     * @return the term of the summation
     */
    protected static double calculateSumTerm(double portion) {
        return portion > 0 ? portion * StrictMath.log(portion) / StrictMath.log(LOG_BASE) : 0.0;
    }

}
