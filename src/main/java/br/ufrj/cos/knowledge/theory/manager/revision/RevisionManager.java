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

package br.ufrj.cos.knowledge.theory.manager.revision;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.manager.TheoryRevisionManager;
import br.ufrj.cos.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
     * Revises the theory based on the list of collection of examples.
     * <p>
     * It is recommended that the implementation of the IncomingExampleManager, the RevisionManager and the desired
     * operators share a common object, in such a way that they could send revision hints to each other, telling where
     * the revision points are in the theory.
     * <p>
     * The default implementation of this class do not look for those hints.
     *
     * @param revisionPoints the revision points
     */
    public void reviseTheory(List<? extends Collection<? extends Example>> revisionPoints) {
        for (Collection<? extends Example> revision : revisionPoints) {
            callRevision(revision);
        }
    }

    /**
     * Calls the revision on the collection of examples
     *
     * @param examples the examples
     * @return {@code true} if the revision was applied, {@code false} otherwise
     */
    protected boolean callRevision(Collection<? extends Example> examples) {
        try {
            return theoryRevisionManager.applyRevision(operatorSelector.selectOperator(examples), examples);
        } catch (TheoryRevisionException e) {
            logger.error(LogMessages.ERROR_REVISING_THEORY, e);
        }
        return false;
    }

    @Override
    public void initialize() throws InitializationException {
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
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
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
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                   TheoryRevisionManager.class.getSimpleName()));
        }
        this.theoryRevisionManager = theoryRevisionManager;
    }

}
