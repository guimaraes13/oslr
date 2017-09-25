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

package br.ufrj.cos.knowledge.manager;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.manager.revision.point.RevisionExamples;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Conjunction;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.Literal;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for manage the theory as a tree.
 * <p>
 * Created on 20/06/17.
 *
 * @author Victor Guimarães
 */
public class TreeTheory implements Initializable {

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
     * Removes the node from the tree.
     *
     * @param node the node
     * @return {@code true} if the remove changes the tree, {@code false} otherwise
     */
    public static boolean removeNodeFromTree(Node node) {
        return node.removeNodeFromTree();
    }

    @Override
    public void initialize() throws InitializationException {
        //IMPROVE: initialize the treeMap from the current theory
        treeMap = new HashMap<>();
        leafExamplesMap = new HashMap<>();
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
            root = Node.newTree(buildDefaultTheory(head), buildDefaultTheory(head));
            treeMap.put(predicate, root);
        }

        return root;
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
