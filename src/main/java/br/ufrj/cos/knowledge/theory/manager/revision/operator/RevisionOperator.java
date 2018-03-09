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

package br.ufrj.cos.knowledge.theory.manager.revision.operator;

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.TheoryEvaluator;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.manager.feature.FeatureGenerator;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static br.ufrj.cos.util.log.RevisionLog.INITIALIZING_REVISION_OPERATOR;

/**
 * Responsible for changing the {@link Theory}.
 * <p>
 * There are two main types of {@link RevisionOperator}, generalization and specialisation. The former makes the
 * {@link Theory} more generic, proving more atomExamples. The later makes it more
 * specific,
 * proving less atomExamples.
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 */
public abstract class RevisionOperator implements Initializable {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    protected LearningSystem learningSystem;

    /**
     * The internal metric of the operator, this metric may not be used to evaluated the theory by applying the
     * operator.
     */
    protected TheoryMetric theoryMetric;

    protected FeatureGenerator featureGenerator;

    @Override
    public void initialize() throws InitializationException {
        logger.debug(INITIALIZING_REVISION_OPERATOR.toString(), this.getClass().getName());
        List<String> fields = new ArrayList<>();
        if (learningSystem == null) {
            fields.add(LearningSystem.class.getSimpleName());
        }
        if (featureGenerator == null) {
            fields.add(FeatureGenerator.class.getSimpleName());
        }

        if (!fields.isEmpty()) {
            throw new InitializationException(ExceptionMessages.errorFieldsSet(this, fields));
        }
    }

    /**
     * Apply the operation on its {@link Theory} given the target {@link Example}
     *
     * @param targets the targets {@link Example}s
     * @return the {@link Theory}
     * @throws TheoryRevisionException in an error occurs during the revision
     */
    public abstract Theory performOperation(Collection<? extends Example> targets) throws TheoryRevisionException;

    /**
     * Gets the {@link TheoryMetric}.
     *
     * @return the {@link TheoryMetric}
     */
    public TheoryMetric getTheoryMetric() {
        return theoryMetric;
    }

    /**
     * Sets the {@link TheoryMetric} if it is not yet set. If it is already set, throws an error.
     *
     * @param theoryMetric the {@link TheoryMetric}
     * @throws InitializationException if the {@link TheoryMetric} is already set
     */
    public void setTheoryMetric(TheoryMetric theoryMetric) throws InitializationException {
        if (this.theoryMetric != null) {
            throw new InitializationException(
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                 TheoryMetric.class.getSimpleName()));
        }
        this.theoryMetric = theoryMetric;
    }

    /**
     * Gets the {@link TheoryEvaluator}.
     *
     * @return the {@link TheoryEvaluator}
     */
    public TheoryEvaluator getTheoryEvaluator() {
        return learningSystem.getTheoryEvaluator();
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
     * Sets the {@link FeatureGenerator} if it is not yet set. If it is already set, throws an error.
     *
     * @param featureGenerator the {@link FeatureGenerator}
     * @throws InitializationException if the {@link LearningSystem} is already set
     */
    public void setFeatureGenerator(FeatureGenerator featureGenerator) throws InitializationException {
        if (this.featureGenerator != null) {
            throw new InitializationException(
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                 FeatureGenerator.class.getSimpleName()));
        }
        this.featureGenerator = featureGenerator;
    }

    /**
     * Method to send a feedback to the revision operator, telling that the revision was accepted, in order to allow
     * it to do something.
     *
     * @param revised the revised theory
     */
    @SuppressWarnings("unused")
    public abstract void theoryRevisionAccepted(Theory revised);

}
