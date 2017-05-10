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

package br.ufrj.cos.knowledge.manager;

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.external.access.ExampleStream;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;

/**
 * Responsible for receiving the atomExamples from the {@link ExampleStream},
 * suggesting the {@link LearningSystem} to getBestRevisionOperator the theory, whenever it believes it is necessary.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
public abstract class IncomingExampleManager {

    protected final LearningSystem learningSystem;

    /**
     * Constructs a {@link IncomingExampleManager} with its fields.
     *
     * @param learningSystem the {@link LearningSystem}
     */
    public IncomingExampleManager(LearningSystem learningSystem) {
        this.learningSystem = learningSystem;
    }

    /**
     * Decides what to do with the arrived {@link Example}s.
     *
     * @param examples the arrived {@link Example}s
     * @throws TheoryRevisionException in an error occurs during the revision
     */
    public abstract void incomingExamples(Example... examples) throws TheoryRevisionException;

}
