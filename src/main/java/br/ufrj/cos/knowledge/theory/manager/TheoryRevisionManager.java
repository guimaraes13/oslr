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

package br.ufrj.cos.knowledge.theory.manager;

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.evaluation.metric.probabilistic.RocCurveMetric;
import br.ufrj.cos.knowledge.theory.manager.revision.RevisionManager;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.RevisionOperatorEvaluator;
import br.ufrj.cos.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for applying the revision on theory, whenever it is called to.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
public class TheoryRevisionManager implements Initializable {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    /**
     * Represent no improvement threshold, i.e. any improvement is valid.
     */
    public static final double NO_IMPROVEMENT_THRESHOLD = 0.0;

    /**
     * The default theory metric.
     */
    public static final TheoryMetric DEFAULT_THEORY_METRIC = new RocCurveMetric();

    protected LearningSystem learningSystem;
    protected RevisionManager revisionManager;
    protected TheoryMetric theoryMetric;

    /**
     * Default constructor to be in compliance to {@link Initializable} interface.
     */
    public TheoryRevisionManager() {
    }

    /**
     * Constructs the class if the minimum required parameters
     *
     * @param learningSystem  the {@link LearningSystem}
     * @param revisionManager the {@link RevisionManager}
     */
    public TheoryRevisionManager(LearningSystem learningSystem, RevisionManager revisionManager) {
        this.learningSystem = learningSystem;
        this.revisionManager = revisionManager;
    }

    @Override
    public void initialize() throws InitializationException {
        List<String> fields = new ArrayList<>();
        if (learningSystem == null) {
            fields.add(LearningSystem.class.getSimpleName());
        }
        if (revisionManager == null) {
            fields.add(RevisionManager.class.getSimpleName());
        }

        if (!fields.isEmpty()) {
            throw new InitializationException(ExceptionMessages.errorFieldsSet(this, fields));
        }

        if (theoryMetric == null) {
            theoryMetric = DEFAULT_THEORY_METRIC;
        }
    }

    /**
     * Method to call the revision of the {@link Theory} on the {@link RevisionManager}
     *
     * @param targets the target {@link Example}s
     * @throws TheoryRevisionException in case an error occurs on the revision
     */
    public void revise(Iterable<? extends Example> targets) throws TheoryRevisionException {
        RevisionOperatorEvaluator revisionOperator = revisionManager.getBestRevisionOperator(targets);

        applyRevision(revisionOperator, targets, learningSystem.evaluateTheory(theoryMetric), NO_IMPROVEMENT_THRESHOLD);
    }

    /**
     * Compares the revision with the current theory, if the revision outperform the current theory by a given
     * threshold, applies the revision on the theory.
     *
     * @param operatorEvaluator     the revision operator
     * @param targets              the targets for the revision
     * @param currentEvaluation    the current evaluation value of the theory
     * @param improvementThreshold the improvement threshold
     * @throws TheoryRevisionException in case an error occurs on the revision
     */
    protected void applyRevision(RevisionOperatorEvaluator operatorEvaluator, Iterable<? extends Example> targets,
                                 double currentEvaluation, double improvementThreshold) throws TheoryRevisionException {
        double revised = operatorEvaluator.evaluateOperator(learningSystem.getExamples(), targets);

        int improve = theoryMetric.compare(revised, currentEvaluation);
        Theory currentTheory = learningSystem.getTheory();
        LogMessages logMessage;
        if (improve >= improvementThreshold) {
            Theory revisedTheory = operatorEvaluator.getRevisedTheory(targets);
            learningSystem.setTheory(revisedTheory);
            learningSystem.trainParameters(learningSystem.getExamples());
            learningSystem.saveTrainedParameters();
            operatorEvaluator.theoryRevisionAccepted(revisedTheory, currentTheory);
            logMessage = LogMessages.THEORY_MODIFICATION_ACCEPTED;
        } else {
            logMessage = LogMessages.THEORY_MODIFICATION_SKIPPED;
        }
        logger.debug(logMessage.toString(), improve, improvementThreshold);
        operatorEvaluator.clearCachedTheory();
    }

    /**
     * Sets the {@link RevisionManager} if it is not yet set. If it is already set, throws an error.
     *
     * @param revisionManager the {@link RevisionManager}
     * @throws InitializationException if the {@link RevisionManager} is already set
     */
    public void setRevisionManager(RevisionManager revisionManager) throws InitializationException {
        if (this.revisionManager != null) {
            throw new InitializationException(
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                   RevisionManager.class.getSimpleName()));
        }
        this.revisionManager = revisionManager;
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

    /**
     * Gets the theory metric.
     *
     * @return the theory metric
     */
    public TheoryMetric getTheoryMetric() {
        return theoryMetric;
    }

    /**
     * Sets the theory metric.
     *
     * @param theoryMetric the theory metric
     * @throws InitializationException if the {@link TheoryMetric} is already set
     */
    public void setTheoryMetric(TheoryMetric theoryMetric) throws InitializationException {
        if (this.theoryMetric != null) {
            throw new InitializationException(
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                   TheoryMetric.class.getSimpleName()));
        }
        this.theoryMetric = theoryMetric;
    }
}
