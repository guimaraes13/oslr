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

package br.ufrj.cos.knowledge.theory.manager.revision.operator.tree;

import br.ufrj.cos.knowledge.KnowledgeException;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.manager.Node;
import br.ufrj.cos.knowledge.manager.TreeTheory;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.knowledge.theory.manager.revision.point.RevisionExamples;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.Literal;
import br.ufrj.cos.util.ExceptionMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

import static br.ufrj.cos.util.log.PreRevisionLog.TRY_REFINE_RULE;

/**
 * Revision operator that removes a node from the {@link TreeTheory}.
 * <p>
 * Created on 05/01/18.
 *
 * @author Victor Guimarães
 */
public class RemoveNodeTreeRevisionOperator2 extends RemoveNodeTreeRevisionOperator {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    @Override
    public Theory performOperation(Collection<? extends Example> targets) throws TheoryRevisionException {
        try {
            Node<HornClause> revisionLeaf = treeTheory.getRevisionLeaf();
            logger.trace(TRY_REFINE_RULE.toString(), revisionLeaf);
            if (revisionLeaf.isRoot()) {
                // Root Case
                // this is the root node
                if (TreeTheory.isDefaultTheory(revisionLeaf)) {
                    // default theory, nothing to delete
                    return null;
                } else {
                    // true theory, make it false (default)
                    return removeRuleFromTheory(revisionLeaf);
                }
            } else if (revisionLeaf.isDefaultChild()) {
                if (revisionLeaf.getParent().getChildren().size() == 1) {
                    // Remove Literal Case
                    Node<HornClause> revisionNode = revisionLeaf.getParent().getChildren().iterator().next();
                    if (revisionNode.isProofLeaf()) {
                        // this node represents the last default of a straight rule, it is a literal deletion operation
                        return removeLiteralFromTheory(revisionNode, targets);
                    }
                }
            } else if (revisionLeaf.getParent().getChildren().size() > 1) {
                // Remove Rule Case
                // this node represents bifurcation of a rule, it is a rule deletion operation
                return removeRuleFromTheory(revisionLeaf);
            }
            return null;
        } catch (KnowledgeException e) {
            throw new TheoryRevisionException(ExceptionMessages.ERROR_DURING_THEORY_COPY.toString(), e);
        }
    }

    @Override
    public void theoryRevisionAccepted(Theory revised) {
        Node<HornClause> revisionLeaf = treeTheory.getRevisionLeaf();
        final String predicate = revisionLeaf.getElement().getHead().getPredicate().toString();
        treeTheory.removeExampleFromLeaf(predicate, revisionLeaf);
        if (revisionLeaf.isRoot()) {
            // Root case
            revisionLeaf.getElement().getBody().clear();
            revisionLeaf.getElement().getBody().add(Literal.FALSE_LITERAL);
        } else if (revisionLeaf.isDefaultChild() && revisionLeaf.getParent().getChildren().size() == 1) {
            // Remove Literal Case
            Node<HornClause> revisionNode = revisionLeaf.getParent().getChildren().iterator().next();
            removeLiteralFromTree(revisionNode);
        } else {
            // Remove Rule Case
            TreeTheory.removeNodeFromTree(revisionLeaf);
            treeTheory.removeExampleFromLeaf(predicate, revisionLeaf);
        }
    }

    /**
     * Removes the literal from the tree and passes it examples to its parent.
     *
     * @param revisionNode the literal node to remove
     */
    protected void removeLiteralFromTree(Node<HornClause> revisionNode) {
        final String predicate = revisionNode.getElement().getHead().getPredicate().toString();
        if (revisionNode.isProofLeaf()) {
            // gets the examples from the last literal, which has been deleted
            final RevisionExamples exampleFromLeaf = treeTheory.getExampleFromLeaf(predicate, revisionNode);
            // unbind the examples from the leaf that will be removed from the tree
            treeTheory.removeExampleFromLeaf(predicate, revisionNode);
            // removes the leaf from the tree
            TreeTheory.removeNodeFromTree(revisionNode);
            // links the example to the removed leaf's parent
            treeTheory.getLeafExampleMapFromTree(predicate).put(revisionNode.getParent(), exampleFromLeaf);
        }
    }

}
