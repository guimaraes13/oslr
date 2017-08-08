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
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.time.TimeUtils;

import java.util.*;

/**
 * Class to keep the examples to be used on the revision.
 * <p>
 * Created on 08/07/17.
 *
 * @author Victor Guimarães
 */
public class RevisionExamples {

    protected final Collection<Example> incomingExamples;
    protected final Collection<Example> relevantSample;
    protected final Map<Example, Map<Atom, Double>> inferredExamples;
    protected final Set<Example> notEvaluatedExamples;
    protected long lastInference;
    protected LearningSystem learningSystem;
    protected RelevantSampleSelector sampleSelector;

    /**
     * Default constructor.
     *
     * @param learningSystem the {@link LearningSystem}
     * @param sampleSelector the {@link RelevantSampleSelector}
     */
    public RevisionExamples(LearningSystem learningSystem,
                            RelevantSampleSelector sampleSelector) {
        this.learningSystem = learningSystem;
        this.sampleSelector = sampleSelector;
        this.incomingExamples = new HashSet<>();
        this.relevantSample = new HashSet<>();
        this.inferredExamples = new HashMap<>();
        this.notEvaluatedExamples = new HashSet<>();
    }

    /**
     * Gets the training examples based on the parameter. If is to train using all examples, returns all the incoming
     * examples, otherwise returns the relevant sample.
     *
     * @param trainUsingAllExamples if is to train using all examples
     * @return all the incoming examples if trainUsingAllExamples if {@code true}, otherwise returns the relevant
     * sample.
     */
    public Collection<? extends Example> getTrainingExamples(boolean trainUsingAllExamples) {
        return trainUsingAllExamples ? getIncomingExamples() : getRelevantSample();
    }

    /**
     * Gets the incoming examples.
     *
     * @return the incoming examples
     */
    protected Collection<Example> getIncomingExamples() {
        return incomingExamples;
    }

    /**
     * Gets the relevant sample.
     *
     * @return the relevant sample
     */
    public Collection<Example> getRelevantSample() {
        return relevantSample;
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

    /**
     * Adds the examples to revise.
     *
     * @param examples the examples
     */
    public void addExample(Iterable<? extends Example> examples) {
        for (Example example : examples) {
            addExample(example);
        }
    }

    /**
     * Adds the example to revise.
     *
     * @param example the example
     */
    public void addExample(Example example) {
        incomingExamples.add(example);
        if (sampleSelector.isRelevant(example)) {
            relevantSample.add(example);
            notEvaluatedExamples.add(example);
        }
    }

    /**
     * Adds the example to revise.
     *
     * @param example  the example
     * @param inferred the inferred values of the examples
     */
    public void addExample(Example example, Map<Atom, Double> inferred) {
        incomingExamples.add(example);
        if (sampleSelector.isRelevant(example)) {
            relevantSample.add(example);
            inferredExamples.put(example, inferred);
        }
    }

    /**
     * Gets the sample selector.
     *
     * @return the sample selector
     */
    public RelevantSampleSelector getSampleSelector() {
        return sampleSelector;
    }

    /**
     * Sets the {@link RelevantSampleSelector} if it is not yet set. If it is already set, throws an error.
     *
     * @param sampleSelector the {@link RelevantSampleSelector}
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

    /**
     * Gets the inferred examples. If the inferred examples were based on an older version of the theory, it is
     * re-inferred.
     *
     * @param theoryLastChange the time of the last change on the theory
     * @return the inferred examples
     * @see TimeUtils
     */
    public Map<Example, Map<Atom, Double>> getInferredExamples(long theoryLastChange) {
        if (lastInference - theoryLastChange < 0) {
            clearInferredExamples();
        }
        if (!notEvaluatedExamples.isEmpty()) {
            inferredExamples.putAll(learningSystem.inferExamples(notEvaluatedExamples));
            notEvaluatedExamples.clear();
            lastInference = TimeUtils.getNanoTime();
        }
        return inferredExamples;
    }

    /**
     * Clears the cached inference values.
     */
    public void clearInferredExamples() {
        inferredExamples.clear();
        notEvaluatedExamples.addAll(relevantSample);
    }

    /**
     * Checks if the revision examples is empty. It is contains no examples.
     *
     * @return {@code true} if it is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return incomingExamples.isEmpty();
    }

}
