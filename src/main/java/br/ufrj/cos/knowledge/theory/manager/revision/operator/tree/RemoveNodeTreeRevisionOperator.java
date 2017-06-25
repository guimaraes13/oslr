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
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.Literal;
import br.ufrj.cos.util.ExceptionMessages;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Revision operator that removes a node from the {@link TreeTheory}.
 * <p>
 * Created on 20/06/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings({"unused"})
public class RemoveNodeTreeRevisionOperator extends TreeRevisionOperator {

    @Override
    public Theory performOperation(Iterable<? extends Example> targets) throws TheoryRevisionException {
        try {
            Node<HornClause> revisionLeaf = treeTheory.revisionLeaf;
            if (revisionLeaf.isRoot()) {
                // this is the root node
                if (TreeTheory.isDefaultTheory(revisionLeaf)) {
                    // default theory, nothing to delete
                    return null;
                } else {
                    // true theory, make it false (default)
                    return removeRuleFromTheory(revisionLeaf);
                }
            } else if (revisionLeaf.getParent().getChildren().size() > 1) {
                // this node represents bifurcation of a rule, it is a rule deletion operation
                return removeRuleFromTheory(revisionLeaf);
            } else {
                // this node represents straight rule, it is a literal deletion operation
                return removeLiteralFromTheory(revisionLeaf);
            }
        } catch (KnowledgeException e) {
            throw new TheoryRevisionException(ExceptionMessages.ERROR_DURING_THEORY_COPY.toString(), e);
        }
    }

    /**
     * Removes the rule, represented by the node, from the theory.
     *
     * @param node the node
     * @return the modified theory
     * @throws KnowledgeException in case of error during the copy of the theory
     */
    protected Theory removeRuleFromTheory(Node<HornClause> node) throws KnowledgeException {
        Theory theory = learningSystem.getTheory().copy();
        Iterator<HornClause> iterator = theory.iterator();
        HornClause clause;
        while (iterator.hasNext()) {
            clause = iterator.next();
            if (node.getElement().equals(clause)) {
                iterator.remove();
                break;
            }
        }
        return theory;
    }

    /**
     * Removes the literal from the rule, represented by the node, reducing it to its parent.
     *
     * @param node the node
     * @return the modified theory
     */
    protected Theory removeLiteralFromTheory(Node<HornClause> node) {
        Iterator<HornClause> iterator = learningSystem.getTheory().iterator();
        Collection<HornClause> clauses = new LinkedHashSet<>();
        HornClause clause;
        while (iterator.hasNext()) {
            clause = iterator.next();
            if (node.getElement().equals(clause)) {
                // this is the revision point, add the revised clause
                clauses.add(node.getParent().getElement());
            } else {
                // this is not the revision point, simple add to the collection
                clauses.add(clause);
            }
        }
        return new Theory(clauses, learningSystem.getTheory().getAcceptPredicate());
    }

    @Override
    public void theoryRevisionAccepted(Theory revised) {
        Node<HornClause> revisionLeaf = treeTheory.revisionLeaf;
        if (revisionLeaf.isRoot()) {
            revisionLeaf.getElement().getBody().clear();
            revisionLeaf.getElement().getBody().add(Literal.FALSE_LITERAL);
        } else {
            TreeTheory.removeNodeFromTree(revisionLeaf);
        }
    }

}
