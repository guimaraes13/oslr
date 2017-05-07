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

package br.ufrj.cos.util;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Class to convert one class into another and encapsulates it into a {@link Iterator} and {@link Iterable}.
 * <p>
 * Created on 05/05/17.
 *
 * @author Victor Guimarães
 */
public abstract class IterableConverter<In, Out> implements Iterable<Out>, Iterator<Out> {

    protected Iterator<? extends In> iterator;

    /**
     * Constructs from {@link Iterator}.
     *
     * @param iterator {@link Iterator}
     */
    public IterableConverter(Iterator<? extends In> iterator) {
        this.iterator = iterator;
    }

    /**
     * Constructs from an arbitrary array of {@link In}s.
     *
     * @param iterator the arbitrary array
     */
    @SafeVarargs
    public IterableConverter(In... iterator) {
        this.iterator = Arrays.asList(iterator).iterator();
    }

    @Override
    public Iterator<Out> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Out next() {
        return processInToOut(iterator.next());
    }

    /**
     * Converts an in to an out.
     *
     * @param in the {@link In}
     * @return the {@link Out}
     */
    public abstract Out processInToOut(In in);

}
