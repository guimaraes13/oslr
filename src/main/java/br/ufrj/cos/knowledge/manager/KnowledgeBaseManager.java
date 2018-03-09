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
@SuppressWarnings({"AbstractClassNeverImplemented", "ClassMayBeInterface", "unused"})
public abstract class KnowledgeBaseManager {

    /**
     * Sets the {@link KnowledgeBase}.
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     */
    public abstract void setKnowledgeBase(KnowledgeBase knowledgeBase);

}
