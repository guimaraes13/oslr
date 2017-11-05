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
import br.ufrj.cos.knowledge.theory.manager.feature.FeatureGenerator;
import br.ufrj.cos.logic.Conjunction;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.Literal;

import java.util.Collection;

/**
 * Encapsulates extended HornClauses, from a initial HornClause and a new literal, into AsyncTheoryEvaluators.
 * <p>
 * Created on 25/06/17.
 *
 * @author Victor Guimarães
 */
public class LiteralAppendAsyncTransformer<K> implements AsyncEvaluatorTransformer<Literal, K> {

    protected HornClause initialClause;
    protected FeatureGenerator featureGenerator;

    /**
     * Default constructor without parameters.
     */
    public LiteralAppendAsyncTransformer() {
    }

    /**
     * Constructor with the feature generator.
     *
     * @param featureGenerator the feature generator.
     */
    public LiteralAppendAsyncTransformer(FeatureGenerator featureGenerator) {
        this.featureGenerator = featureGenerator;
    }

    @Override
    public AsyncTheoryEvaluator<K> transform(AsyncTheoryEvaluator<K> evaluator, Literal literal,
                                             Collection<? extends Example> examples) {
        HornClause clause = new HornClause(initialClause.getHead(), new Conjunction(initialClause.getBody()));
        clause.getBody().add(literal);
        if (featureGenerator != null && examples != null) {
            clause = featureGenerator.createFeatureForRule(clause, examples);
        }
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

    /**
     * Gets the {@link FeatureGenerator}.
     *
     * @return the {@link FeatureGenerator}
     */
    public FeatureGenerator getFeatureGenerator() {
        return featureGenerator;
    }

    /**
     * Sets the {@link FeatureGenerator}.
     *
     * @param featureGenerator the {@link FeatureGenerator}
     */
    public void setFeatureGenerator(FeatureGenerator featureGenerator) {
        this.featureGenerator = featureGenerator;
    }
}
