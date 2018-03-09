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

package br.ufrj.cos.knowledge.theory.manager.revision.point;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.InitializationException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Selects an independent sample of examples from the target of a revision point. Two example is said to be
 * independent, in a given
 * distance, if they do not share a common relevant in the given distance.
 * <p>
 * Created on 09/07/17.
 *
 * @author Victor Guimarães
 */
public class IndependentSampleSelector extends RelevantSampleSelector {

    /**
     * The default depth of the relevant breadth first search.
     */
    public static final int DEFAULT_RELEVANT_DEPTH = 0;
    /**
     * The relevant depth field name.
     */
    protected static final String RELEVANT_DEPTH_FIELD_NAME = "relevantDepth";
    private static final boolean SAFE_STOP = false;
    protected final Set<Atom> previousRelevants;
    /**
     * The depth of the relevant breadth first search.
     */
    protected int relevantDepth = DEFAULT_RELEVANT_DEPTH;

    /**
     * Default constructor.
     */
    public IndependentSampleSelector() {
        previousRelevants = new HashSet<>();
    }

    @Override
    public boolean isAllRelevants() {
        return relevantDepth < DEFAULT_RELEVANT_DEPTH;
    }

    @Override
    public boolean isRelevant(Example example) {
        if (relevantDepth < DEFAULT_RELEVANT_DEPTH) { return true; }
        boolean relevant = false;
        Set<Term> terms;
        terms = example.getGoalQuery().getTerms().stream().filter(Term::isConstant).collect(Collectors.toSet());
        Set<Atom> currentRelevants = learningSystem.relevantsBreadthFirstSearch(terms, relevantDepth, SAFE_STOP);
        if (Collections.disjoint(previousRelevants, currentRelevants)) {
            relevant = true;
        }
        previousRelevants.addAll(currentRelevants);
        return relevant;
    }

    @Override
    public RelevantSampleSelector copy() throws InitializationException {
        IndependentSampleSelector relevantSampleSelector = new IndependentSampleSelector();
        relevantSampleSelector.setLearningSystem(learningSystem);
        relevantSampleSelector.setRelevantDepth(relevantDepth);
        relevantSampleSelector.initialize();
        return relevantSampleSelector;
    }

    /**
     * Gets the relevant depth.
     *
     * @return the relevant depth
     */
    public int getRelevantDepth() {
        return relevantDepth;
    }

    /**
     * Sets the relevant depth of the search.
     *
     * @param relevantDepth the relevant depth of the search
     * @throws InitializationException if the {@link #relevantDepth} is already set
     */
    public void setRelevantDepth(int relevantDepth) throws InitializationException {
        if (!previousRelevants.isEmpty()) {
            throw new InitializationException(
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_AFTER_USE.toString(),
                                                 RELEVANT_DEPTH_FIELD_NAME));
        }
        this.relevantDepth = relevantDepth;
    }

}
