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

package br.ufrj.cos.engine.proppr;

import br.ufrj.cos.engine.EngineSystemTranslator;
import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.ExampleSet;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.logic.Atom;

import java.util.Set;

/**
 * Translator to convert the system's syntax to ProPPR, and vice versa
 * <p>
 * Created on 25/04/17.
 *
 * @author Victor Guimarães
 */
public class ProPprEngineSystemTranslator extends EngineSystemTranslator {

    /**
     * Constructs the class if the minimum required parameters
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     * @param theory        the {@link Theory}
     * @param examples      the {@link ExampleSet}
     */
    public ProPprEngineSystemTranslator(KnowledgeBase knowledgeBase, Theory theory, ExampleSet examples) {
        super(knowledgeBase, theory, examples);
    }

    @Override
    public Set<Atom> groundingExamples(Example... examples) {
        //TODO: not implemented yet
        return null;
    }

}
