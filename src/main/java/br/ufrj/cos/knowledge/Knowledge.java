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
 * Copyright (C) 2017-2019 Victor
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

package br.ufrj.cos.knowledge;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Responsible for holding a {@link Collection} of objects that complies with a given {@link Predicate}.
 * <p>
 * Created on 25/04/17.
 *
 * @author Victor Guimarães
 */
public class Knowledge<T> implements Collection<T> {

    protected final Collection<T> collection;
    protected final Predicate<? super T> acceptPredicate;

    /**
     * Constructs from a {@link Collection} of {@link T}
     *
     * @param collection the {@link Collection}
     */
    public Knowledge(Collection<T> collection) {
        this.collection = collection;
        this.acceptPredicate = o -> true;
    }

    /**
     * Constructs from a {@link Collection} of {@link T}
     *
     * @param t the T
     */
    public Knowledge(T t) {
        this.collection = new HashSet<>(1);
        this.acceptPredicate = o -> true;
        this.add(t);
    }

    /**
     * Constructs from a {@link Collection} of {@link T} with a {@link Predicate} filter.
     *
     * @param collection      the {@link Collection}
     * @param acceptPredicate the {@link Predicate} filter
     */
    public Knowledge(Collection<T> collection, Predicate<? super T> acceptPredicate) {
        this.collection = collection;
        if (acceptPredicate != null) {
            this.acceptPredicate = acceptPredicate;
        } else {
            this.acceptPredicate = o -> true;
        }
    }

    /**
     * Constructs from a {@link T} with a {@link Predicate} filter.
     *
     * @param t               the {@link T}
     * @param acceptPredicate the {@link Predicate} filter
     */
    public Knowledge(T t, Predicate<? super T> acceptPredicate) {
        this.collection = new HashSet<>(1);
        if (acceptPredicate != null) {
            this.acceptPredicate = acceptPredicate;
        } else {
            this.acceptPredicate = o -> true;
        }
        this.add(t);
    }

    /**
     * Add all objects from c that is instance of clazz
     *
     * @param c     the {@link Collection} of objects
     * @param clazz the class
     * @return <tt>true</tt> if this collection changed as a result of the call
     */
    public boolean addAll(Collection<? super T> c, Class<? extends T> clazz) {
        boolean changed = false;
        for (Object o : c) {
            if (clazz.isInstance(o)) {
                changed |= this.add(clazz.cast(o));
            }
        }

        return changed;
    }

    /**
     * Gets the predicate
     *
     * @return the predicate
     */
    public Predicate<? super T> getAcceptPredicate() {
        return acceptPredicate;
    }

    @Override
    public int size() {
        return collection.size();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return collection.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return collection.iterator();
    }

    @Override
    public Object[] toArray() {
        return collection.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        //noinspection SuspiciousToArrayCall
        return collection.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return t != null && acceptPredicate.test(t) && collection.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return collection.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return collection.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for (T t : c) {
            changed |= add(t);
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return collection.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return collection.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return collection.retainAll(c);
    }

    @Override
    public void clear() {
        collection.clear();
    }

    @Override
    public Spliterator<T> spliterator() {
        return collection.spliterator();
    }

    @Override
    public Stream<T> stream() {
        return collection.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return collection.parallelStream();
    }

    @Override
    public int hashCode() {
        return collection.hashCode();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return collection.equals(o);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        collection.forEach(action);
    }

}
