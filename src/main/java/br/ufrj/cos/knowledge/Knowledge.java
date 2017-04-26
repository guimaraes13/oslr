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

package br.ufrj.cos.knowledge;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Responsible for holding a {@link Collection} of objects that complains with a given {@link Predicate}.
 * <p>
 * Created on 25/04/17.
 *
 * @author Victor Guimarães
 */
public class Knowledge<T> implements Collection<T> {

    protected Collection<T> clauses;
    protected Predicate<? super T> acceptPredicate;

    public Knowledge(Collection<T> clauses) {
        this.clauses = clauses;
        this.acceptPredicate = o -> true;
    }

    public Knowledge(Collection<T> clauses, Predicate<? super T> acceptPredicate) {
        this.clauses = clauses;
        this.acceptPredicate = acceptPredicate;
    }

    @Override
    public boolean add(T t) {
        if (acceptPredicate.test(t)) {
            return clauses.add(t);
        }

        return false;
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
    public int size() {
        return clauses.size();
    }

    @Override
    public boolean isEmpty() {
        return clauses.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return clauses.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return clauses.iterator();
    }

    @Override
    public Object[] toArray() {
        return clauses.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return clauses.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return clauses.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return clauses.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return clauses.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return clauses.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return clauses.retainAll(c);
    }

    @Override
    public void clear() {
        clauses.clear();
    }

    @Override
    public boolean equals(Object o) {
        return clauses.equals(o);
    }

    @Override
    public int hashCode() {
        return clauses.hashCode();
    }

    @Override
    public Spliterator<T> spliterator() {
        return clauses.spliterator();
    }

    @Override
    public Stream<T> stream() {
        return clauses.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return clauses.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        clauses.forEach(action);
    }
}
