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

package br.ufrj.cos.knowledge.theory.manager.revision.point;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;

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
    public int relevantDepth = DEFAULT_RELEVANT_DEPTH;

    /**
     * Default constructor.
     */
    public IndependentSampleSelector() {
        previousRelevants = new HashSet<>();
    }

    @Override
    public boolean isRelevant(Example example) {
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
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_AFTER_USE.toString(),
                                                   RELEVANT_DEPTH_FIELD_NAME));
        }
        if (relevantDepth > DEFAULT_RELEVANT_DEPTH) { this.relevantDepth = relevantDepth; }
    }

}
