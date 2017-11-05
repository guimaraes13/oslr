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

import br.ufrj.cos.knowledge.KnowledgeException;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.AsyncTheoryEvaluator;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Conjunction;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.Literal;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.LanguageUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Class of operator that given an rule and a set of examples, finds a new literal to append, into the body of the
 * rule, that improves the rule's classification of the examples.
 * <p>
 * Created on 23/06/17.
 *
 * @author Victor Guimarães
 */
public abstract class LiteralAppendOperator extends RevisionOperator {

    /**
     * The default value of the {@link #generateFeatureBeforeEvaluate}.
     */
    public static final boolean DEFAULT_FEATURE_BEFORE_EVALUATE = false;
    /**
     * If {@code true}, generates the feature before evaluating the rule to decide which one is the best.
     */
    @SuppressWarnings("CanBeFinal")
    public boolean generateFeatureBeforeEvaluate = DEFAULT_FEATURE_BEFORE_EVALUATE;

    @Override
    public Theory performOperation(Collection<? extends Example> targets) throws TheoryRevisionException {
        try {
            Theory theory = learningSystem.getTheory().copy();
            HornClause initialClause = buildEmptyClause(targets);
            if (initialClause == null) { return theory; }

            HornClause hornClause = buildExtendedHornClause(targets, initialClause, new HashSet<>()).getHornClause();
            hornClause = featureGenerator.createFeatureForRule(hornClause, targets);
            theory.add(hornClause);
            return theory;
        } catch (KnowledgeException e) {
            throw new TheoryRevisionException(ExceptionMessages.ERROR_DURING_THEORY_COPY.toString(), e);
        }
    }

    /**
     * Returns a initial empty clause, from the first target example in targets, by putting the head of the clause as
     * the predicate of the example, replacing the terms by new distinct variables.
     *
     * @param targets the target
     * @return the empty clause
     */
    protected static HornClause buildEmptyClause(Iterable<? extends Example> targets) {
        Iterator<? extends Example> iterator = targets.iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        Atom atom = iterator.next().getAtom();
        Atom head = LanguageUtils.toVariableAtom(atom.getPredicate());
        return new HornClause(head, new Conjunction());
    }

    /**
     * Method to build a {@link HornClause}, that improves the metric on the examples, based on the initial clause.
     * This method creates a {@link HornClause} by adding a new literal to the body of the initialClause, if possible.
     * <p>
     * This method should not modify the initial clause nor generate literals equivalent to the, possibly empty,
     * collection of equivalentLiterals.
     *
     * @param examples           the examples
     * @param initialClause      the initial clause
     * @param equivalentLiterals the equivalent literals
     * @return the horn clause
     * @throws TheoryRevisionException in an error occurs during the revision
     */
    @SuppressWarnings("RedundantThrows")
    public abstract AsyncTheoryEvaluator buildExtendedHornClause(Collection<? extends Example> examples,
                                                                 HornClause initialClause,
                                                                 Collection<? extends Literal> equivalentLiterals)
            throws TheoryRevisionException;

}
