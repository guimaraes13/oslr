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
import br.ufrj.cos.logic.Atom;
import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.prove.wam.Query;

import java.util.*;

/**
 * An {@link Iterator} that transforms {@link Example}s from the system representation to {@link InferenceExample}
 * from ProPPR's representation.
 * <p>
 * Created on 05/05/17.
 *
 * @author Victor Guimarães
 */
public class InferenceExampleIterable implements Iterable<InferenceExample>, Iterator<InferenceExample> {

    protected Iterator<? extends Example> examples;

    /**
     * Constructs from {@link Iterator}.
     *
     * @param examples {@link Iterator}
     */
    public InferenceExampleIterable(Iterator<? extends Example> examples) {
        this.examples = examples;
    }

    /**
     * Constructs from an arbitrary array of {@link Example}s.
     *
     * @param examples the arbitrary array
     */
    public InferenceExampleIterable(Example... examples) {
        this.examples = Arrays.asList(examples).iterator();
    }

    @Override
    public Iterator<InferenceExample> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return examples.hasNext();
    }

    @Override
    public InferenceExample next() {
        return exampleToInferenceExample(examples.next());
    }

    /**
     * Converts an {@link Example} to an {@link InferenceExample}.
     *
     * @param example the {@link Example}
     * @return the {@link InferenceExample}
     */
    public static InferenceExample exampleToInferenceExample(Example example) {
        Query query = atomToQuery(example.getAtom());
        List<Query> posSet = new ArrayList<>();
        List<Query> negSet = new ArrayList<>();

        example.getGroundedQuery().forEach(atom -> (atom.isPositive() ? posSet : negSet).add(atomToQuery(atom)));

        return new InferenceExample(query, posSet.toArray(new Query[0]), negSet.toArray(new Query[0]));
    }

    /**
     * Converts an {@link Atom} to a {@link Query}.
     *
     * @param atom the {@link Atom}
     * @return the {@link Query}
     */
    public static Query atomToQuery(Atom atom) {
        return new Query(ProPprEngineSystemTranslator.atomToGoal(atom, new HashMap<>()));
    }

}
