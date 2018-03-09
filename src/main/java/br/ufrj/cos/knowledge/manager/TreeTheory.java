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

package br.ufrj.cos.knowledge.manager;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.manager.revision.point.RevisionExamples;
import br.ufrj.cos.logic.*;
import br.ufrj.cos.util.LanguageUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Responsible for manage the theory as a tree.
 * <p>
 * Created on 20/06/17.
 *
 * @author Victor Guimarães
 */
public class TreeTheory {

    /**
     * Represents the default theory body.
     */
    protected static final Conjunction DEFAULT_THEORY_BODY = new Conjunction(Literal.FALSE_LITERAL);
    /**
     * The leaf that represent the revision point.
     */
    public List<Node<HornClause>> revisionLeaves;

    /**
     * The current revision index.
     */
    public int revisionLeafIndex;

    protected Map<String, Node<HornClause>> treeMap;
    protected Map<String, Map<Node<HornClause>, RevisionExamples>> leafExamplesMap;

    /**
     * Checks if the node represents a default theory.
     *
     * @param node the node
     * @return {@code true} if it does, {@code false} otherwise
     */
    public static boolean isDefaultTheory(Node<HornClause> node) {
        if (!node.isRoot()) {
            return false;
        }

        return node.getElement().getBody().equals(DEFAULT_THEORY_BODY);
    }

    /**
     * Removes the node from the tree.
     *
     * @param node the node
     * @return {@code true} if the remove changes the tree, {@code false} otherwise
     */
    public static boolean removeNodeFromTree(Node node) {
        return node.removeNodeFromTree();
    }

    /**
     * Initializes the tree with the theory.
     *
     * @param theory the theory
     */
    public void initialize(Theory theory) {
        leafExamplesMap = new HashMap<>();
        treeMap = new HashMap<>();
        if (!theory.isEmpty()) { buildTreeMap(theory); }
    }

    /**
     * Adds the child to the parent node.
     *
     * @param parent the parent node
     * @param child  the child
     * @return the child node
     */
    public static Node<HornClause> addNodeToTree(Node<HornClause> parent, HornClause child) {
        return parent.addChildToNode(child, buildDefaultTheory(parent.getElement().getHead()));
    }

    /**
     * Builds a default {@link Theory} for a given head
     *
     * @param head the head
     * @return the {@link Theory}
     */
    protected static HornClause buildDefaultTheory(Atom head) {
        Conjunction body = new Conjunction(Literal.FALSE_LITERAL);
        return new HornClause(head, body);
    }

    /**
     * Builds the tree map of the theory
     *
     * @param theory the theory
     */
    protected void buildTreeMap(Theory theory) {
        //IMPROVE: initialize the treeMap from the current theory
        final Map<Predicate, List<HornClause>> clausesByPredicate = new HashMap<>();
        LanguageUtils.splitClausesByPredicateToList(theory, clausesByPredicate, c -> c.getHead().getPredicate());
        for (Map.Entry<Predicate, List<HornClause>> entry : clausesByPredicate.entrySet()) {
            Iterator<HornClause> iterator = entry.getValue().iterator();
            HornClause currentClause = iterator.next();
            final Atom head = currentClause.getHead();
            final Node<HornClause> root = buildInitialTree(head.getPredicate().toString(), head);
            root.getElement().getBody().clear();
            root.getElement().getBody().add(Literal.TRUE_LITERAL);
            addNodesToTree(currentClause, root, new Conjunction());
            while (iterator.hasNext()) {
                currentClause = iterator.next();
                Node<HornClause> parentNode = findParentNode(root, currentClause);
                addNodesToTree(currentClause, parentNode, parentNode.getElement().getBody());
            }
        }
    }

    /**
     * Builds the initial tree for a head.
     *
     * @param predicate the predicate
     * @param head      the head
     * @return the initial tree's root
     */
    protected Node<HornClause> buildInitialTree(String predicate, Atom head) {
        Node<HornClause> root;
        root = Node.newTree(buildDefaultTheory(head), buildDefaultTheory(head));
        treeMap.put(predicate, root);
        return root;
    }

    /**
     * Adds the nodes from the modified clause to the tree.
     *
     * @param clause       the clause
     * @param revisionLeaf the revised leaf
     * @param initialBody  the initial body
     */
    public static void addNodesToTree(HornClause clause, Node<HornClause> revisionLeaf, Conjunction initialBody) {
        Atom head = revisionLeaf.getElement().getHead();
        Conjunction currentBody = initialBody;
        Conjunction nextBody;
        Node<HornClause> node = revisionLeaf;
        for (Literal literal : clause.getBody()) {
            if (currentBody.contains(literal)) { continue; }
            if (currentBody.size() == 1 && currentBody.contains(Literal.TRUE_LITERAL)) { currentBody.clear(); }
            nextBody = new Conjunction(currentBody.size() + 1);
            nextBody.addAll(currentBody);
            nextBody.add(literal);
            node = addNodeToTree(node, new HornClause(head, nextBody));
            currentBody = nextBody;
        }
    }

    /**
     * Finds the parent node of the clause in the tree.
     *
     * @param root   the root of the tree
     * @param clause the clause
     * @return the parent node
     */
    protected static Node<HornClause> findParentNode(Node<HornClause> root, HornClause clause) {
        Node<HornClause> parent = root;
        for (Literal literal : clause.getBody()) {
            if (parent.getChildren() == null || parent.getChildren().isEmpty()) { break; }
            @SuppressWarnings("BooleanVariableAlwaysNegated") boolean found = false;
            for (Node<HornClause> child : parent.getChildren()) {
                if (child.getElement().getBody().contains(literal)) {
                    parent = child;
                    found = true;
                    break;
                }
            }
            if (!found) { break; }
        }
        return parent;
    }

    /**
     * Retrieves the root of the tree responsible for dealing with the given example. If the tree does not exists
     * yet, it is created.
     *
     * @param example   the example
     * @param predicate the predicate of the example
     * @return the tree
     */
    public Node<HornClause> getTreeForExample(Example example, String predicate) {
        Node<HornClause> root = treeMap.get(predicate);
        if (root == null) {
            Atom head = LanguageUtils.toVariableAtom(example.getGoalQuery().getPredicate());
            root = buildInitialTree(predicate, head);
        }

        return root;
    }

    /**
     * Gets the tree from the predicate.
     *
     * @param predicate the predicate
     * @return the tree
     */
    public Node<HornClause> getTreeByPredicate(String predicate) {
        return treeMap.get(predicate);
    }

    /**
     * Gets the leaf examples map for the predicate. Computes a new one if it is absent.
     *
     * @param predicate the predicate
     * @return the leaf examples map
     */
    public Map<Node<HornClause>, RevisionExamples> getLeafExampleMapFromTree(String predicate) {
        return leafExamplesMap.computeIfAbsent(predicate, e -> new HashMap<>());
    }

    /**
     * Removes the set of examples from the leaf map of the predicate.
     *
     * @param predicate the predicate
     * @param leaf      the leaf
     * @return the removed set of examples
     */
    public RevisionExamples removeExampleFromLeaf(String predicate, Node<HornClause> leaf) {
        return leafExamplesMap.get(predicate).remove(leaf);
    }

    /**
     * Gets the set of examples from the leaf map of the predicate.
     *
     * @param predicate the predicate
     * @param leaf      the leaf
     * @return the set of examples
     */
    public RevisionExamples getExampleFromLeaf(String predicate, Node<HornClause> leaf) {
        return leafExamplesMap.get(predicate).get(leaf);
    }

    /**
     * Gets the current revision leaf.
     *
     * @return the current revision leaf
     */
    public Node<HornClause> getRevisionLeaf() {
        return revisionLeaves.get(revisionLeafIndex);
    }

    /**
     * Gets the current revision leaf.
     *
     * @param index the index
     * @return the current revision leaf
     */
    public Node<HornClause> getRevisionLeaf(int index) {
        return revisionLeaves.get(index);
    }

}
