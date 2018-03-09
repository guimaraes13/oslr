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

package br.ufrj.cos.knowledge.base;

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.knowledge.Knowledge;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.util.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static br.ufrj.cos.util.log.SystemLog.ERROR_UPDATING_KNOWLEDGE_BASE_GRAPH;

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
    /**
     * A map of the term and the atom where it appears.
     */
    protected final Map<Term, Set<Atom>> termAtomMap;
    /**
     * A map of the term and its neighbours. Two terms are neighbours if the appears in the same atom.
     */
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
        try {
            for (Atom atom : atoms) {
                addAtomToMaps(atom);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error(ERROR_UPDATING_KNOWLEDGE_BASE_GRAPH.toString(), e);
        }
    }

    /**
     * Finds the shortest path, of at most maximumDistance long, between two terms in the knowledge base, if such path
     * exists.
     *
     * @param source          the source term
     * @param destination     the destination term
     * @param maximumDistance the maximum distance, set to negative for no maximum distance.
     * @return the shortest path between the terms
     */
    public Collection<Term[]> shortestPath(Term source, Term destination, int maximumDistance) {
        if (!getTerms().contains(source) || !getTerms().contains(destination)) { return null; }

        if (source.equals(destination) || termNeighbours.get(source).contains(destination)) {
            return Collections.singleton(new Term[]{source, destination});
        }

        return findShortestPath(source, destination, maximumDistance);
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
     * Gets the relevant {@link Atom}s, given the relevant seed {@link Term}s, by performing a breadth-first search
     * on the {@link KnowledgeBase}'s cached graph
     *
     * @param terms          the seed {@link Term}s
     * @param relevantsDepth the depth of the relevant breadth first search
     * @return the relevant {@link Atom}s to the seed {@link Term}s
     */
    @SuppressWarnings({"OverlyLongMethod"})
    public Set<Atom> baseBreadthFirstSearch(Iterable<? extends Term> terms, int relevantsDepth) {
        Map<Term, Integer> termDistance = new HashMap<>();
        Queue<Term> queue = new ArrayDeque<>();
        Set<Atom> atoms = new HashSet<>();

        for (Term term : terms) {
            termDistance.put(term, 0);
            queue.add(term);
        }

        Set<Atom> atomSet;
        Term currentTerm;
        Integer currentDistance;
        Integer previousDistance = 0;
        while (!queue.isEmpty()) {
            currentTerm = queue.poll();
            currentDistance = termDistance.get(currentTerm);

            if (!Objects.equals(currentDistance, previousDistance)) {
                previousDistance = currentDistance;
            }

            atomSet = getAtomsWithTerm(currentTerm);
            atoms.addAll(atomSet);

            if (relevantsDepth == LearningSystem.NO_MAXIMUM_DEPTH || currentDistance < relevantsDepth) {
                for (Term neighbour : getTermNeighbours(currentTerm)) {
                    if (!termDistance.containsKey(neighbour)) {
                        termDistance.put(neighbour, currentDistance + 1);
                        queue.add(neighbour);
                    }
                }
            }
        }

        return atoms;
    }

    /**
     * Gets the {@link Set} of {@link Atom}s which have the given {@link Term}
     *
     * @param term the {@link Term}
     * @return the {@link Set} of {@link Atom}s
     */
    public Set<Atom> getAtomsWithTerm(Term term) {
        return termAtomMap.getOrDefault(term, Collections.emptySet());
    }

    /**
     * Gets the neighbours of a {@link Term}. A {@link Term} is neighbour of another if both appears together in an
     * {@link Atom}
     *
     * @param term the {@link Term}
     * @return the {@link Set} of neighbours
     */
    public Set<Term> getTermNeighbours(Term term) {
        return termNeighbours.getOrDefault(term, Collections.emptySet());
    }

    /**
     * Finds the shortest path, of at most maximumDistance long, between two terms in the knowledge base, if such path
     * exists.
     *
     * @param source          the source term
     * @param destination     the destination term
     * @param maximumDistance the maximum distance, set to negative for no maximum distance.
     * @return the shortest path between the terms
     */
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    protected Collection<Term[]> findShortestPath(Term source, Term destination, int maximumDistance) {
        Map<Term, Integer> distanceForVertex = new HashMap<>();
        Map<Term, Set<Term>> predecessorForVertex = new HashMap<>();

        distanceForVertex.put(source, 0);

        Queue<Term> queue = new ArrayDeque<>();
        queue.add(source);
        Term current;
        Integer distance;
        boolean found = false;
        int previousDistance = 0;
        while (!queue.isEmpty()) {
            current = queue.poll();
            distance = distanceForVertex.get(current);
            if (found && distance > previousDistance) {
                break;
            }
            previousDistance = distance;
            if (maximumDistance > 0 && distance > maximumDistance - 1) {
                break;
            }

            for (Term neighbor : termNeighbours.get(current)) {
                predecessorForVertex.computeIfAbsent(neighbor, k -> new HashSet<>()).add(current);
                if (!distanceForVertex.containsKey(neighbor)) {
                    distanceForVertex.put(neighbor, distance + 1);
                    queue.add(neighbor);
                }

                if (neighbor.equals(destination)) {
                    found = true;
                    break;
                }
            }
        }

        if (!found) { return null; }
        return buildPaths(source, destination, predecessorForVertex, distanceForVertex.get(destination));
    }

    /**
     * Builds the paths based on the predecessors then filters it by the ones that starts on source and ends on
     * destination.
     *
     * @param source               the source of the path
     * @param destination          the destination of the path
     * @param predecessorForVertex the predecessors of the terms
     * @param pathLength           the length of the path
     * @return the paths that starts on source and ends on destination
     */
    protected static Collection<Term[]> buildPaths(Term source, Term destination,
                                                   Map<Term, Set<Term>> predecessorForVertex, int pathLength) {
        Queue<Term[]> queue = new ArrayDeque<>();
        queue.add(new Term[]{destination});
        Term[] currentArray;
        Term[] auxiliary;
        int size;

        for (int i = pathLength - 1; i > -1; i--) {
            size = queue.size();
            for (int j = 0; j < size; j++) {
                currentArray = queue.poll();
                for (Term predecessor : predecessorForVertex.get(currentArray[0])) {
                    auxiliary = new Term[currentArray.length + 1];
                    System.arraycopy(currentArray, 0, auxiliary, 1, currentArray.length);
                    auxiliary[0] = predecessor;
                    queue.add(auxiliary);
                }
            }
        }

        return queue.stream().filter(a -> a[0].equals(source) && a[a.length - 1].equals(destination))
                .collect(Collectors.toSet());
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
                logger.error(ERROR_UPDATING_KNOWLEDGE_BASE_GRAPH.toString(), e);
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

    @Override
    public boolean add(Atom atom) {
        if (super.add(atom)) {
            try {
                addAtomToMaps(atom);
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error(ERROR_UPDATING_KNOWLEDGE_BASE_GRAPH.toString(), e);
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
