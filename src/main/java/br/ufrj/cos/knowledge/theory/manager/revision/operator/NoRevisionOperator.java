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

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;

import java.util.Collection;

/**
 * A operator that does nothing.
 * <p>
 * Test proposes only.
 * <p>
 * Created on 19/11/2017.
 *
 * @author Victor Guimarães
 */
public class NoRevisionOperator extends RevisionOperator {

    @Override
    public Theory performOperation(Collection<? extends Example> targets) throws TheoryRevisionException {
        return learningSystem.getTheory();
    }

    @Override
    public void theoryRevisionAccepted(Theory revised) {

    }

}
