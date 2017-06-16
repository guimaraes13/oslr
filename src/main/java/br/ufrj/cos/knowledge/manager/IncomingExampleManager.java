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

package br.ufrj.cos.knowledge.manager;

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.external.access.ExampleStream;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for receiving the atomExamples from the {@link ExampleStream},
 * suggesting the {@link LearningSystem} to getBestRevisionOperator the theory, whenever it believes it is necessary.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
public abstract class IncomingExampleManager implements Initializable {

    protected LearningSystem learningSystem;

    /**
     * Default constructor to be in compliance to {@link Initializable} interface.
     */
    protected IncomingExampleManager() {
    }

    /**
     * Constructs a {@link IncomingExampleManager} with its fields.
     *
     * @param learningSystem the {@link LearningSystem}
     */
    protected IncomingExampleManager(LearningSystem learningSystem) {
        this.learningSystem = learningSystem;
    }

    @Override
    public void initialize() throws InitializationException {
        if (learningSystem == null) {
            throw new InitializationException(
                    ExceptionMessages.errorFieldsSet(this, LearningSystem.class.getSimpleName()));
        }
    }

    /**
     * Decides what to do with the arrived {@link Example}.
     *
     * @param example the arrived {@link Example}s
     * @throws TheoryRevisionException in an error occurs during the revision
     */
    public void incomingExamples(Example example) throws TheoryRevisionException {
        List<Example> exampleList = new ArrayList<>(1);
        exampleList.add(example);
        incomingExamples(exampleList);
    }

    /**
     * Decides what to do with the arrived {@link Example}s.
     *
     * @param examples the arrived {@link Example}s
     * @throws TheoryRevisionException in an error occurs during the revision
     */
    public abstract void incomingExamples(Iterable<? extends Example> examples) throws TheoryRevisionException;

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
