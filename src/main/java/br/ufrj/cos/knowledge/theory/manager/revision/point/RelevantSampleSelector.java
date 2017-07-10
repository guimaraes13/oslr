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

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;

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
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                   LearningSystem.class.getSimpleName()));
        }
        this.learningSystem = learningSystem;
    }
}
