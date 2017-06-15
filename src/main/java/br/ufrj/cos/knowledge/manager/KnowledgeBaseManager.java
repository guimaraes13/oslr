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

import br.ufrj.cos.knowledge.base.KnowledgeBase;

/**
 * Responsible for managing the knowledge. It should hold methods to decide whether to move a fact (clause) from the
 * knowledge base to the atomExamples (theory) or the opposite.
 * <p>
 * Those methods should not make the changes directly, but invoke other methods on the
 * {@link br.ufrj.cos.core.LearningSystem} to do so.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings({"AbstractClassNeverImplemented", "ClassMayBeInterface"})
public abstract class KnowledgeBaseManager {

    /**
     * Sets the {@link KnowledgeBase}.
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     */
    public abstract void setKnowledgeBase(KnowledgeBase knowledgeBase);

}
