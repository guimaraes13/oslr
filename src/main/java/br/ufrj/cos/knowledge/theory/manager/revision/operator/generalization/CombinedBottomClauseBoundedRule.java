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

import br.ufrj.cos.knowledge.KnowledgeException;
import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.logic.*;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.LanguageUtils;
import br.ufrj.cos.util.VariableGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static br.ufrj.cos.util.log.PreRevisionLog.RULE_APPENDED_TO_THEORY;
import static br.ufrj.cos.util.log.RevisionLog.*;

/**
 * Class to create a single rule from a set of examples by choosing literals from the combined bottom clause from all
 * the examples.
 * <p>
 * Created on 10/08/17.
 *
 * @author Victor Guimarães
 */
public class CombinedBottomClauseBoundedRule extends BottomClauseBoundedRule {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    @Override
    public Theory performOperation(Collection<? extends Example> targets) throws TheoryRevisionException {
        try {
            logger.info(PERFORMING_OPERATION_ON_EXAMPLES.toString(), targets.size());
            Theory theory = learningSystem.getTheory().copy();
            performOperationForExamples(targets, theory);
            return theory;
        } catch (KnowledgeException e) {
            throw new TheoryRevisionException(ExceptionMessages.ERROR_DURING_THEORY_COPY.toString(), e);
        }
    }

    /**
     * Performs the operation for a all the examples combined.
     *
     * @param targets the examples
     * @param theory  the theory
     */
    protected void performOperationForExamples(Collection<? extends Example> targets, Theory theory) {
        HornClause newRule;
        Map<Predicate, List<Example>> examplesByPredicate =
                targets.stream().collect(Collectors.groupingBy(e -> e.getGoalQuery().getPredicate()));
        logger.info(FOUND_PREDICATES.toString(), examplesByPredicate.keySet().size());
        Predicate predicate;
        List<Example> examples;
        for (Map.Entry<Predicate, List<Example>> entry : examplesByPredicate.entrySet()) {
            try {
                predicate = entry.getKey();
                examples = entry.getValue();
                logger.info(BUILDING_CLAUSE_FROM_PREDICATE_EXAMPLES.toString(), predicate, examples.size());
                HornClause bottomClause = buildCombinedBottomClause(predicate, examples);
                logger.info(BOTTOM_CLAUSE_SIZE.toString(), bottomClause.getBody().size());
                newRule = buildRuleFromBottomClause(targets, bottomClause);
                if (theory.add(newRule)) {
                    logger.info(RULE_APPENDED_TO_THEORY.toString(), newRule);
                }
            } catch (TheoryRevisionException | IllegalAccessException | InstantiationException e) {
                logger.trace(ERROR_REVISING_EXAMPLE, e);
            }
        }
    }

    /**
     * Builds the bottom clause from the combination of the bottom clause from the examples.
     *
     * @param predicate the predicate of the examples
     * @param examples  the examples
     * @return the combined bottom clause
     * @throws IllegalAccessException if an error occurs when instantiating a new list of {@link Term}s
     * @throws InstantiationException if an error occurs when instantiating a new list of {@link Term}s
     */
    protected HornClause buildCombinedBottomClause(Predicate predicate, Collection<? extends Example> examples) throws
            IllegalAccessException, InstantiationException {
        Set<Term> positiveTerms = examples.stream()
                .flatMap(e -> e.getPositiveTerms().stream()).collect(Collectors.toSet());
        Set<Atom> relevants = learningSystem.relevantsBreadthFirstSearch(positiveTerms, relevantsDepth, !refine);
        Set<Atom> groundedAtoms = examples.stream().flatMap(e -> e.getGroundedQuery().stream())
                .filter(AtomExample::isPositive).map(AtomExample::getAtom).collect(Collectors.toSet());

        //QUESTION: to variable examples individually?
        //TODO: pass the variable map as parameter
        return buildVariableBottomClause(predicate, relevants, groundedAtoms);
    }

    /**
     * Builds the variable bottom clause from the relevants and the grounded examples.
     *
     * @param predicate        the predicate of the examples
     * @param relevants        the relevants to the examples
     * @param groundedExamples the grounded examples
     * @return the variable bottom clause combined from the examples
     * @throws IllegalAccessException if an error occurs when instantiating a new list of {@link Term}s
     * @throws InstantiationException if an error occurs when instantiating a new list of {@link Term}s
     */
    @SuppressWarnings("OverlyCoupledMethod")
    protected HornClause buildVariableBottomClause(Predicate predicate, Set<Atom> relevants,
                                                   Set<Atom> groundedExamples)
            throws InstantiationException, IllegalAccessException {
        Map<Term, Term> variableMap = new HashMap();
        VariableGenerator variableGenerator = variableGeneratorClass.newInstance();
        Conjunction conjunction = new Conjunction(relevants.size());
        List<Term> exampleVariables = getExampleVariables(predicate.getArity(), variableGenerator);
        for (Atom atom : groundedExamples) {
            addVariableSubstitutions(atom, variableMap, exampleVariables);
        }
        for (Atom atom : relevants) {
            conjunction.add(new Literal(LanguageUtils.toVariableAtom(atom, variableMap, variableGenerator)));
        }

        return new HornClause(new Atom(predicate, exampleVariables), conjunction);
    }

    /**
     * Gets the substitution to variable from the terms of the examples.
     *
     * @param arity             the arity of the example
     * @param variableGenerator the variable generator
     * @return the list of substitution variables by term
     */
    protected static List<Term> getExampleVariables(int arity, VariableGenerator variableGenerator) {
        List<Term> exampleVariables = new ArrayList<>(arity);
        for (int i = 0; i < arity; i++) {
            exampleVariables.add(variableGenerator.next());
        }
        return exampleVariables;
    }

    /**
     * Adds the substitution of the i-th term of the atom as the i-th term from the variable list, if it is a constant.
     *
     * @param atom        the atom
     * @param variableMap the variable map
     * @param variables   the variables
     */
    protected static void addVariableSubstitutions(Atom atom, Map<Term, Term> variableMap, List<Term> variables) {
        for (int i = 0; i < atom.getTerms().size(); i++) {
            if (atom.getTerms().get(i).isConstant() && !variableMap.containsKey(atom.getTerms().get(i))) {
                variableMap.put(atom.getTerms().get(i), variables.get(i));
            }
        }
    }

}
