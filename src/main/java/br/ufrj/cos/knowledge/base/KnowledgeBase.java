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

import br.ufrj.cos.knowledge.Knowledge;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.util.LogMessages;
import br.ufrj.cos.util.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;

/**
 * Responsible for holding the knowledge base. In addition, it keeps a cache in form of a graph of its {@link Term}s and
 * {@link Atom} where each {@link Term} is a node and there is a link between to {@link Term}s if they share a
 * {@link Atom}. There is also a {@link Map} for retrieving the {@link Atom}s where a {@link Term} appears.
 * <p>
 * Warning: those caches only works if the implementation of the given {@link Knowledge}'s {@link Collection} uses
 * the interface methods to add and delete its elements. If you are not sure that it is the case, please, call the
 * {@link #rebuildCache()} method after any change on the {@link Collection}, this will rebuild all the cache from
 * scratch.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
public class KnowledgeBase extends Knowledge<Atom> {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    protected final Map<Term, Set<Atom>> termAtomMap;
    protected final Map<Term, Set<Term>> termNeighbours;
    /**
     * The class to be used on the {@link Set}s within the {@link Map}s
     */
    @SuppressWarnings({"CanBeFinal", "NonConstantFieldWithUpperCaseName"})
    public Class<? extends Set> MAP_SET_CLASS = HashSet.class;

    /**
     * Constructs from a {@link Collection} of {@link Atom}s
     *
     * @param atoms the {@link Atom}s
     */
    public KnowledgeBase(Collection<Atom> atoms) {
        super(atoms);
        termAtomMap = new HashMap<>();
        termNeighbours = new HashMap<>();
    }

    /**
     * Constructs from a {@link Collection} of {@link Atom}s with a filter {@link Predicate}
     *
     * @param atoms           the {@link Atom}s
     * @param acceptPredicate the filter {@link Predicate}
     */
    public KnowledgeBase(Collection<Atom> atoms, Predicate<? super Clause> acceptPredicate) {
        super(atoms, acceptPredicate);
        termAtomMap = new HashMap<>();
        termNeighbours = new HashMap<>();
    }

    /**
     * Clears and rebuilds all the cache from scratch.
     */
    public void rebuildCache() {
        termAtomMap.clear();
        termNeighbours.clear();
        for (Atom atom : this) {
            try {
                addAtomToMaps(atom);
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error(LogMessages.ERROR_UPDATING_KNOWLEDGE_BASE_GRAPH.toString(), atom, e);
            }
        }
    }

    /**
     * Updates the cached maps when an {@link Atom} is added to the {@link KnowledgeBase}.
     *
     * @param atom the atom
     * @throws InstantiationException if an error occurs when instantiating a new set
     * @throws IllegalAccessException if an error occurs when instantiating a new set
     */
    protected void addAtomToMaps(Atom atom) throws InstantiationException, IllegalAccessException {
        for (Term term : atom.getTerms()) {
            MapUtils.assertExistsSet(termAtomMap, MAP_SET_CLASS, term).add(atom);
            addNeighbour(term, atom);
        }
    }

    /**
     * Updates the neighbours of the new {@link Term} with respect to the new {@link Atom}.
     *
     * @param term the term
     * @param atom the atom
     * @throws InstantiationException if an error occurs when instantiating a new set
     * @throws IllegalAccessException if an error occurs when instantiating a new set
     */
    protected void addNeighbour(Term term, Atom atom) throws InstantiationException, IllegalAccessException {
        for (Term neighbour : atom.getTerms()) {
            MapUtils.assertExistsSet(termNeighbours, MAP_SET_CLASS, term).add(neighbour);
        }
    }

    /**
     * Gets the {@link Set} of {@link Term}s in the base
     *
     * @return the {@link Set} of {@link Term}s in the base
     */
    public Set<Term> getTerms() {
        return termAtomMap.keySet();
    }

    /**
     * Gets the {@link Set} of {@link Atom}s which have the given {@link Term}
     *
     * @param term the {@link Term}
     * @return the {@link Set} of {@link Atom}s
     */
    public Set<Atom> getAtomsWithTerm(Term term) {
        return termAtomMap.get(term);
    }

    /**
     * Gets the neighbours of a {@link Term}. A {@link Term} is neighbour of another if both appears together in an
     * {@link Atom}
     *
     * @param term the {@link Term}
     * @return the {@link Set} of neighbours
     */
    public Set<Term> getTermNeighbours(Term term) {
        return termNeighbours.get(term);
    }

    @Override
    public boolean add(Atom atom) {
        if (super.add(atom)) {
            try {
                addAtomToMaps(atom);
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error(LogMessages.ERROR_UPDATING_KNOWLEDGE_BASE_GRAPH.toString(), atom, e);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Iterator<Atom> iterator() {
        Iterator<Atom> iterator = super.iterator();
        return new KnowledgeBaseIterator(this, iterator);
    }

    @Override
    public boolean remove(Object o) {
        if (super.remove(o)) {
            removeAtomFromMaps((Atom) o);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;

        if (size() > c.size()) {
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<?> i = c.iterator(); i.hasNext(); ) {
                modified |= remove(i.next());
            }
        } else {
            for (Iterator<?> i = iterator(); i.hasNext(); ) {
                if (c.contains(i.next())) {
                    i.remove();
                    modified = true;
                }
            }
        }
        return modified;
    }

    @Override
    public boolean removeIf(Predicate<? super Atom> filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        final Iterator<Atom> each = iterator();
        while (each.hasNext()) {
            if (filter.test(each.next())) {
                each.remove();
                removed = true;
            }
        }
        return removed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<Atom> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public void clear() {
        super.clear();
        termAtomMap.clear();
        termNeighbours.clear();
    }

    /**
     * Updates the cached maps when a {@link Atom} is removed from the {@link KnowledgeBase}.
     *
     * @param atom the removed atom
     */
    protected void removeAtomFromMaps(Atom atom) {
        for (Term term : atom.getTerms()) {
            removeAtomFromMaps(term, atom);
        }
    }

    /**
     * Removes the atom from the cached map, updating the map of neighbours when necessary.
     *
     * @param term the term
     * @param atom the atom
     */
    protected void removeAtomFromMaps(Term term, Atom atom) {
        Set<Atom> atoms = termAtomMap.get(term);
        if (atoms == null) {
            return;
        }
        atoms.remove(atom);
        if (atoms.isEmpty()) {
            // There is no more atoms with this constant; removes this constant from all neighbours lists
            removeTermFromMaps(term);
        } else {
            // This constants still exists, update its neighbours, if necessary
            updateNeighbourFromTerm(term, atom.getTerms());
        }
    }

    /**
     * Removes the term from the list of all their neighbours, then remove it from {@link #termNeighbours}.
     *
     * @param term the term to remove
     */
    protected void removeTermFromMaps(Term term) {
        Set<Term> neighbours = termNeighbours.get(term);
        Set<Term> reflexNeighbours;
        for (Term neighbour : neighbours) {
            reflexNeighbours = termNeighbours.get(neighbour);
            if (reflexNeighbours != null) {
                reflexNeighbours.remove(term);
            }
        }
        termNeighbours.remove(term);
    }

    /**
     * Updates the list of neighbours of the given term, removing the terms that are no longer neighbours.
     * This method do not add new neighbours, only removes the old ones.
     *
     * @param term       the term
     * @param neighbours the list of neighbour to update; typically, when update due a atom removal, the terms of the
     *                   atom is enough
     */
    protected void updateNeighbourFromTerm(Term term, Iterable<Term> neighbours) {
        Set<Atom> atoms = termAtomMap.get(term);
        Set<Atom> neighboursAtom;
        for (Term neighbour : neighbours) {
            neighboursAtom = termAtomMap.get(neighbour);
            if (Collections.disjoint(atoms, neighboursAtom)) {
                removeFromNeighbours(term, neighbour);
            }
        }
    }

    /**
     * Removes the {@code neighbour2} from the neighbour's neighbourhood and vice versa.
     *
     * @param neighbour1 the first neighbour
     * @param neighbour2 the second neighbour
     */
    protected void removeFromNeighbours(Term neighbour1, Term neighbour2) {
        removeFromNeighboursIfExists(neighbour1, neighbour2);
        removeFromNeighboursIfExists(neighbour2, neighbour1);
    }

    /**
     * Removes the neighbour from the source's neighbour
     *
     * @param source    the source
     * @param neighbour the neighbour
     * @return return true if changes the neighbours set
     */
    protected boolean removeFromNeighboursIfExists(Term source, Term neighbour) {
        Set<Term> neighbours = termNeighbours.get(source);
        return neighbour != null && neighbours.remove(neighbour);
    }

}
