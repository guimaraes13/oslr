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

package br.ufrj.cos.knowledge.theory.manager.revision;

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.manager.feature.FeatureGenerator;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.RevisionOperator;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

import static br.ufrj.cos.util.log.RevisionLog.INITIALIZING_REVISION_OPERATOR_EVALUATOR;

/**
 * Responsible for evaluating an specific {@link RevisionOperator}.
 * <p>
 * This class is not thread-safe, if you want to perform multiple evaluations using the same {@link RevisionOperator}
 * with the same {@link TheoryMetric} in different {@link Theory}(is), please create a instance of this class for each
 * thread.
 * <p>
 * This class may cache the updated {@link Theory}, depends on the implementation.
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 */
public class RevisionOperatorEvaluator implements Initializable {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    protected RevisionOperator revisionOperator;

    protected Theory updatedTheory;
    protected boolean isRevised;

    /**
     * Constructs a {@link RevisionOperatorEvaluator} with its fields.
     *
     * @param revisionOperator the {@link RevisionOperator}
     */
    public RevisionOperatorEvaluator(RevisionOperator revisionOperator) {
        this.revisionOperator = revisionOperator;
    }

    /**
     * Default constructor to be in compliance to {@link Initializable} interface.
     */
    public RevisionOperatorEvaluator() {
    }

    @Override
    public void initialize() throws InitializationException {
        logger.debug(INITIALIZING_REVISION_OPERATOR_EVALUATOR.toString(), this.getClass().getName());
        revisionOperator.initialize();
    }

    /**
     * Evaluates a {@link Theory} as {@link RevisionOperator} was applied, based on the metric.
     *
     * @param targets the target {@link Example}s
     * @param metric  the metric
     * @return the evaluated value
     * @throws TheoryRevisionException in case an error occurs on the revision
     */
    public double evaluateOperator(Collection<? extends Example> targets,
                                   TheoryMetric metric) throws TheoryRevisionException {
        if (!isRevised) {
            updatedTheory = revisionOperator.performOperation(targets);
            isRevised = true;
        }
        if (updatedTheory == null) { return metric.getDefaultValue(); }
        return revisionOperator.getTheoryEvaluator().evaluateTheory(metric, updatedTheory, targets);
    }

    /**
     * Gets the revised theory. This method is useful because most of the {@link RevisionOperatorEvaluator} needs to
     * previously apply the change before evaluateTheory it. This methods allows it to store the revised
     * {@link Theory} to improve performance.
     * <p>
     * If the {@link Theory} was not created (or stored) it is computed on the call of this method.
     *
     * @param targets the target {@link Example}s
     * @return the revised {@link Theory}
     * @throws TheoryRevisionException in case an error occurs on the revision
     */
    public Theory getRevisedTheory(Collection<? extends Example> targets) throws TheoryRevisionException {
        if (isRevised) {
            isRevised = false;
            return updatedTheory;
        }
        return revisionOperator.performOperation(targets);
    }

    /**
     * Sets the {@link LearningSystem} if it is not yet set. If it is already set, throws an error.
     *
     * @param learningSystem the {@link LearningSystem}
     * @throws InitializationException if the {@link LearningSystem} is already set
     */
    public void setLearningSystem(LearningSystem learningSystem) throws InitializationException {
        revisionOperator.setLearningSystem(learningSystem);
    }

    /**
     * Sets the {@link FeatureGenerator} if it is not yet set. If it is already set, throws an error.
     *
     * @param featureGenerator the {@link FeatureGenerator}
     * @throws InitializationException if the {@link LearningSystem} is already set
     */
    public void setFeatureGenerator(FeatureGenerator featureGenerator) throws InitializationException {
        revisionOperator.setFeatureGenerator(featureGenerator);
    }

    /**
     * Gets the {@link RevisionOperator}.
     *
     * @return the {@link RevisionOperator}
     */
    public RevisionOperator getRevisionOperator() {
        return revisionOperator;
    }

    /**
     * Sets the {@link RevisionOperator} if it is not yet set. If it is already set, throws an error.
     *
     * @param revisionOperator the {@link RevisionOperator}
     * @throws InitializationException if the {@link RevisionOperator} is already set
     */
    public void setRevisionOperator(RevisionOperator revisionOperator) throws InitializationException {
        if (this.revisionOperator != null) {
            throw new InitializationException(
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                 RevisionOperator.class.getSimpleName()));
        }
        this.revisionOperator = revisionOperator;
    }

    /**
     * Clears the revised theory.
     */
    public void clearCachedTheory() {
        isRevised = false;
    }

    /**
     * Method to send a feedback to the revision operator, telling that the revision was accepted, in order to allow
     * it to do something.
     *
     * @param revised the revised theory
     */
    public void theoryRevisionAccepted(Theory revised) {
        revisionOperator.theoryRevisionAccepted(revised);
    }

    @Override
    public String toString() {
        return revisionOperator.getClass().getCanonicalName();
    }

    /**
     * Checks if the {@link RevisionOperator} calls the train of the parameters.
     *
     * @return {@code true} if it does, {@code false} otherwise
     */
    public boolean isTrained() {
        return revisionOperator.getTheoryMetric() != null &&
                revisionOperator.getTheoryMetric().parametersRetrainedBeforeEvaluate;
    }

}
