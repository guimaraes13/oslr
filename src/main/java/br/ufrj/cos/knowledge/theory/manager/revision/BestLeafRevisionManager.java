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

package br.ufrj.cos.knowledge.theory.manager.revision;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.manager.TreeTheory;
import br.ufrj.cos.knowledge.theory.manager.revision.heuristic.RevisionHeuristic;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class to select the best leaves to use as revision, based on some heuristic.
 * <p>
 * Created on 26/06/17.
 *
 * @author Victor Guimarães
 */
public class BestLeafRevisionManager extends RevisionManager {

    /**
     * The default maximum number of leaves to call the revision, at once.
     */
    public static final int DEFAULT_LEAVES_TO_REFINE = 1;
    /**
     * The maximum number of leaves to call the revision, at once.
     */
    @SuppressWarnings("CanBeFinal")
    public int numberOfLeavesToRevise = DEFAULT_LEAVES_TO_REFINE;

    protected TreeTheory treeTheory;
    protected RevisionHeuristic revisionHeuristic;

    @Override
    public void reviseTheory(List<? extends Collection<? extends Example>> revisionPoints) {
        int totalRevision = Math.min(revisionPoints.size(), numberOfLeavesToRevise);
        List<Pair<Integer, ? extends Collection<? extends Example>>> revisions = sortKeepingIndexes(revisionPoints);
        for (int i = 0; i < totalRevision; i++) {
            treeTheory.revisionLeafIndex = revisions.get(i).getKey();
            callRevision(revisions.get(i).getValue());
        }
    }

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        List<String> fields = new ArrayList<>();
        if (treeTheory == null) {
            fields.add(TreeTheory.class.getSimpleName());
        }
        if (revisionHeuristic == null) {
            fields.add(RevisionHeuristic.class.getSimpleName());
        }
        if (!fields.isEmpty()) {
            throw new InitializationException(ExceptionMessages.errorFieldsSet(this, fields));
        }
    }

    /**
     * Sorts the revision points based on the heuristic, keeping the original index of the revision.
     *
     * @param revisionPoints the revision points
     * @return the sorted list with the revision points and original indexes
     */
    protected List<Pair<Integer, ? extends Collection<? extends Example>>> sortKeepingIndexes(
            List<? extends Collection<? extends Example>> revisionPoints) {
        List<Pair<Integer, ? extends Collection<? extends Example>>> sorted = buildIndexPairList(revisionPoints);
        sorted.sort((o1, o2) -> revisionHeuristic.compare(o1.getValue(), o2.getValue()));
        return sorted;
    }

    /**
     * Builds list of index and collection pairs.
     *
     * @param revisionPoints the revision points
     * @return the list of pairs
     */
    protected List<Pair<Integer, ? extends Collection<? extends Example>>> buildIndexPairList(
            List<? extends Collection<? extends Example>> revisionPoints) {
        List<Pair<Integer, ? extends Collection<? extends Example>>> list = new ArrayList<>(revisionPoints.size());
        for (int i = 0; i < revisionPoints.size(); i++) {
            list.add(new ImmutablePair<>(i, revisionPoints.get(i)));
        }
        return list;
    }

    /**
     * Gets the tree theory.
     *
     * @return tree theory
     */
    public TreeTheory getTreeTheory() {
        return treeTheory;
    }

    /**
     * Sets the tree theory.
     *
     * @param treeTheory tree theory
     * @throws InitializationException if the {@link TreeTheory} is already set
     */
    public void setTreeTheory(TreeTheory treeTheory) throws InitializationException {
        if (this.treeTheory != null) {
            throw new InitializationException(
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                   TreeTheory.class.getSimpleName()));
        }
        this.treeTheory = treeTheory;
    }

    /**
     * Gets the revision heuristic.
     *
     * @return the revision heuristic
     */
    public RevisionHeuristic getRevisionHeuristic() {
        return revisionHeuristic;
    }

    /**
     * Sets the revision heuristic.
     *
     * @param revisionHeuristic the revision heuristic
     * @throws InitializationException if the {@link TreeTheory} is already set
     */
    public void setRevisionHeuristic(RevisionHeuristic revisionHeuristic) throws InitializationException {
        if (this.revisionHeuristic != null) {
            throw new InitializationException(
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                   RevisionHeuristic.class.getSimpleName()));
        }
        this.revisionHeuristic = revisionHeuristic;
    }
}
