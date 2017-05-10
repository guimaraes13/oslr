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

package br.ufrj.cos.knowledge.theory.manager.revision.operator.generalization;

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.RevisionOperator;

/**
 * Represents a generalization operator i.e. an operator that, if applied, makes the Theory more general
 * (proving more atomExamples).
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 */
public abstract class GeneralizationRevisionOperator extends RevisionOperator {

    /**
     * Constructs the class if the minimum required parameters
     *
     * @param learningSystem the {@link LearningSystem}
     * @param theoryMetric   the {@link TheoryMetric}
     */
    public GeneralizationRevisionOperator(LearningSystem learningSystem, TheoryMetric theoryMetric) {
        super(learningSystem, theoryMetric);
    }

}
