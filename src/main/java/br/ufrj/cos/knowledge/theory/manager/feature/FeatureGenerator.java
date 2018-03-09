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

package br.ufrj.cos.knowledge.theory.manager.feature;

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

import static br.ufrj.cos.util.log.RevisionLog.INITIALIZING_FEATURE_GENERATOR;

/**
 * Responsible for creating feature for proposed rules.
 * <p>
 * This is an opportunity to take advantage of the {@link br.ufrj.cos.engine.EngineSystemTranslator}.
 * <p>
 * Created on 13/10/2017.
 *
 * @author Victor Guimarães
 */
public abstract class FeatureGenerator implements Initializable {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    protected LearningSystem learningSystem;

    @Override
    public void initialize() throws InitializationException {
        logger.debug(INITIALIZING_FEATURE_GENERATOR.toString(), this.getClass().getName());
        if (learningSystem == null) {
            throw new InitializationException(
                    ExceptionMessages.errorFieldsSet(this, LearningSystem.class.getSimpleName()));
        }
    }

    /**
     * Creates proper features to take advantage of the {@link br.ufrj.cos.engine.EngineSystemTranslator}.
     *
     * @param rule     the proposed rule
     * @param examples the examples to optimize the features
     * @return the rule with the features to be interpreted by the {@link br.ufrj.cos.engine.EngineSystemTranslator}
     */
    public abstract HornClause createFeatureForRule(HornClause rule, Collection<? extends Example> examples);

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

}
