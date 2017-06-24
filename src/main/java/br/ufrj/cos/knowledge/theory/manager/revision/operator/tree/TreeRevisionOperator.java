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

import br.ufrj.cos.knowledge.manager.TreeTheory;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.RevisionOperator;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;

/**
 * Super class for revision operator that performs operation in {@link TreeTheory}.
 * <p>
 * Created on 20/06/17.
 *
 * @author Victor Guimarães
 */
public abstract class TreeRevisionOperator extends RevisionOperator {

    protected TreeTheory treeTheory;

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        if (this.treeTheory == null) {
            throw new InitializationException(
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                   TreeTheory.class.getSimpleName()));
        }
    }

    /**
     * Gets the tree theory.
     *
     * @return tree theory
     */
    public TreeTheory getTreeTheory() {
        return treeTheory;
    }

    /**
     * Sets the tree theory.
     *
     * @param treeTheory tree theory
     * @throws InitializationException if the {@link TreeTheory} is already set
     */
    public void setTreeTheory(TreeTheory treeTheory) throws InitializationException {
        if (this.treeTheory != null) {
            throw new InitializationException(
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                   TreeTheory.class.getSimpleName()));
        }
        this.treeTheory = treeTheory;
    }

}
