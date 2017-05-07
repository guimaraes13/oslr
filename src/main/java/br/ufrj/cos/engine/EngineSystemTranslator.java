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

package br.ufrj.cos.engine;

import br.ufrj.cos.knowledge.Knowledge;
import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.generalization.BottomClauseBoundedRule;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.logic.WeightedAtom;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Represents an facade to be in between the system and the logic inference engine. It is useful for isolate the
 * internal representation of the system from the logic representation of the inference engine, so the logic engine
 * could be easily replaced by simplify implementing another subclass of this one.
 * <p>
 * Created on 25/04/17.
 *
 * @author Victor Guimarães
 */
public abstract class EngineSystemTranslator {

    protected KnowledgeBase knowledgeBase;
    protected Theory theory;
    protected Examples examples;

    /**
     * Constructs the class if the minimum required parameters.
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     * @param theory        the {@link Theory}
     * @param examples      the {@link Examples}
     */
    public EngineSystemTranslator(KnowledgeBase knowledgeBase, Theory theory, Examples examples) {
        this.knowledgeBase = knowledgeBase;
        this.theory = theory;
        this.examples = examples;
    }

    /**
     * Method to call the logic engine and retrieve the grounding/proved {@link Atom} relevant to the given
     * {@link Term}s.
     * <p>
     * An {@link Atom} is relevant to a {@link Term} if it contains it, transitively.
     *
     * @param terms the {@link Term}s
     * @return the relevant {@link Atom}s.
     * @see BottomClauseBoundedRule
     */
    public abstract Set<Atom> groundRelevants(Collection<Term> terms);

    /**
     * Method to call the logic engine and retrieve the grounding/proved form of the given iterator. This method must
     * be as simple as possible and returns only the grounded iterator. No probabilities are required here.
     *
     * @param examples the iterator to ground/prove
     * @return the grounded/proved set of {@link Atom}s
     */
    public abstract Set<Atom> groundExamples(Example... examples);

    /**
     * Method to train the parameters of the logic engine.
     *
     * @param examples the iterator to train with
     */
    public abstract void trainParameters(Example... examples);

    /**
     * Saves the last trained parameters to the current parameters.
     */
    public abstract void saveTrainedParameters();

    /**
     * Method to infer the probability of the iterator based on the {@link Knowledge} and the parameters from the
     * logic engine.
     *
     * @param examples the iterator to infer
     * @return a {@link Map} of the solutions to its correspondent {@link Example}.
     */
    public abstract Map<Example, Set<WeightedAtom>> inferExample(Example... examples);

}
