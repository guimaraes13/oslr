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

package br.ufrj.cos.core;

import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.ExampleSet;
import br.ufrj.cos.knowledge.manager.IncomingExampleManager;
import br.ufrj.cos.knowledge.manager.KnowledgeBaseManager;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.manager.TheoryEvaluator;
import br.ufrj.cos.knowledge.theory.manager.TheoryRevisionManager;

/**
 * Responsible for the execution and control of the entire system.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
public class LearningSystem {

    //Core Knowledge
    protected KnowledgeBase knowledgeBase;
    protected Theory theory;
    protected ExampleSet examples;

    //Knowledge Manager
    protected KnowledgeBaseManager knowledgeBaseManager;
    protected IncomingExampleManager incomingExampleManager;

    //Theory Manager
    protected TheoryRevisionManager theoryRevisionManager;
    protected TheoryEvaluator theoryEvaluator;

    //TODO: implement necessary methods!
    // It does not need to do all by itself know, just have the methods to receive the commands from outside sources!
    // In the future, create an extension of this class that have an examples input stream and decides what to do based on that.

}
