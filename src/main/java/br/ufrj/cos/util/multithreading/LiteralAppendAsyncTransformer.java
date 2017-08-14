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

import br.ufrj.cos.knowledge.theory.evaluation.AsyncTheoryEvaluator;
import br.ufrj.cos.logic.Conjunction;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.Literal;

/**
 * Encapsulates extended HornClauses, from a initial HornClause and a new literal, into AsyncTheoryEvaluators.
 * <p>
 * Created on 25/06/17.
 *
 * @author Victor Guimarães
 */
public class LiteralAppendAsyncTransformer implements AsyncEvaluatorTransformer<Literal, Object> {

    protected HornClause initialClause;

    @Override
    public AsyncTheoryEvaluator<Object> transform(AsyncTheoryEvaluator<Object> evaluator, Literal literal) {
        HornClause clause = new HornClause(initialClause.getHead(), new Conjunction(initialClause.getBody()));
        clause.getBody().add(literal);
        evaluator.setHornClause(clause);
        return evaluator;
    }

    /**
     * Gets the initial clause.
     *
     * @return the initial clause
     */
    public HornClause getInitialClause() {
        return initialClause;
    }

    /**
     * Sets the initial clause.
     *
     * @param initialClause the initial clause
     */
    public void setInitialClause(HornClause initialClause) {
        this.initialClause = initialClause;
    }
}
