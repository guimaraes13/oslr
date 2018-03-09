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
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.Literal;
import br.ufrj.cos.util.ExceptionMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static br.ufrj.cos.util.log.PreRevisionLog.TRY_REFINE_RULE;
import static br.ufrj.cos.util.log.RevisionLog.PROPOSED_REMOVE_LITERAL;
import static br.ufrj.cos.util.log.RevisionLog.PROPOSED_REMOVE_RULE;

/**
 * Revision operator that removes a node from the {@link TreeTheory}.
 * <p>
 * Created on 20/06/17.
 *
 * @author Victor Guimarães
 */
public class RemoveNodeTreeRevisionOperator extends TreeRevisionOperator {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    @Override
    public Theory performOperation(Collection<? extends Example> targets) throws TheoryRevisionException {
        try {
            Node<HornClause> revisionLeaf = treeTheory.getRevisionLeaf();
            logger.trace(TRY_REFINE_RULE.toString(), revisionLeaf);
            if (revisionLeaf.isDefaultChild()) { return null; }
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
                return removeLiteralFromTheory(revisionLeaf, targets);
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
        final HornClause element = node.getElement();
        while (iterator.hasNext()) {
            clause = iterator.next();
            if (element.equals(clause)) {
                iterator.remove();
                break;
            }
        }
        logger.debug(PROPOSED_REMOVE_RULE.toString(), element);
        return theory;
    }

    /**
     * Removes the literal from the rule, represented by the node, reducing it to its parent.
     *
     * @param node the node
     * @param examples the examples
     * @return the modified theory
     */
    protected Theory removeLiteralFromTheory(Node<HornClause> node, Collection<? extends Example> examples) {
        Iterator<HornClause> iterator = learningSystem.getTheory().iterator();
        Collection<HornClause> clauses = new LinkedHashSet<>();
        HornClause clause;
        while (iterator.hasNext()) {
            clause = iterator.next();
            if (node.getElement().equals(clause)) {
                // this is the revision point, add the revised clause
                HornClause revisedClause = featureGenerator.createFeatureForRule(node.getParent().getElement(),
                                                                                 examples);
                clauses.add(revisedClause);
            } else {
                // this is not the revision point, simple add to the collection
                clauses.add(clause);
            }
        }
        if (logger.isDebugEnabled()) {
            Set<Literal> body = new HashSet<>(node.getElement().getBody());
            body.removeAll(node.getParent().getElement().getBody());
            logger.debug(PROPOSED_REMOVE_LITERAL.toString(), body.toArray());
        }
        return new Theory(clauses, learningSystem.getTheory().getAcceptPredicate());
    }

    @Override
    public void theoryRevisionAccepted(Theory revised) {
        Node<HornClause> revisionLeaf = treeTheory.getRevisionLeaf();
        treeTheory.removeExampleFromLeaf(revisionLeaf.getElement().getHead().getPredicate().toString(), revisionLeaf);
        if (revisionLeaf.isRoot()) {
            revisionLeaf.getElement().getBody().clear();
            revisionLeaf.getElement().getBody().add(Literal.FALSE_LITERAL);
        } else {
            TreeTheory.removeNodeFromTree(revisionLeaf);
        }
    }

}
