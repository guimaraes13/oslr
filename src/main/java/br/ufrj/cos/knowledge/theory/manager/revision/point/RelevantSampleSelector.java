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

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;

/**
 * Class to select if a example is relevant to be evaluated or not. All the examples of a revision point must be
 * passed to this class, and the relevance of one example might depends on a previously evaluated example.
 * <p>
 * Created on 09/07/17.
 *
 * @author Victor Guimarães
 */
public abstract class RelevantSampleSelector implements Initializable {

    protected LearningSystem learningSystem;

    @Override
    public void initialize() throws InitializationException {
        if (learningSystem == null) {
            throw new InitializationException(
                    ExceptionMessages.errorFieldsSet(this, LearningSystem.class.getSimpleName()));
        }
    }

    /**
     * Checks if all examples are relevants.
     *
     * @return {@code true} if it is, {@code false} otherwise
     */
    public abstract boolean isAllRelevants();

    /**
     * Checks if a example is relevant in a revision point.
     *
     * @param example the example
     * @return {@code true} if it is, {@code false} otherwise
     */
    public abstract boolean isRelevant(Example example);

    /**
     * Creates another instance of this class with the same parameters of this instance, which are independents from
     * the examples. The new instance must behave exactly as this one, except by the part that is based on the
     * previously evaluated examples.
     *
     * @return a copy of this instance
     * @throws InitializationException if something goes wrong during the copy
     */
    public abstract RelevantSampleSelector copy() throws InitializationException;

    /**
     * Gets the learning system.
     *
     * @return the learning system
     */
    public LearningSystem getLearningSystem() {
        return learningSystem;
    }

    /**
     * Sets the {@link LearningSystem} if it is not yet set. If it is already set, throws an error.
     *
     * @param learningSystem the {@link LearningSystem}
     * @throws InitializationException if the {@link LearningSystem} is already set
     */
    public void setLearningSystem(LearningSystem learningSystem) throws InitializationException {
        if (this.learningSystem != null) {
            throw new InitializationException(
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                 LearningSystem.class.getSimpleName()));
        }
        this.learningSystem = learningSystem;
    }
}
