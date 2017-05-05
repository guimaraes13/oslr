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

package br.ufrj.cos.knowledge.theory.manager;

import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.manager.revision.RevisionManager;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;

/**
 * Responsible for applying the revision on theory, whenever it is called to.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
public abstract class TheoryRevisionManager {

    protected final RevisionManager revisionManager;

    /**
     * Constructs the class if the minimum required parameters
     *
     * @param revisionManager the {@link RevisionManager}
     */
    public TheoryRevisionManager(RevisionManager revisionManager) {
        this.revisionManager = revisionManager;
    }

    /**
     * Method to call the revision of the {@link Theory} on the {@link RevisionManager}
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     * @param theory        the {@link Theory}
     * @param examples      the {@link Examples}
     * @param targets       the target {@link Example}s
     * @throws TheoryRevisionException in case an error occurs on the revision
     */
    public void revise(KnowledgeBase knowledgeBase, Theory theory, Examples examples,
                       Example... targets) throws TheoryRevisionException {
        revisionManager.revise(knowledgeBase, theory, examples, targets);
    }

}
