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

package br.ufrj.cos.knowledge.example;

import br.ufrj.cos.knowledge.Knowledge;
import br.ufrj.cos.logic.Predicate;
import br.ufrj.cos.util.LanguageUtils;
import br.ufrj.cos.util.MapUtils;

import java.util.*;

/**
 * Responsible to hold the read atomExamples from the input.
 * <p>
 * Created on 25/04/17.
 *
 * @author Victor Guimarães
 */
public class Examples extends Knowledge<ProPprExample> {

    /**
     * The default class to be used on the {@link Set}s within the {@link Map}s
     */
    public static final Class<HashSet> DEFAULT_MAP_SET_CLASS = HashSet.class;
    /**
     * The class to be used on the {@link Set}s within the {@link Map}s
     */
    @SuppressWarnings({"CanBeFinal"})
    public Class<? extends Set> mapSetClass = DEFAULT_MAP_SET_CLASS;

    /**
     * Default constructor.
     */
    public Examples() {
        super(new HashSet<>());
    }

    /**
     * Constructor with the example lists
     *
     * @param proPprExamples the {@link ProPprExample}s
     * @param atomExamples   the {@link AtomExample}s
     * @throws InstantiationException if an error occurs when instantiating a new set
     * @throws IllegalAccessException if an error occurs when instantiating a new set
     */
    public Examples(Collection<ProPprExample> proPprExamples, Collection<? extends AtomExample> atomExamples) throws
            IllegalAccessException, InstantiationException {
        super(proPprExamples);
        if (!atomExamples.isEmpty()) { appendAtomExamplesIntoProPpr(atomExamples, buildPredicateMap()); }
    }

    /**
     * Appends the {@link AtomExample}s into the corresponding {@link ProPprExample}. An {@link AtomExample} is
     * appended to a {@link ProPprExample} if it is a possible grounding of the {@link ProPprExample}'s goal.
     * <p>
     * {@link AtomExample}s that is not ground of any {@link ProPprExample}'s goal will be discarded.
     * <p>
     * An {@link AtomExample} may be added to more than one {@link ProPprExample}.
     *
     * @param atomExamples the {@link AtomExample}s
     * @param predicateMap the predicate map of {@link ProPprExample}
     */
    public static void appendAtomExamplesIntoProPpr(Iterable<? extends AtomExample> atomExamples,
                                                    Map<Predicate, Set<ProPprExample>> predicateMap) {
        for (AtomExample atomExample : atomExamples) {
            for (ProPprExample proPprExample : predicateMap.get(atomExample.getPredicate())) {
                if (LanguageUtils.isAtomUnifiableToGoal(atomExample, proPprExample.getGoal())) {
                    proPprExample.getAtomExamples().add(atomExample);
                }
            }
        }
    }

    /**
     * Builds a {@link Map} that links the {@link String} predicate to the {@link ProPprExample}s that has the
     * predicate.
     *
     * @return the {@link Map}
     * @throws InstantiationException if an error occurs when instantiating a new set
     * @throws IllegalAccessException if an error occurs when instantiating a new set
     */
    protected Map<Predicate, Set<ProPprExample>> buildPredicateMap() throws InstantiationException,
            IllegalAccessException {
        Map<Predicate, Set<ProPprExample>> predicateMap = new HashMap<>();
        for (ProPprExample exampleSet : this) {
            MapUtils.assertExistsSet(predicateMap, mapSetClass, exampleSet.getGoal().getPredicate()).add(exampleSet);
        }

        return predicateMap;
    }

}
