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

package br.ufrj.cos.util.multithreading;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.evaluation.AsyncTheoryEvaluator;
import br.ufrj.cos.knowledge.theory.manager.feature.FeatureGenerator;
import br.ufrj.cos.logic.Conjunction;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.Literal;

import java.util.Collection;
import java.util.Set;

/**
 * Encapsulates extended HornClauses, from a initial HornClause and a new literal, into AsyncTheoryEvaluators.
 * <p>
 * Created on 25/06/17.
 *
 * @author Victor Guimarães
 */
public class ConjunctionAppendAsyncTransformer<K> implements AsyncEvaluatorTransformer<Set<? extends Literal>, K> {

    protected HornClause initialClause;
    protected FeatureGenerator featureGenerator;

    /**
     * Default constructor without parameters.
     */
    public ConjunctionAppendAsyncTransformer() {
    }

    /**
     * Constructor with the feature generator.
     *
     * @param featureGenerator the feature generator.
     */
    public ConjunctionAppendAsyncTransformer(FeatureGenerator featureGenerator) {
        this.featureGenerator = featureGenerator;
    }

    @Override
    public AsyncTheoryEvaluator<K> transform(AsyncTheoryEvaluator<K> evaluator, Set<? extends Literal> conjunction,
                                             Collection<? extends Example> examples) {
        HornClause clause = new HornClause(initialClause.getHead(), new Conjunction(initialClause.getBody()));
        clause.getBody().addAll(conjunction);
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
