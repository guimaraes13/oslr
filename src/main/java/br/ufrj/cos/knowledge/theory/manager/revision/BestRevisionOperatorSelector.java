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
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.util.InitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;

import static br.ufrj.cos.util.log.InferenceLog.ERROR_EVALUATING_REVISION_OPERATOR;

/**
 * Class that selects the best possible revision operator.
 * <p>
 * Created on 20/06/17.
 *
 * @author Victor Guimarães
 */
public class BestRevisionOperatorSelector extends RevisionOperatorSelector {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    RevisionOperatorEvaluatorSelector selector;

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        if (operatorEvaluators.size() < 2) {
            selector = new SingleRevisionOperatorEvaluator(operatorEvaluators);
        } else {
            selector = new BestSelector(operatorEvaluators);
        }
    }

    @Override
    public RevisionOperatorEvaluator selectOperator(Collection<? extends Example> targets, TheoryMetric metric) {
        return selector.selectOperator(targets, metric);
    }

    private interface RevisionOperatorEvaluatorSelector {

        /**
         * Selects the proper operator given the target examples, based on the metric.
         *
         * @param targets the target examples
         * @param metric  the metric
         * @return the operator
         */
        public RevisionOperatorEvaluator selectOperator(Collection<? extends Example> targets, TheoryMetric metric);

    }

    private static class SingleRevisionOperatorEvaluator implements RevisionOperatorEvaluatorSelector {

        @SuppressWarnings("CanBeFinal")
        protected RevisionOperatorEvaluator operatorEvaluator;

        private SingleRevisionOperatorEvaluator(Collection<RevisionOperatorEvaluator> operatorEvaluators) {
            Iterator<RevisionOperatorEvaluator> iterator = operatorEvaluators.iterator();
            if (iterator.hasNext()) {
                this.operatorEvaluator = iterator.next();
            } else {
                this.operatorEvaluator = null;
            }
        }

        @Override
        public RevisionOperatorEvaluator selectOperator(Collection<? extends Example> targets, TheoryMetric metric) {
            if (operatorEvaluator != null) {
                operatorEvaluator.clearCachedTheory();
            }
            return operatorEvaluator;
        }
    }

    private static class BestSelector implements RevisionOperatorEvaluatorSelector {

        protected final Collection<RevisionOperatorEvaluator> operatorEvaluators;
        protected final RevisionOperatorEvaluator preferred;

        public BestSelector(Collection<RevisionOperatorEvaluator> operatorEvaluators) {
            this.operatorEvaluators = operatorEvaluators;
            this.preferred = operatorEvaluators.iterator().next();
        }

        @Override
        public RevisionOperatorEvaluator selectOperator(Collection<? extends Example> targets, TheoryMetric metric) {
            RevisionOperatorEvaluator bestEvaluated = preferred;
            double bestEvaluation = metric.getDefaultValue();
            double current;

            for (RevisionOperatorEvaluator evaluator : operatorEvaluators) {
                try {
                    evaluator.clearCachedTheory();
                    current = evaluator.evaluateOperator(targets, metric);
                    if (metric.compare(current, bestEvaluation) > 0) {
                        bestEvaluation = current;
                        bestEvaluated = evaluator;
                    }
                } catch (TheoryRevisionException e) {
                    logger.warn(ERROR_EVALUATING_REVISION_OPERATOR.toString(), e);
                }
            }

            return bestEvaluated;
        }

    }

}