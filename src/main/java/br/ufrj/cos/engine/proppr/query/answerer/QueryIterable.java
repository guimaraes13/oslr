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

package br.ufrj.cos.engine.proppr.query.answerer;

import br.ufrj.cos.engine.proppr.ProPprEngineSystemTranslator;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.util.IterableConverter;
import edu.cmu.ml.proppr.prove.wam.Query;

import java.util.Iterator;

/**
 * An {@link Iterator} that transforms {@link Example}s from the system representation to {@link Query}is
 * in ProPPR's representation.
 * <p>
 * Created on 05/05/17.
 *
 * @author Victor Guimarães
 */
public class QueryIterable extends IterableConverter<Example, Query> {

    /**
     * Constructs from an {@link Iterable}.
     *
     * @param iterable the {@link Iterable}
     */
    public QueryIterable(Iterable<? extends Example> iterable) {
        super(iterable);
    }

    /**
     * Constructs from an {@link Iterator}.
     *
     * @param iterator the {@link Iterator}
     */
    public QueryIterable(Iterator<? extends Example> iterator) {
        super(iterator);
    }

    /**
     * Constructs from an array.
     *
     * @param iterator the array
     */
    public QueryIterable(Example... iterator) {
        super(iterator);
    }

    @Override
    public Query processInToOut(Example example) {
        return ProPprEngineSystemTranslator.atomToQuery(example.getGoalQuery());
    }

}
