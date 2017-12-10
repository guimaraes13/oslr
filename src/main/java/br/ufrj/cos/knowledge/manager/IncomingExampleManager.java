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
import br.ufrj.cos.knowledge.theory.manager.revision.point.RelevantSampleSelector;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static br.ufrj.cos.util.log.IncomingExampleLog.INITIALIZING_INCOMING_EXAMPLE_MANAGER;

/**
 * Responsible for receiving the atomExamples from the {@link ExampleStream},
 * suggesting the {@link LearningSystem} to getBestRevisionOperator the theory, whenever it believes it is necessary.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
public abstract class IncomingExampleManager implements Initializable {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    protected LearningSystem learningSystem;
    protected RelevantSampleSelector sampleSelector;

    /**
     * Default constructor to be in compliance to {@link Initializable} interface.
     */
    protected IncomingExampleManager() {
    }

    /**
     * Constructs a {@link IncomingExampleManager} with its fields.
     *
     * @param learningSystem the {@link LearningSystem}
     * @param sampleSelector the {@link RelevantSampleSelector}
     */
    protected IncomingExampleManager(LearningSystem learningSystem,
                                     RelevantSampleSelector sampleSelector) {
        this.learningSystem = learningSystem;
        this.sampleSelector = sampleSelector;
    }

    @Override
    public void initialize() throws InitializationException {
        logger.debug(INITIALIZING_INCOMING_EXAMPLE_MANAGER.toString(), this.getClass().getName());
        List<String> fields = new ArrayList<>();
        if (learningSystem == null) {
            fields.add(LearningSystem.class.getSimpleName());
        }
        if (sampleSelector == null) {
            fields.add(RelevantSampleSelector.class.getSimpleName());
        }

        if (!fields.isEmpty()) {
            throw new InitializationException(ExceptionMessages.errorFieldsSet(this, fields));
        }
        sampleSelector.setLearningSystem(learningSystem);
        sampleSelector.initialize();
    }

    /**
     * Decides what to do with the arrived {@link Example}.
     *
     * @param example the arrived {@link Example}s
     */
    public void incomingExamples(Example example) {
        incomingExamples(Collections.singletonList(example));
    }

    /**
     * Decides what to do with the arrived {@link Example}s.
     *
     * @param examples the arrived {@link Example}s
     */
    public abstract void incomingExamples(Iterable<? extends Example> examples);

    /**
     * Gets the remaining examples that were not used on the revision.
     *
     * @return the remaining examples not used on the revision.
     */
    public abstract Collection<? extends Example> getRemainingExamples();

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

    /**
     * Gets the relevant sample selector.
     *
     * @return the relevant sample selector
     */
    public RelevantSampleSelector getSampleSelector() {
        return sampleSelector;
    }

    /**
     * Sets the relevant sample selector.
     *
     * @param sampleSelector the relevant sample selector
     * @throws InitializationException if the {@link RelevantSampleSelector} is already set
     */
    public void setSampleSelector(RelevantSampleSelector sampleSelector) throws InitializationException {
        if (this.sampleSelector != null) {
            throw new InitializationException(
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                 RelevantSampleSelector.class.getSimpleName()));
        }
        this.sampleSelector = sampleSelector;
    }

}
