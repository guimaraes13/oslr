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

package br.ufrj.cos.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class to convert one class into another and encapsulates it into a {@link Iterator} and {@link Iterable}.
 * <p>
 * Created on 05/05/17.
 *
 * @author Victor Guimarães
 */
public abstract class IterableConverter<In, Out> implements Iterable<Out>, Iterator<Out> {

    protected final Iterator<? extends In> iterator;

    protected final Map<Integer, In> countMap = new HashMap<>();
    private int counter = 0;

    /**
     * Constructs from {@link Iterable}.
     *
     * @param iterable {@link Iterable}
     */
    protected IterableConverter(Iterable<? extends In> iterable) {
        this.iterator = iterable.iterator();
    }

    /**
     * Constructs from {@link Iterator}.
     *
     * @param iterator {@link Iterator}
     */
    protected IterableConverter(Iterator<? extends In> iterator) {
        this.iterator = iterator;
    }

    /**
     * Constructs from an arbitrary array of {@link In}s.
     *
     * @param iterator the arbitrary array
     */
    @SafeVarargs
    protected IterableConverter(In... iterator) {
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
        In in = iterator.next();
        counter++;
        countMap.put(counter, in);
        return processInToOut(in);
    }

    /**
     * Converts an in to an out.
     *
     * @param in the {@link In}
     * @return the {@link Out}
     */
    public abstract Out processInToOut(In in);

    /**
     * Gets a count {@link Map} of the {@link In}s. This {@link Map} associates a number, from [1, N] to each input,
     * where N is the number of elements. This number is associated in the order a input is required.
     *
     * @return the count {@link Map}
     */
    public Map<Integer, In> getCountMap() {
        return countMap;
    }
}
