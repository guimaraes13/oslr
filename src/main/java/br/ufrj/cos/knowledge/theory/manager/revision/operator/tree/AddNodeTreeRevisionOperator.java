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
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.LiteralAppendOperator;
import br.ufrj.cos.logic.Conjunction;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.Literal;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;

import java.util.*;

/**
 * Revision operator that adds a new node on the {@link TreeTheory}.
 * <p>
 * Created on 20/06/17.
 *
 * @author Victor Guimarães
 */
public class AddNodeTreeRevisionOperator extends TreeRevisionOperator {

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
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                   LiteralAppendOperator.class.getSimpleName()));
        }
        this.appendOperator.setLearningSystem(learningSystem);
        appendOperator.initialize();
    }

    @Override
    public Theory performOperation(Collection<? extends Example> targets) throws TheoryRevisionException {
        try {
            Node<HornClause> revisionLeaf = treeTheory.getRevisionLeaf();
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
        if (revisionLeaf.isRoot() && TreeTheory.isDefaultTheory(revisionLeaf)) {
            // turns into true theory
            revisionLeaf.getElement().getBody().clear();
            revisionLeaf.getElement().getBody().add(Literal.TRUE_LITERAL);
        }
        TreeTheory.addNodeToTree(revisionLeaf, revisedClause);
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
        HornClause hornClause = appendOperator.buildExtendedHornClause(examples, element,
                                                                       buildRedundantLiterals(node));
        if (hornClause == null) { return null; }
        revisedClause = hornClause;
        Theory theory = learningSystem.getTheory().copy();
        if (removeOld) { theory.remove(node.getElement()); }
        theory.add(hornClause);

        List<HornClause> clauses = new ArrayList<>(theory);
        clauses.sort(Comparator.comparing(LanguageUtils::formatHornClause));

        return new Theory(clauses, learningSystem.getTheory().getAcceptPredicate());
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
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                   LiteralAppendOperator.class.getSimpleName()));
        }
        this.appendOperator = appendOperator;
    }

}
