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

package br.ufrj.cos.knowledge.theory.manager.revision;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.util.InitializationException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Class to select leaves to use as revision, stochastic, proportional to some heuristic.
 * <p>
 * Created on 26/06/17.
 *
 * @author Victor Guimarães
 */
public class StochasticLeafRevisionManager extends BestLeafRevisionManager {

    /**
     * The default random seed.
     */
    public static final long DEFAULT_RANDOM_SEED = 1234L;
    /**
     * The random seed.
     */
    @SuppressWarnings("CanBeFinal")
    public long randomSeed = DEFAULT_RANDOM_SEED;

    protected Random random;

    @Override
    public void reviseTheory(List<? extends Collection<? extends Example>> revisionPoints, TheoryMetric metric) {
        int totalRevision = Math.min(revisionPoints.size(), numberOfLeavesToRevise);
        List<Pair<Integer, ? extends Collection<? extends Example>>> pairList = buildIndexPairList(revisionPoints);
        List<Pair<Integer, Double>> heuristicList = buildHeuristicList(revisionPoints);
        int index;
        for (int i = 0; i < totalRevision; i++) {
            index = rouletteSelection(heuristicList);
            treeTheory.revisionLeafIndex = pairList.get(index).getKey();
            callRevision(pairList.get(index).getValue(), metric);
            pairList.remove(index);
            heuristicList.remove(index);
        }
    }

    /**
     * Builds list of heuristic value and collection index pairs.
     *
     * @param revisionPoints the revision points
     * @return the list of pairs
     */
    protected List<Pair<Integer, Double>> buildHeuristicList(
            List<? extends Collection<? extends Example>> revisionPoints) {
        List<Pair<Integer, Double>> list = new ArrayList<>(revisionPoints.size());
        for (int i = 0; i < revisionPoints.size(); i++) {
            list.add(new ImmutablePair<>(i, revisionHeuristic.evaluate(revisionPoints.get(i))));
        }
        return list;
    }

    /**
     * Uses the roulette revision will be applied.
     *
     * @param heuristicList the list of index and heuristic pairs
     * @return the index of the select entry in the heuristicList
     */
    public int rouletteSelection(List<Pair<Integer, Double>> heuristicList) {
        int index = 0;
        double sum = calculateSumOfHeuristics(heuristicList);
        double randomValue = random.nextDouble();
        double split = heuristicList.get(index).getValue() / sum;
        while (randomValue > split) {
            index++;
            randomValue -= split;
            split = heuristicList.get(index).getValue() / sum;
        }

        return index;
    }

    /**
     * Gets the sum of the heuristic from the pair list.
     *
     * @param list the pair list
     * @return the sum of the heuristic
     */
    protected static double calculateSumOfHeuristics(List<Pair<Integer, Double>> list) {
        double sum = 0.0;
        for (Pair<?, Double> pair : list) {
            sum += pair.getValue();
        }
        return sum;
    }

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        random = new Random(randomSeed);
    }

}
