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

package br.ufrj.cos.knowledge.theory.manager.revision.operator;

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.TheoryEvaluator;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;

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
public abstract class RevisionOperator {

    protected final LearningSystem learningSystem;

    /**
     * The external metric of the operator, this metric will be used to evaluated the theory by applying the operator.
     * In addition, a operator might have internal metrics to take internal decision.
     */
    protected final TheoryMetric theoryMetric;

    /**
     * Constructs the class if the minimum required parameters
     *
     * @param learningSystem the {@link LearningSystem}
     * @param theoryMetric   the {@link TheoryMetric}
     */
    public RevisionOperator(LearningSystem learningSystem, TheoryMetric theoryMetric) {
        this.learningSystem = learningSystem;
        this.theoryMetric = theoryMetric;
    }

    /**
     * Apply the operation on its {@link Theory} given the target {@link Example}
     *
     * @param targets the targets {@link Example}s
     * @return the {@link Theory}
     * @throws TheoryRevisionException in an error occurs during the revision
     */
    public abstract Theory performOperation(Example... targets) throws TheoryRevisionException;

    /**
     * Gets the {@link TheoryMetric}.
     *
     * @return the {@link TheoryMetric}
     */
    public TheoryMetric getTheoryMetric() {
        return theoryMetric;
    }

    /**
     * Gets the {@link TheoryEvaluator}.
     *
     * @return the {@link TheoryEvaluator}
     */
    public TheoryEvaluator getTheoryEvaluator() {
        return learningSystem.getTheoryEvaluator();
    }

}
