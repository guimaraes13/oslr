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

    protected LearningSystem learningSystem;
    protected RevisionManager revisionManager;

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

        if (fields.size() > 0) {
            throw new InitializationException(ExceptionMessages.errorFieldsSet(this, fields));
        }
    }

    /**
     * Method to call the revision of the {@link Theory} on the {@link RevisionManager}
     *
     * @param targets the target {@link Example}s
     * @throws TheoryRevisionException in case an error occurs on the revision
     */
    public void revise(Example... targets) throws TheoryRevisionException {
        RevisionOperatorEvaluator revisionOperator = revisionManager.getBestRevisionOperator(targets);
        TheoryMetric metric = revisionOperator.getTheoryMetric();
        double current = learningSystem.evaluateTheory(metric);
        double revised = revisionOperator.evaluateOperator(learningSystem.getExamples(), targets);

        if (metric.compare(revised, current) >= 0) {
            learningSystem.setTheory(revisionOperator.getRevisedTheory(targets));
            learningSystem.trainParameters(learningSystem.getExamples());
            learningSystem.saveTrainedParameters();
        } else {
            logger.debug(LogMessages.THEORY_MODIFICATION_SKIPPED);
        }
        revisionOperator.clearCachedTheory();
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

}
