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

import br.ufrj.cos.knowledge.theory.manager.TheoryRevisionManager;
import br.ufrj.cos.knowledge.theory.manager.revision.point.RevisionExamples;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static br.ufrj.cos.util.log.RevisionLog.ERROR_REVISING_THEORY;
import static br.ufrj.cos.util.log.RevisionLog.INITIALIZING_REVISION_MANAGER;

/**
 * Responsible for applying the revision operator on the {@link br.ufrj.cos.knowledge.theory.Theory}.
 * <p>
 * It is recommended that the implementation of the IncomingExampleManager, the RevisionManager and the desired
 * operators share a common object, in such a way that they could send revision hints to each other, telling where
 * the revision points are in the theory.
 * <p>
 * The default implementation of this class do not look for those hints.
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 */
public class RevisionManager implements Initializable {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    protected TheoryRevisionManager theoryRevisionManager;
    protected RevisionOperatorSelector operatorSelector;

    /**
     * Revises the theory based on the list of collection of examples, based on the metric.
     * <p>
     * It is recommended that the implementation of the IncomingExampleManager, the RevisionManager and the desired
     * operators share a common object, in such a way that they could send revision hints to each other, telling where
     * the revision points are in the theory.
     * <p>
     * The default implementation of this class do not look for those hints.
     *
     * @param revisionPoints        the revision points
     * @param trainUsingAllExamples if is to train using all examples or just the relevant sample
     */
    public void reviseTheory(List<? extends RevisionExamples> revisionPoints,
                             final boolean trainUsingAllExamples) {
        for (RevisionExamples revision : revisionPoints) {
            callRevision(revision);
        }
    }

    /**
     * Calls the revision chosen by the {@link RevisionOperatorSelector}, based on the metric, on the collection of
     * examples.
     *
     * @param examples the revision examples
     * @return {@code true} if the revision was applied, {@code false} otherwise
     */
    protected boolean callRevision(RevisionExamples examples) {
        try {
            return theoryRevisionManager.applyRevision(operatorSelector, examples);
        } catch (TheoryRevisionException e) {
            logger.error(ERROR_REVISING_THEORY, e);
        }
        return false;
    }

    @Override
    public void initialize() throws InitializationException {
        logger.debug(INITIALIZING_REVISION_MANAGER.toString(), this.getClass().getName());
        List<String> fields = new ArrayList<>();
        if (operatorSelector == null) {
            fields.add(RevisionOperatorSelector.class.getSimpleName());
        }
        if (theoryRevisionManager == null) {
            fields.add(TheoryRevisionManager.class.getSimpleName());
        }
        if (!fields.isEmpty()) {
            throw new InitializationException(ExceptionMessages.errorFieldsSet(this, fields));
        }
        operatorSelector.initialize();
    }

    /**
     * Sets the {@link RevisionOperatorSelector} if it is not yet set. If it is already set, throws an error.
     *
     * @param operatorSelector the {@link RevisionOperatorSelector}
     * @throws InitializationException if the {@link RevisionOperatorSelector} is already set
     */
    public void setOperatorSelector(RevisionOperatorSelector operatorSelector) throws InitializationException {
        if (this.operatorSelector != null) {
            throw new InitializationException(
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                 RevisionOperatorSelector.class.getSimpleName()));
        }
        this.operatorSelector = operatorSelector;
    }

    /**
     * Sets the {@link TheoryRevisionManager} if it is not yet set. If it is already set, throws an error.
     *
     * @param theoryRevisionManager the {@link TheoryRevisionManager}
     * @throws InitializationException if the {@link TheoryRevisionManager} is already set
     */
    public void setTheoryRevisionManager(TheoryRevisionManager theoryRevisionManager) throws InitializationException {
        if (this.theoryRevisionManager != null) {
            throw new InitializationException(
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                 TheoryRevisionManager.class.getSimpleName()));
        }
        this.theoryRevisionManager = theoryRevisionManager;
    }

}
