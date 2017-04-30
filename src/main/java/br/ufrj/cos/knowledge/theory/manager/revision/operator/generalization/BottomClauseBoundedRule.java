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

import br.ufrj.cos.engine.EngineSystemTranslator;
import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.ExampleSet;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.AsynchronousTheoryEvaluator;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryMetric;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.logic.*;
import br.ufrj.cos.util.HornClauseUtils;
import br.ufrj.cos.util.LogMessages;
import br.ufrj.cos.util.TimeMeasure;
import br.ufrj.cos.util.VariableGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Operator that implements Guimarães and Paes rule creation algorithm.
 * <p>
 * V. Guimarães and A. Paes,
 * Looking at the Bottom and the Top: A Hybrid Logical Relational Learning System Based on Answer Sets,
 * 2015 Brazilian Conference on Intelligent Systems (BRACIS), Natal, 2015, pp. 240-245.
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 * @see <a href="http://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=7424026&isnumber=7423894">Looking at the
 * Bottom and the Top: A Hybrid Logical Relational Learning System Based on Answer Sets</a>
 */
public class BottomClauseBoundedRule extends GeneralizationRevisionOperator {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * Represents a constant for no maximum depth on the transitivity of the relevant concept
     */
    public static final int NO_MAXIMUM_DEPTH = -1;

    /**
     * Represents a constant for no maximum side way movements
     */
    public static final int NO_MAXIMUM_SIDE_WAY_MOVEMENTS = -1;

    /**
     * The class to use as the variable generator
     */
    public Class<? extends VariableGenerator> VARIABLE_GENERATOR_CLASS = VariableGenerator.class;

    /**
     * Represents the maximum depth on the transitivity of the relevant concept. A {@link Atom} is relevant to the
     * example if
     * it shares (or transitively) a {@link Term} with it.
     * <p>
     * If it is 0, it means that only the {@link Atom}s which actually share a {@link Term} if the
     * example will be considered.
     * <p>
     * If it is 1, it means that only the {@link Atom}s which actually share a {@link Term} if the
     * example will be considered, and the {@link Atom}s which share a {@link Term}s if those ones.
     * <p>
     * And so on. If it is {@link #NO_MAXIMUM_DEPTH}, it means that there is no limit on the
     * transitivity.
     */
    public int relevantsDepth = 0;

    /**
     * Represents the maximum side way movements, i.e. the number of {@link Literal} that will be added to the body
     * of the {@link HornClause} without improving the metric.
     * <p>
     * If the metric improves by adding a {@link Literal} to the body, it not counts as a side way movements.
     * <p>
     * If it is {@link #NO_MAXIMUM_SIDE_WAY_MOVEMENTS}, it means there is no maximum side way
     * movements, it will be limited by the size of the bottom clause.
     */
    public int sideWayMovements = NO_MAXIMUM_SIDE_WAY_MOVEMENTS;

    /**
     * Flag to specify which {@link HornClause} will be returned in case of a tie in the evaluation metric.
     * <p>
     * If it is {@code true}, the most generic one will be returned (i.e. the smallest).
     * <p>
     * If it is {@code false}, the most specific one will be returned (i.e. the largest).
     */
    public boolean generic = true;

    /**
     * The maximum amount of time, in seconds, allowed to the evaluation of the {@link Theory}.
     * <p>
     * Default is 300 seconds (i.e. 5 minutes).
     */
    public int evaluationTimeout = 300;

    @SuppressWarnings("CanBeFinal")
    protected EngineSystemTranslator engineSystem;

    protected TheoryMetric theoryMetric;

    /**
     * Constructs the class if the minimum required parameters. The other fields can be setted by direct access
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     * @param theory        the {@link Theory}
     * @param examples      the {@link ExampleSet}
     * @param engineSystem  the {@link EngineSystemTranslator}
     * @param theoryMetric  the {@link TheoryMetric}
     */
    public BottomClauseBoundedRule(KnowledgeBase knowledgeBase, Theory theory, ExampleSet examples,
                                   EngineSystemTranslator engineSystem,
                                   TheoryMetric theoryMetric) {
        super(knowledgeBase, theory, examples);
        this.engineSystem = engineSystem;
        this.theoryMetric = theoryMetric;
    }

    @Override
    public Theory performOperation(Example... targets) throws TheoryRevisionException {
        for (Example example : targets) {
            buildRuleForExample(example);
            //TODO: put rule on the theory
        }
        return theory;
    }

    /**
     * Builds a {@link HornClause} from a target example.
     *
     * @param example the target example
     * @return a {@link HornClause}
     */
    protected HornClause buildRuleForExample(Example example) {
        try {
            logger.trace(LogMessages.BUILDING_THE_BOTTOM_CLAUSE.toString(), example);
            HornClause bottomClause = buildBottomClause(example);

            Set<HornClause> minimalClauses = HornClauseUtils.buildMinimalSafeRule(bottomClause);
            logger.trace(LogMessages.FIND_MINIMAL_SAFE_CLAUSES);

            Theory revisedTheory = theory.copy();
            revisedTheory.add(bottomClause);
            double initialMeasure = evaluateTheory(revisedTheory);
            //QUESTION: perform all evaluations simultaneously?
            //TODO: evaluate the top collection
            //TODO: get the best clause
            //TODO: greedy add the remaining literal from the bottom clause to the best top clause
            //TODO: evaluate each iteration saving the current and the best
            //TODO: if generic, keeps the first best, keeps the latest otherwise
            //TODO: continues until maximum side way movements
            //TODO: return best rule
        } catch (Exception e) {
            logger.error(LogMessages.ERROR_REVISING_THEORY, e);
        }
        return null;
    }

    /**
     * Builds the bottom clause based on the target {@link Example}
     *
     * @param target the target {@link Example}
     * @return the bottom clause
     * @throws IllegalAccessException if an error occurs when instantiating a new object by reflection
     * @throws InstantiationException if an error occurs when instantiating a new object by reflection
     */
    protected HornClause buildBottomClause(Example target) throws InstantiationException, IllegalAccessException {
        Set<Atom> relevants = relevantsBreadthFirstSearch(target.getPositiveTerms());
        relevants.addAll(engineSystem.groundingExamples(target));
        Map<Term, Variable> variableMap = target.getVariableMap();

        return toVariableHornClauseForm(target, relevants, variableMap);
    }

    /**
     * Evaluates the given {@link Theory} in another {@link Thread} with a limit of time defined by the
     * {@link #evaluationTimeout}. If the evaluation success, the correspondent evaluation value is returned. If it
     * fails, is returned the default value of the metric, instead.
     *
     * @param theory the {@link Theory}
     * @return the evaluation value
     */
    public double evaluateTheory(Theory theory) {
        AsynchronousTheoryEvaluator evaluator = new AsynchronousTheoryEvaluator(knowledgeBase, theory, examples,
                                                                                theoryMetric);
        try {
            evaluator.start();
            evaluator.join(evaluationTimeout * TimeMeasure.SECONDS_TO_MILLISECONDS_MULTIPLIER);
            if (evaluator.isAlive()) {
                logger.trace(LogMessages.EVALUATION_THEORY_REACHED_TIMEOUT.toString(), evaluationTimeout);
            } else {
                evaluator.interrupt();
            }

            return evaluator.getEvaluation();
        } catch (InterruptedException e) {
            logger.error(LogMessages.ERROR_EVALUATING_CANDIDATE_THEORY, e);
        }

        return theoryMetric.getDefaultValue();
    }

    /**
     * Gets the relevant {@link Atom}s, given the relevant seed {@link Term}s, by performing a breadth-first search
     * on the {@link KnowledgeBase}'s base cached graph
     *
     * @param terms the seed {@link Term}s
     * @return the relevant {@link Atom}s to the seed {@link Term}s
     */
    public Set<Atom> relevantsBreadthFirstSearch(Iterable<? extends Term> terms) {
        Map<Term, Integer> termDistance = new HashMap<>();
        Queue<Term> queue = new ArrayDeque<>();
        Set<Atom> atoms = new HashSet<>();

        for (Term term : terms) {
            termDistance.put(term, 0);
            queue.add(term);
        }

        Term current;
        Integer distance;
        while (!queue.isEmpty()) {
            current = queue.poll();
            distance = termDistance.get(current);

            atoms.addAll(knowledgeBase.getAtomsWithTerm(current));

            if (relevantsDepth == NO_MAXIMUM_DEPTH || distance < relevantsDepth) {
                for (Term neighbour : knowledgeBase.getTermNeighbours(current)) {
                    termDistance.put(neighbour, distance + 1);
                    queue.add(neighbour);
                }
            }
        }

        return atoms;
    }

    /**
     * Creates a variable version of the {@link HornClause} with the variable target {@link Example} in the head
     *
     * @param target      the target {@link Example}
     * @param body        the body of the rule
     * @param variableMap the variableMap, useful when wants to collapse more than on {@link Term} into the same
     *                    {@link Variable}
     * @return the variable {@link HornClause}
     * @throws IllegalAccessException if an error occurs when instantiating a new list of {@link Term}s
     * @throws InstantiationException if an error occurs when instantiating a new list of {@link Term}s
     */
    protected HornClause toVariableHornClauseForm(Example target, Collection<? extends Atom> body,
                                                  Map<Term, Variable> variableMap)
            throws IllegalAccessException, InstantiationException {
        VariableGenerator variableGenerator = VARIABLE_GENERATOR_CLASS.newInstance();
        //Maps the Term::getName to each Variable in the values of variableMap and makes a Set of it
        variableGenerator.setUsedNames(variableMap.values().stream().map(Term::getName).collect(Collectors.toSet()));
        Conjunction conjunction = new Conjunction(body.size());
        for (Atom atom : body) {
            conjunction.add(new Literal(toVariableAtom(atom, variableMap, variableGenerator)));
        }

        return new HornClause(toVariableAtom(target.getAtom(), variableMap, variableGenerator), conjunction);
    }

    /**
     * Turn into variable the {@link Term}s of the given {@link Atom}
     *
     * @param atom        the {@link Atom}
     * @param variableMap the {@link Map} of {@link Term}s to {@link Variable}s
     * @param generator   the {@link VariableGenerator}
     * @return the new {@link Atom}
     * @throws IllegalAccessException if an error occurs when instantiating a new list of {@link Term}s
     * @throws InstantiationException if an error occurs when instantiating a new list of {@link Term}s
     */
    protected Atom toVariableAtom(Atom atom, Map<Term, Variable> variableMap, VariableGenerator generator) throws
            IllegalAccessException, InstantiationException {
        List<Term> terms = atom.getTerms().getClass().newInstance();
        for (Term term : atom.getTerms()) {
            terms.add(variableMap.computeIfAbsent(term, k -> generator.next()));
        }

        return new Atom(atom.getName(), terms);
    }

}
