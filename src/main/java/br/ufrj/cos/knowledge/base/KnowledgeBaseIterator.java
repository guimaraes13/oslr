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

package br.ufrj.cos.knowledge.base;

import br.ufrj.cos.logic.Atom;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * An {@link Iterator} over the {@link KnowledgeBase} to ensure that the cache will be updated due a removal.
 * <p>
 * Created on 28/04/17.
 *
 * @author Victor Guimarães
 */
public class KnowledgeBaseIterator implements Iterator<Atom> {

    protected final KnowledgeBase knowledgeBase;
    protected final Iterator<Atom> iterator;

    protected Atom current;

    /**
     * Constructs the class with all needed parameters
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     * @param iterator      the {@link Iterator}
     */
    public KnowledgeBaseIterator(KnowledgeBase knowledgeBase, Iterator<Atom> iterator) {
        this.knowledgeBase = knowledgeBase;
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Atom next() {
        current = iterator.next();
        return current;
    }

    @Override
    public void remove() {
        iterator.remove();
        knowledgeBase.removeAtomFromMaps(current);
    }

    @Override
    public void forEachRemaining(Consumer<? super Atom> action) {
        iterator.forEachRemaining(action);
    }

}
