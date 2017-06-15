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

package br.ufrj.cos.engine.proppr.ground;

import br.ufrj.cos.engine.proppr.ProPprEngineSystemTranslator;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.util.IterableConverter;
import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.prove.wam.Query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An {@link Iterator} that transforms {@link Example}s from the system representation to {@link InferenceExample}
 * in ProPPR's representation.
 * <p>
 * Created on 05/05/17.
 *
 * @author Victor Guimarães
 */
public class InferenceExampleIterable extends IterableConverter<Example, InferenceExample> {

    /**
     * The default empty query array.
     */
    public static final Query[] EMPTY_QUERY = new Query[0];

    /**
     * Constructs from an {@link Iterable}.
     *
     * @param iterable the {@link Iterable}
     */
    public InferenceExampleIterable(Iterable<? extends Example> iterable) {
        super(iterable);
    }

    /**
     * Constructs from an {@link Iterator}.
     *
     * @param iterator the {@link Iterator}
     */
    public InferenceExampleIterable(Iterator<? extends Example> iterator) {
        super(iterator);
    }

    /**
     * Constructs from an array.
     *
     * @param iterator the array
     */
    public InferenceExampleIterable(Example... iterator) {
        super(iterator);
    }

    @Override
    public InferenceExample processInToOut(Example example) {
        return exampleToInferenceExample(example);
    }

    /**
     * Converts an {@link Example} to an {@link InferenceExample}.
     *
     * @param example the {@link Example}
     * @return the {@link InferenceExample}
     */
    public static InferenceExample exampleToInferenceExample(Example example) {
        Query query = ProPprEngineSystemTranslator.atomToQuery(example.getAtom());
        List<Query> posSet = new ArrayList<>();
        List<Query> negSet = new ArrayList<>();

        example.getGroundedQuery().forEach(atom -> (atom.isPositive() ? posSet : negSet).add
                (ProPprEngineSystemTranslator.atomToQuery(atom)));

        return new InferenceExample(query, posSet.toArray(EMPTY_QUERY), negSet.toArray(EMPTY_QUERY));
    }

}
