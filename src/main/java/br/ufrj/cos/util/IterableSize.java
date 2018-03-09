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

import java.util.Iterator;

/**
 * Class to limit the iteration over a iterable to a maximum size of elements. Then this iterator can be reset to
 * allow another iteration, of the same size, in the next elements.
 * <p>
 * Created on 08/08/17.
 *
 * @author Victor Guimarães
 */
public class IterableSize<E> implements Iterable<E>, Iterator<E> {

    protected final int numberOfElements;
    protected final Iterator<E> iterator;
    protected int index = 0;

    /**
     * Constructor if the size and the iterable.
     *
     * @param numberOfElements the size of the iteration
     * @param iterable         the iterable
     */
    public IterableSize(int numberOfElements, Iterable<E> iterable) {
        this(numberOfElements, iterable.iterator());
    }

    /**
     * Constructor if the size and the iterator.
     *
     * @param numberOfElements the size of the iteration
     * @param iterator         the iterator
     */
    public IterableSize(int numberOfElements, Iterator<E> iterator) {
        this.numberOfElements = numberOfElements;
        this.iterator = iterator;
    }

    /**
     * Resets the iterator to pass {@link #numberOfElements} elements.
     *
     * @return the number of read elements
     */
    public int reset() {
        int oldIndex = index;
        index = 0;
        return oldIndex;
    }

    @Override
    public boolean hasNext() {
        return (numberOfElements < 1 || index < numberOfElements) && iterator.hasNext();
    }

    @Override
    public E next() {
        if (hasNext()) {
            index++;
            return iterator.next();
        }

        return null;
    }

    @Override
    public Iterator<E> iterator() {
        return this;
    }

}
