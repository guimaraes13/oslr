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

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.RevisionOperator;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

import static br.ufrj.cos.util.log.RevisionLog.INITIALIZING_REVISION_OPERATOR_SELECTOR;

/**
 * Responsible for selecting the best suited {@link RevisionOperator}(s).
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 */
public abstract class RevisionOperatorSelector implements Initializable {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    protected Collection<RevisionOperatorEvaluator> operatorEvaluators;

    @Override
    public void initialize() throws InitializationException {
        logger.debug(INITIALIZING_REVISION_OPERATOR_SELECTOR.toString(), this.getClass().getName());
        if (operatorEvaluators == null || operatorEvaluators.isEmpty()) {
            throw new InitializationException(
                    ExceptionMessages.errorFieldsSet(this, RevisionOperatorEvaluator.class.getSimpleName()));
        }
        for (RevisionOperatorEvaluator operator : operatorEvaluators) {
            operator.initialize();
        }
    }

    /**
     * Selects the best suited {@link RevisionOperator} to be applied on the {@link Theory}, based on the metric.
     *
     * @param targets the target iterator
     * @param metric  the metric
     * @return the best suited {@link RevisionOperatorEvaluator}
     */
    public abstract RevisionOperatorEvaluator selectOperator(Collection<? extends Example> targets,
                                                             TheoryMetric metric);

    /**
     * Sets the {@link RevisionOperatorEvaluator} set if it is not yet set. If it is already set, throws an error.
     *
     * @param operatorEvaluators the {@link RevisionOperatorEvaluator} set
     * @throws InitializationException if the {@link RevisionOperatorEvaluator} is already set
     */
    public void setOperatorEvaluators(
            Collection<RevisionOperatorEvaluator> operatorEvaluators) throws InitializationException {
        if (isOperatorEvaluatorsSetted()) {
            throw new InitializationException(
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                 RevisionOperatorEvaluator.class.getSimpleName()));
        }
        this.operatorEvaluators = operatorEvaluators;
    }

    /**
     * Checks if the {@link #operatorEvaluators} is setted.
     *
     * @return {@code true} if it is, {@code false} otherwise
     */
    public boolean isOperatorEvaluatorsSetted() {
        return this.operatorEvaluators != null;
    }

}
