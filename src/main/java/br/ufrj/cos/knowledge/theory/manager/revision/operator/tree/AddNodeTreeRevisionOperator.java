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

package br.ufrj.cos.knowledge.theory.manager.revision.operator.tree;

import br.ufrj.cos.knowledge.KnowledgeException;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.manager.Node;
import br.ufrj.cos.knowledge.manager.TreeTheory;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.AsyncTheoryEvaluator;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.LiteralAppendOperator;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Conjunction;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.Literal;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static br.ufrj.cos.util.log.PreRevisionLog.RULE_PROPOSED_TO_THEORY;
import static br.ufrj.cos.util.log.PreRevisionLog.TRY_REFINE_RULE;
import static br.ufrj.cos.util.log.RevisionLog.PROPOSED_REFINED_RULE;
import static br.ufrj.cos.util.log.RevisionLog.REFINING_RULE;

/**
 * Revision operator that adds a new node on the {@link TreeTheory}.
 * <p>
 * Created on 20/06/17.
 *
 * @author Victor Guimarães
 */
public class AddNodeTreeRevisionOperator extends TreeRevisionOperator {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * Represents a constant for no maximum side way movements.
     */
    public static final int NO_MAXIMUM_SIDE_WAY_MOVEMENTS = -1;

    /**
     * The default value for {@link #improvementThreshold}.
     */
    public static final double DEFAULT_IMPROVEMENT_THRESHOLD = 0.0;
    /**
     * Flag to specify if the rule must be refined or not.
     */
    @SuppressWarnings("CanBeFinal")
    public boolean refine = false;
    /**
     * Represents the maximum side way movements, i.e. the number of {@link Literal} that will be added to the body
     * of the {@link HornClause} without improving the metric.
     * <p>
     * If the metric improves by adding a {@link Literal} to the body, it not counts as a side way movements.
     * <p>
     * If it is {@link #NO_MAXIMUM_SIDE_WAY_MOVEMENTS}, it means there is no maximum side way
     * movements, it will be limited by the size of the bottom clause.
     */
    @SuppressWarnings("CanBeFinal")
    public int maximumSideWayMovements = NO_MAXIMUM_SIDE_WAY_MOVEMENTS;
    /**
     * The minimal necessary difference, between and current {@link HornClause} evaluation and a new candidate one,
     * to be considered as improvement. If the threshold is not met, it is considered a side way movement.
     * <p>
     * Use a threshold of 0 and {@link #maximumSideWayMovements} of {@link #NO_MAXIMUM_SIDE_WAY_MOVEMENTS} to allow the
     * search to test all possible {@link HornClause}s.
     * <p>
     * Use a threshold of {@code e} and {@link #maximumSideWayMovements} of 0 to stop as soon as a {@link HornClause}
     * does
     * not improves more than {@code e}.
     */
    @SuppressWarnings("CanBeFinal")
    public double improvementThreshold = DEFAULT_IMPROVEMENT_THRESHOLD;
    /**
     * Flag to specify which {@link HornClause} will be returned in case of a tie in the evaluation metric.
     * <p>
     * If it is {@code true}, the most generic one will be returned (i.e. the smallest).
     * <p>
     * If it is {@code false}, the most specific one will be returned (i.e. the largest).
     */
    @SuppressWarnings("CanBeFinal")
    public boolean generic = true;
    protected HornClause revisedClause;
    protected LiteralAppendOperator appendOperator;

    /**
     * Gets the last literal of the body of each child of the node. This allows that the creation of another rule
     * from this node to avoid creating already existing rules.
     *
     * @param node the node
     * @return the redundant literals
     */
    protected static Collection<? extends Literal> buildRedundantLiterals(Node<HornClause> node) {
        Collection<Literal> redundantLiterals = new HashSet<>();
        for (Node<HornClause> sibling : node.getChildren()) {
            redundantLiterals.add(sibling.getElement().getBody().getLastElement());
        }
        return redundantLiterals;
    }

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        if (this.appendOperator == null) {
            throw new InitializationException(
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                 LiteralAppendOperator.class.getSimpleName()));
        }
        this.appendOperator.setLearningSystem(learningSystem);
        appendOperator.initialize();
    }

    @Override
    public Theory performOperation(Collection<? extends Example> targets) throws TheoryRevisionException {
        try {
            Node<HornClause> revisionLeaf = treeTheory.getRevisionLeaf();
            logger.trace(TRY_REFINE_RULE.toString(), revisionLeaf);
            if (revisionLeaf.isRoot()) {
                // this is the root node
                return addRuleToTheory(revisionLeaf, targets);
            } else if (revisionLeaf.isDefaultChild()) {
                // this node represents a false leaf, it is a rule creation in the parent
                return addRuleToTheory(revisionLeaf.getParent(), targets);
            } else {
                // this node represents a rule, it is a literal addition operation
                return addLiteralToTheory(revisionLeaf, targets);
            }
        } catch (KnowledgeException e) {
            throw new TheoryRevisionException(ExceptionMessages.ERROR_REVISING_THE_THEORY.toString(), e);
        }
    }

    @Override
    public void theoryRevisionAccepted(Theory revised) {
        Node<HornClause> revisionLeaf = treeTheory.getRevisionLeaf();
        if (revisionLeaf.isDefaultChild()) { revisionLeaf = revisionLeaf.getParent(); }
        Conjunction initialBody;
        if (revisionLeaf.isRoot() && TreeTheory.isDefaultTheory(revisionLeaf)) {
            // turns into true theory
            revisionLeaf.getElement().getBody().clear();
            revisionLeaf.getElement().getBody().add(Literal.TRUE_LITERAL);
            initialBody = new Conjunction();
        } else {
            initialBody = revisionLeaf.getElement().getBody();
        }
        addNodesToTree(revisionLeaf, initialBody);
    }

    /**
     * Adds the nodes from the modified clause to the tree.
     *
     * @param revisionLeaf the revised leaf
     * @param initialBody  the initial body
     */
    protected void addNodesToTree(Node<HornClause> revisionLeaf, Conjunction initialBody) {
        Atom head = revisionLeaf.getElement().getHead();
        Conjunction currentBody = initialBody;
        Conjunction nextBody;
        Node<HornClause> node = revisionLeaf;
        for (Literal literal : revisedClause.getBody()) {
            if (currentBody.contains(literal)) { continue; }
            nextBody = new Conjunction(currentBody.size() + 1);
            nextBody.addAll(currentBody);
            nextBody.add(literal);
            node = TreeTheory.addNodeToTree(node, new HornClause(head, nextBody));
            currentBody = nextBody;
        }
    }

    /**
     * Adds a rule, represented by the node with the addition of a literal, to the theory. It is, if it is possible
     * to create a new rule, by adding a literal to the current node (different from the node's children), that
     * improves the theory, given the examples.
     *
     * @param node     the node
     * @param examples the examples
     * @return the modified theory
     * @throws KnowledgeException in case of error during the operation
     */
    protected Theory addRuleToTheory(Node<HornClause> node,
                                     Collection<? extends Example> examples) throws KnowledgeException {
        return createSortedTheory(node, examples, false);
    }

    /**
     * Adds a literal to the rule, represented by the node. It is, if it is possible to add a new literal to the
     * rule, that makes it better, given the examples, create a new rule with this literal and replace the current
     * rule for the new one.
     *
     * @param node     the node
     * @param examples the examples
     * @return the modified theory
     * @throws KnowledgeException in case of error during the operation
     */
    protected Theory addLiteralToTheory(Node<HornClause> node,
                                        Collection<? extends Example> examples) throws KnowledgeException {
        return createSortedTheory(node, examples, true);
    }

    /**
     * Creates a sorted theory adding a new rule from the creation of a append from the examples, by using the
     * {@link LiteralAppendOperator}.
     *
     * @param node      the node
     * @param examples  the examples
     * @param removeOld if is to remove the initial rule, defined by the node, from the theory
     * @return the new sorted theory
     * @throws KnowledgeException in case of error during the operation
     */
    protected Theory createSortedTheory(Node<HornClause> node, Collection<? extends Example> examples,
                                        boolean removeOld) throws KnowledgeException {
        HornClause element = node.isRoot() ? new HornClause(node.getElement().getHead(), new Conjunction()) :
                node.getElement();
        AsyncTheoryEvaluator hornClause = appendOperator.buildExtendedHornClause(examples, element,
                                                                                 buildRedundantLiterals(node));
        if (hornClause == null) { return null; }
        if (refine) {
            hornClause = refineClause(hornClause, examples);
        }
        revisedClause = hornClause.getHornClause();
        logger.debug(RULE_PROPOSED_TO_THEORY.toString(), revisedClause);
        Theory theory = learningSystem.getTheory().copy();
        theory.add(revisedClause);
        if (removeOld) { theory.remove(node.getElement()); }

        List<HornClause> clauses = new ArrayList<>(theory);
        clauses.sort(Comparator.comparing(LanguageUtils::formatHornClause));

        return new Theory(clauses, learningSystem.getTheory().getAcceptPredicate());
    }

    /**
     * Refines the clause. It starts from the initialClause and adds a {@link Literal} at time into its body.
     * At each time, getting the best possible {@link HornClause}. It finishes when one of the following criteria is
     * met:
     * <p>
     * 1) The addition of another {@link Literal} does not improves the {@link HornClause} in
     * {@link #maximumSideWayMovements} times;
     * <br>
     * 2) There is no more possible addition to make;
     * <p>
     * After it finishes, it return the best {@link HornClause} found, based on the {@link #generic} criteria.
     *
     * @param initialClause the initial candidate clause
     * @param examples      the examples
     * @return a {@link AsyncTheoryEvaluator} containing the best {@link HornClause} found
     * @throws TheoryRevisionException in case of error during the append of new literals
     */
    protected AsyncTheoryEvaluator refineClause(AsyncTheoryEvaluator initialClause,
                                                Collection<? extends Example> examples) throws TheoryRevisionException {
        logger.debug(REFINING_RULE.toString(), initialClause);
        int sideWayMovements = 0;
        AsyncTheoryEvaluator best = initialClause;
        AsyncTheoryEvaluator current = initialClause;
        while (!isToStopBySideWayMovements(sideWayMovements)) {
            current = appendOperator.buildExtendedHornClause(examples, current.getHornClause(), null);
            if (current == null) {
                break;
            }
            logger.trace(PROPOSED_REFINED_RULE.toString(), current);
            if (theoryMetric.difference(current.getEvaluation(), best.getEvaluation()) > improvementThreshold) {
                best = current;
                sideWayMovements = 0;
            } else {
                sideWayMovements++;
                if (theoryMetric.difference(current.getEvaluation(), best.getEvaluation()) >= 0.0 && !generic) {
                    best = current;
                }
            }
        }
        return best;
    }

    /**
     * Checks if is to stop due to much iterations without improvements.
     *
     * @param sideWayMovements the number of iterations without improvements
     * @return {@code true} if it is to stop, {@code false} if it is to continue
     */
    protected boolean isToStopBySideWayMovements(int sideWayMovements) {
        return maximumSideWayMovements > NO_MAXIMUM_SIDE_WAY_MOVEMENTS && sideWayMovements > maximumSideWayMovements;
    }

    /**
     * Gets the {@link LiteralAppendOperator}.
     *
     * @return the {@link LiteralAppendOperator}
     */
    public LiteralAppendOperator getAppendOperator() {
        return appendOperator;
    }

    /**
     * Sets the {@link LiteralAppendOperator}.
     *
     * @param appendOperator the {@link LiteralAppendOperator}
     * @throws InitializationException if the {@link LiteralAppendOperator} is already set
     */
    public void setAppendOperator(LiteralAppendOperator appendOperator) throws InitializationException {
        if (this.appendOperator != null) {
            throw new InitializationException(
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                 LiteralAppendOperator.class.getSimpleName()));
        }
        this.appendOperator = appendOperator;
    }

}
