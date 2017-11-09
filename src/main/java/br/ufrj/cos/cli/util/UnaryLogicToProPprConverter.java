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

package br.ufrj.cos.cli.util;

import br.ufrj.cos.cli.CommandLineInterface;
import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.ProPprExample;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Predicate;
import br.ufrj.cos.logic.Variable;
import br.ufrj.cos.util.LanguageUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to convert examples from the logic representation to the ProPPR's representation.
 * <p>
 * Created on 04/08/17.
 *
 * @author Victor Guimarães
 */
public class UnaryLogicToProPprConverter extends LogicToProPprConverter {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new UnaryLogicToProPprConverter();
        mainProgram(instance, logger, args);
    }

    @Override
    protected Collection<? extends ProPprExample> convertAtomToExamples(Collection<? extends Atom> positives,
                                                                        Collection<? extends Atom> negatives) {
        if (positives.isEmpty()) { return Collections.emptyList(); }
        Map<Predicate, Set<AtomExample>> atomsByPredicate = new HashMap<>();
        Collection<AtomExample> positiveExamples =
                positives.stream().map(e -> new AtomExample(e, true)).collect(Collectors.toList());
        Collection<AtomExample> negativeExamples =
                negatives.stream().map(e -> new AtomExample(e, false)).collect(Collectors.toList());

        LanguageUtils.splitAtomsByPredicateKeepingOrder(positiveExamples, atomsByPredicate, Atom::getPredicate);
        LanguageUtils.splitAtomsByPredicateKeepingOrder(negativeExamples, atomsByPredicate, Atom::getPredicate);

        final Collection<ProPprExample> proPprExamples = new ArrayList<>();
        for (Map.Entry<Predicate, Set<AtomExample>> entry : atomsByPredicate.entrySet()) {
            appendAtomIntoExamples(entry.getKey(), entry.getValue(), proPprExamples);
        }
        return proPprExamples;
    }

    /**
     * Appends the positive and negative parts from the examples of the predicate in ProPPR's examples format.
     *
     * @param predicate      the predicate
     * @param examples       the examples
     * @param proPprExamples the collection to append to
     * @return the ProPPR's examples format
     */
    protected Collection<? extends ProPprExample> appendAtomIntoExamples(Predicate predicate,
                                                                         Collection<? extends AtomExample> examples,
                                                                         Collection<ProPprExample> proPprExamples) {
        if (predicate.getArity() != 1) {
            return Collections.emptySet();
        }
        final Atom goal = new Atom(predicate, Collections.singletonList(new Variable(variableName)));
        Collection<? extends AtomExample> positives = examples.stream()
                .filter(AtomExample::isPositive).collect(Collectors.toList());
        Collection<? extends AtomExample> negatives = examples.stream()
                .filter(a -> !a.isPositive()).collect(Collectors.toList());
        Collection<? extends AtomExample> bigger = negatives.size() > positives.size() ? negatives : positives;
        Collection<? extends AtomExample> smaller = negatives.size() > positives.size() ? positives : negatives;

        mergeAtomIntoExamples(goal, smaller, bigger, proPprExamples);

        return proPprExamples;
    }

    /**
     * Merges the atoms into ProPPR's examples and appends to proPprExamples
     *
     * @param goal           the goal of the atoms
     * @param smaller        the smaller set of atoms (positives or negatives)
     * @param bigger         the bigger set of atoms (positives or negatives)
     * @param proPprExamples the collections to append to
     */
    private void mergeAtomIntoExamples(Atom goal, Collection<? extends AtomExample> smaller,
                                       Collection<? extends AtomExample> bigger,
                                       Collection<ProPprExample> proPprExamples) {
        Iterator<? extends AtomExample> biggerIterator = bigger.iterator();
        Iterator<? extends AtomExample> smallerIterator = bigger.iterator();

        final int size = bigger.size() / smaller.size();
        final double probability = ((double) bigger.size() / smaller.size()) - size;
        final Random random = getRandom();
        while (biggerIterator.hasNext()) {
            final List<AtomExample> atomExamples = new ArrayList<>(size + 2);
            atomExamples.add(biggerIterator.next());
            for (int i = 0; i < size && smallerIterator.hasNext(); i++) {
                atomExamples.add(smallerIterator.next());
            }
            if (smallerIterator.hasNext() && random.nextDouble() < probability) {
                atomExamples.add(smallerIterator.next());
            }
            proPprExamples.add(new ProPprExample(goal, atomExamples));
        }

        final List<AtomExample> atomExamples = new ArrayList<>();
        if (smallerIterator.hasNext()) {
            while (smallerIterator.hasNext()) {
                atomExamples.add(smallerIterator.next());
            }
            proPprExamples.add(new ProPprExample(goal, atomExamples));
        }
    }

}
