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

package br.ufrj.cos.util.multithreading;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.evaluation.AsyncTheoryEvaluator;
import br.ufrj.cos.logic.HornClause;

import java.util.Collection;
import java.util.Map;

/**
 * Encapsulates entries of HornClause and substitutions into the AsyncTheoryEvaluator.
 * <p>
 * Created on 25/06/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("unused")
public class ClauseSubstitutionAsyncTransformer<E> implements AsyncEvaluatorTransformer<Map.Entry<HornClause, E>, E> {

    @Override
    public AsyncTheoryEvaluator<E> transform(AsyncTheoryEvaluator<E> evaluator, Map.Entry<HornClause, E> mapEntry,
                                             Collection<? extends Example> examples) {
        evaluator.setHornClause(mapEntry.getKey());
        evaluator.setElement(mapEntry.getValue());

        return evaluator;
    }

}
