/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2018 Victor Guimarães
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
import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.ProPprExample;
import br.ufrj.cos.knowledge.manager.Node;
import br.ufrj.cos.knowledge.manager.TreeTheory;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.knowledge.theory.manager.revision.point.RevisionExamples;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.Literal;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.InitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static br.ufrj.cos.util.LanguageUtils.groupAtomExamplesByGoal;
import static br.ufrj.cos.util.log.PreRevisionLog.TRY_REFINE_RULE;

/**
 * Revision operator that removes a node from the {@link TreeTheory}.
 * <p>
 * Created on 05/01/18.
 *
 * @author Victor Guimarães
 */
public class RemoveNodeTreeRevisionOperator3 extends RemoveNodeTreeRevisionOperator2 {

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
            } else if (!revisionLeaf.getParent().getChildren().isEmpty()) {
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
        } else if (revisionLeaf.isDefaultChild()) {
            // Remove Literal Case
            if (revisionLeaf.getParent().getChildren().size() == 1) {
                Node<HornClause> revisionNode = revisionLeaf.getParent().getChildren().iterator().next();
                removeLiteralFromTree(revisionNode);
            }
        } else if (!revisionLeaf.getParent().getChildren().isEmpty()) {
            // Remove Rule Case
            if (revisionLeaf.getParent().getChildren().size() == 1) {
                try {
                    removeRuleFromTree(revisionLeaf);
                } catch (InitializationException e) {
                    logger.error(ExceptionMessages.ERROR_REVISING_THE_THEORY_REASON, e);
                }
            } else {
                TreeTheory.removeNodeFromTree(revisionLeaf);
                treeTheory.removeExampleFromLeaf(predicate, revisionLeaf);
            }
        }
    }

    /**
     * Removes the rule from the tree.
     *
     * @param revisionLeaf the leaf node of the rule
     * @throws InitializationException in case of error during the copy of the unproved examples
     */
    protected void removeRuleFromTree(final Node<HornClause> revisionLeaf) throws InitializationException {
        Map<Atom, Set<AtomExample>> groupedExamples = new HashMap<>();
        final String predicate = revisionLeaf.getElement().getHead().getPredicate().toString();
        RevisionExamples unprovedExamples;
        Node<HornClause> currentLeaf = revisionLeaf;
        Node<HornClause> previousLeaf;
        do {
            previousLeaf = currentLeaf;
            currentLeaf = currentLeaf.getParent();
            // gets the unproved examples from the default node of the parent
            unprovedExamples = treeTheory.getExampleFromLeaf(predicate, currentLeaf.getDefaultChild());
            groupAtomExamplesByGoal(unprovedExamples.getTrainingExamples(true),
                                    groupedExamples);
            // unbind the examples from the leaf that will be removed from the tree
            treeTheory.removeExampleFromLeaf(predicate, currentLeaf.getDefaultChild());
        } while (currentLeaf.getChildren().size() == 1 && currentLeaf.getParent() != null);

        treeTheory.removeExampleFromLeaf(predicate, revisionLeaf);
        if (currentLeaf.isRoot() && currentLeaf.getChildren().size() == 1) {
            // Root case
            currentLeaf.getElement().getBody().clear();
            currentLeaf.getElement().getBody().add(Literal.FALSE_LITERAL);
        }

        RevisionExamples revisionExamples = mergeRevisionExamples(groupedExamples, unprovedExamples);
        treeTheory.getLeafExampleMapFromTree(predicate).put(currentLeaf, revisionExamples);
        TreeTheory.removeNodeFromTree(previousLeaf);
    }

    /**
     * Merges the examples from the default children in the removed rule's path.
     *
     * @param groupedExamples  the examples grouped by the goal queries
     * @param unprovedExamples the revision examples from the last default child
     * @return the merged revision examples.
     * @throws InitializationException in case of error during the copy of the unproved examples
     */
    protected RevisionExamples mergeRevisionExamples(Map<Atom, Set<AtomExample>> groupedExamples,
                                                     RevisionExamples unprovedExamples) throws InitializationException {
        RevisionExamples revisionExamples = new RevisionExamples(learningSystem, unprovedExamples
                .getSampleSelector().copy());
        for (Map.Entry<Atom, Set<AtomExample>> entry : groupedExamples.entrySet()) {
            revisionExamples.addExample(new ProPprExample(entry.getKey(), new ArrayList<>(entry.getValue())));
        }
        return revisionExamples;
    }

}
