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

import br.ufrj.cos.knowledge.manager.TreeTheory;
import br.ufrj.cos.knowledge.theory.manager.revision.heuristic.RevisionHeuristic;
import br.ufrj.cos.knowledge.theory.manager.revision.point.RevisionExamples;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.InitializationException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Comparator;
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
     * The maximum number of leaves to call the revision, at once. Set to negative to revise all possible leaves at
     * the time.
     */
    @SuppressWarnings("CanBeFinal")
    public int numberOfLeavesToRevise = DEFAULT_LEAVES_TO_REFINE;

    protected TreeTheory treeTheory;
    protected RevisionHeuristic revisionHeuristic;

    @Override
    public void reviseTheory(List<? extends RevisionExamples> revisionPoints,
                             final boolean trainUsingAllExamples) {
        int totalRevision = getMaximumRevisionPoints(revisionPoints);
        List<Pair<Integer, ? extends RevisionExamples>> revisions = sortKeepingIndexes(revisionPoints,
                                                                                       trainUsingAllExamples);
        for (int i = 0; i < totalRevision; i++) {
            treeTheory.revisionLeafIndex = revisions.get(i).getKey();
            callRevision(revisions.get(i).getValue());
        }
    }

    /**
     * Gets the maximum revision points that will be used, based on the {@link #numberOfLeavesToRevise} and the size
     * of the revisionPoints
     *
     * @param revisionPoints the revision points
     * @return the maximum revision points that will be used
     */
    protected int getMaximumRevisionPoints(List<?> revisionPoints) {
        return numberOfLeavesToRevise < DEFAULT_LEAVES_TO_REFINE ?
                Math.min(revisionPoints.size(), numberOfLeavesToRevise) : revisionPoints.size();
    }

    /**
     * Sorts the revision points based on the heuristic, keeping the original index of the revision.
     *
     * @param revisionPoints        the revision points
     * @param trainUsingAllExamples if is to train using all examples or just the relevant sample
     * @return the sorted list with the revision points and original indexes
     */
    protected List<Pair<Integer, ? extends RevisionExamples>> sortKeepingIndexes(
            List<? extends RevisionExamples> revisionPoints, final boolean trainUsingAllExamples) {
        List<Pair<Integer, ? extends RevisionExamples>> sorted = buildIndexPairList(revisionPoints);
        sorted.sort(Comparator.comparing(
                o -> new ImmutablePair<>(o.getValue().getTrainingExamples(trainUsingAllExamples),
                                         treeTheory.getRevisionLeaf(o.getLeft())),
                revisionHeuristic));
        return sorted;
    }

    /**
     * Builds list of index and collection pairs.
     *
     * @param revisionPoints the revision points
     * @return the list of pairs
     */
    protected static List<Pair<Integer, ? extends RevisionExamples>> buildIndexPairList(
            List<? extends RevisionExamples> revisionPoints) {
        List<Pair<Integer, ? extends RevisionExamples>> list = new ArrayList<>(revisionPoints.size());
        for (int i = 0; i < revisionPoints.size(); i++) {
            list.add(new ImmutablePair<>(i, revisionPoints.get(i)));
        }
        return list;
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
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
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
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                 RevisionHeuristic.class.getSimpleName()));
        }
        this.revisionHeuristic = revisionHeuristic;
    }
}
