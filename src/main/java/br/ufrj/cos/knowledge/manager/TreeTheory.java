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
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Conjunction;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.Literal;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Responsible for manage the theory as a tree.
 * <p>
 * Created on 20/06/17.
 *
 * @author Victor Guimarães
 */
public class TreeTheory implements Initializable {

    /**
     * The leaf that represent the revision point.
     */
    public Node<Theory> signedLeaf;

    protected Map<String, Node<Theory>> treeMap;
    protected Map<String, Map<Node<Theory>, Set<Example>>> leafExamplesMap;

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
    public Node<Theory> getTreeByPredicate(String predicate) {
        return treeMap.get(predicate);
    }

    /**
     * Gets the leaf examples map for the predicate. Computes a new one if it is absent.
     *
     * @param predicate the predicate
     * @return the leaf examples map
     */
    public Map<Node<Theory>, Set<Example>> getLeafExampleMapFromTree(String predicate) {
        return leafExamplesMap.computeIfAbsent(predicate, e -> new HashMap<>());
    }

    /**
     * Gets the set of examples from the leaf map of the predicate.
     *
     * @param predicate the predicate
     * @param leaf      the leaf
     * @return the set of examples
     */
    public Set<Example> getExampleFromLeaf(String predicate, Node<Theory> leaf) {
        return leafExamplesMap.get(predicate).get(leaf);
    }

    /**
     * Retrieves the root of the tree responsible for dealing with the given example. If the tree does not exists
     * yet, it is created.
     *
     * @param example         the example
     * @param predicate       the predicate of the example
     * @param acceptPredicate tje accept predicate of the theory
     * @return the tree
     */
    public Node<Theory> getTreeForExample(Example example, String predicate,
                                          Predicate<? super HornClause> acceptPredicate) {
        Node<Theory> root = treeMap.get(predicate);
        if (root == null) {
            Atom head = LanguageUtils.toVariableAtom(example.getGoalQuery().getName(), example.getGoalQuery()
                    .getArity());
            root = Node.newTree(buildDefaultTheory(head, acceptPredicate), buildDefaultTheory(head, acceptPredicate));
            treeMap.put(predicate, root);
        }

        return root;
    }

    /**
     * Builds a default {@link Theory} for a given example
     *
     * @param example         the example
     * @param acceptPredicate tje accept predicate of the theory
     * @return the {@link Theory}
     */
    protected static Theory buildDefaultTheory(Atom example, Predicate<? super HornClause> acceptPredicate) {
        Conjunction body = new Conjunction(Literal.FALSE_LITERAL);
        return new Theory(new HornClause(example, body), acceptPredicate);
    }

}
