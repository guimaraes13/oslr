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

package br.ufrj.cos.engine;

import br.ufrj.cos.knowledge.Knowledge;
import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.generalization.BottomClauseBoundedRule;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.FunctionalSymbol;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.util.Initializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static br.ufrj.cos.util.log.EngineSystemLog.INITIALIZING_ENGINE_SYSTEM_TRANSLATOR;

/**
 * Represents an facade to be in between the system and the logic inference engine. It is useful to isolate the
 * internal representation of the system from the logic representation of the inference engine, so the logic engine
 * could be easily replaced by simplify implementing another subclass of this one.
 * <p>
 * In addition, it implements {@link ThreadLocal}, which isolates the changes made by each thread. This class does
 * not store examples, only the knowledge base and theory. And only the theory and internal parameters are guaranteed
 * to be thread-local by the {@link ThreadLocal}.
 * <p>
 * Created on 25/04/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class EngineSystemTranslator extends ThreadLocal<EngineSystemTranslator> implements Initializable {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    protected KnowledgeBase knowledgeBase;
    protected Knowledge<FunctionalSymbol> functionBase;
    protected Theory theory;

    @Override
    public void initialize() {
        logger.debug(INITIALIZING_ENGINE_SYSTEM_TRANSLATOR.toString(), this.getClass().getName());
    }

    @SuppressWarnings("AbstractMethodOverridesConcreteMethod")
    @Override
    protected abstract EngineSystemTranslator initialValue();

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
     * @param examples the array to ground/prove
     * @return the grounded/proved set of {@link Atom}s
     */
    public abstract Set<Atom> groundExamples(Example... examples);

    /**
     * Method to call the logic engine and retrieve the grounding/proved form of the given iterator. This method must
     * be as simple as possible and returns only the grounded iterator. No probabilities are required here.
     *
     * @param examples the array to ground/prove
     * @return the grounded/proved set of {@link Atom}s
     */
    public abstract Set<Atom> groundExamples(Iterable<? extends Example> examples);

    /**
     * Method to train the parameters of the logic engine.
     *
     * @param examples the array to train with
     */
    public abstract void trainParameters(Example... examples);

    /**
     * Method to train the parameters of the logic engine.
     *
     * @param examples the iterable to train with
     */
    public abstract void trainParameters(Iterable<? extends Example> examples);

    /**
     * Saves the last trained parameters to the current parameters.
     */
    public abstract void saveTrainedParameters();

    /**
     * Method to infer the probability of the examples based on the {@link KnowledgeBase} and the parameters from the
     * logic engine.
     *
     * @param examples the array to infer
     * @return a {@link Map} of the solutions to its correspondent {@link Example}s.
     */
    public abstract Map<Example, Map<Atom, Double>> inferExamples(Example... examples);

    /**
     * Method to infer the probability of the examples based on the {@link KnowledgeBase} and the parameters from the
     * logic engine.
     *
     * @param examples the iterable to infer
     * @return a {@link Map} of the solutions to its correspondent {@link Example}s.
     */
    public abstract Map<Example, Map<Atom, Double>> inferExamples(Iterable<? extends Example> examples);

    /**
     * Method to infer the probability of the examples based on the {@link Theory}, {@link KnowledgeBase} and the
     * parameters from the logic engine. The parameters changes due the call of this method should not be stored.
     * <p>
     * This method is useful to evaluate a theory revision without save the parameters.
     *
     * @param theory   the {@link Theory}
     * @param examples the iterable to infer
     * @return a {@link Map} of the solutions to its correspondent {@link Example}s.
     */
    public abstract Map<Example, Map<Atom, Double>> inferExamples(Theory theory, Example... examples);

    /**
     * Method to infer the probability of the examples based on the {@link Theory}, {@link KnowledgeBase} and the
     * parameters from the logic engine. The parameters changes due the call of this method should not be stored.
     * <p>
     * This method is useful to evaluate a theory revision without save the parameters.
     *
     * @param theory   the {@link Theory}
     * @param examples the iterable to infer
     * @return a {@link Map} of the solutions to its correspondent {@link Example}s.
     */
    public abstract Map<Example, Map<Atom, Double>> inferExamples(Theory theory, Iterable<? extends Example> examples);

    /**
     * Method to infer the probability of the examples based on the {@link HornClause}, {@link KnowledgeBase} and the
     * parameters from the logic engine. The parameters changes due the call of this method should not be stored.
     * <p>
     * This method is useful to evaluate a clause revision without save the parameters.
     *
     * @param clause   the {@link HornClause}
     * @param examples the iterable to infer
     * @return a {@link Map} of the solutions to its correspondent {@link Example}s.
     */
    public abstract Map<Example, Map<Atom, Double>> inferExamples(HornClause clause,
                                                                  Iterable<? extends Example> examples);

    /**
     * Method to infer the probability of the examples based on the {@link Theory} (appending new clauses),
     * {@link KnowledgeBase} and the parameters from the logic engine. The parameters changes due the call of this
     * method should not be stored.
     * <p>
     * This method is useful to evaluate a theory revision without save the parameters.
     *
     * @param appendClauses the {@link HornClause} to append
     * @param examples      the iterable to infer
     * @return a {@link Map} of the solutions to its correspondent {@link Example}s.
     */
    public abstract Map<Example, Map<Atom, Double>> inferExamples(Iterable<? extends HornClause> appendClauses,
                                                                  Iterable<? extends Example> examples);

    /**
     * Method to infer the probability of the examples based on the {@link Theory} (appending new clauses),
     * {@link KnowledgeBase} and the parameters from the logic engine. The parameters changes due the call of this
     * method should not be stored.
     * <p>
     * This method is useful to evaluate a theory revision without save the parameters.
     *
     * @param appendClauses the {@link HornClause} to append
     * @param examples      the iterable to infer
     * @return a {@link Map} of the solutions to its correspondent {@link Example}s.
     */
    public abstract Map<Example, Map<Atom, Double>> inferExamples(Iterable<? extends HornClause> appendClauses,
                                                                  Example... examples);

    /**
     * Method to infer the probabilities of the grounds in the iterator based on the {@link KnowledgeBase}, training
     * the parameters before inference. The parameters changes due the call of this method should not be stored.
     * <p>
     * This method is useful to evaluate a theory revision without save the parameters.
     *
     * @param examples the iterable to infer
     * @return a {@link Map} of the solutions to its correspondent {@link Example}.
     */
    public abstract Map<Example, Map<Atom, Double>> inferExampleTrainingParameters(Iterable<? extends Example>
                                                                                           examples);

    /**
     * Method to infer the probabilities of the examples in the iterator based on the {@link Theory} and
     * {@link KnowledgeBase}, training the parameters before inference. The changes due the call of this
     * method should not be stored.
     * <p>
     * This method is useful to evaluate a theory revision without save the parameters.
     *
     * @param theory   the {@link Theory}
     * @param examples the iterable to infer
     * @return a {@link Map} of the solutions to its correspondent {@link Example}.
     */
    public abstract Map<Example, Map<Atom, Double>> inferExampleTrainingParameters
    (Theory theory, Iterable<? extends Example> examples);

    /**
     * Method to infer the probability of the examples based on the {@link Theory} (appending new clauses),
     * {@link KnowledgeBase}, training the parameters before inference. The changes due the call of this
     * method should not be stored.
     * <p>
     * This method is useful to evaluate a theory revision without save the parameters.
     *
     * @param appendClauses the {@link HornClause} to append
     * @param examples      the iterable to infer
     * @return a {@link Map} of the solutions to its correspondent {@link Example}s.
     */
    public abstract Map<Example, Map<Atom, Double>> inferExampleTrainingParameters
    (Iterable<? extends HornClause> appendClauses, Iterable<? extends Example> examples);

    /**
     * Gets the {@link KnowledgeBase}.
     *
     * @return the {@link KnowledgeBase}
     */
    public KnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }

    /**
     * Sets the {@link KnowledgeBase}.
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     */
    public abstract void setKnowledgeBase(KnowledgeBase knowledgeBase);

    /**
     * Gets the function {@link Knowledge}.
     *
     * @return the function {@link KnowledgeBase}
     */
    public Knowledge<FunctionalSymbol> getFunctionBase() {
        return functionBase;
    }

    /**
     * Sets the function {@link Knowledge}.
     *
     * @param functionBase the function {@link Knowledge}
     */
    public abstract void setFunctionBase(Knowledge<FunctionalSymbol> functionBase);

    /**
     * Gets the {@link Theory}.
     *
     * @return the {@link Theory}
     */
    public Theory getTheory() {
        return theory;
    }

    /**
     * Sets the {@link Theory}.
     *
     * @param theory the {@link Theory}
     */
    public abstract void setTheory(Theory theory);

    /**
     * Method to save the necessary parameters to files in the working directory.
     *
     * @param workingDirectory the working directory
     */
    public abstract void saveParameters(File workingDirectory);

    /**
     * Method to load the saved parameters from files in the working directory.
     *
     * @param workingDirectory the working directory
     */
    public abstract void loadParameters(File workingDirectory);

    /**
     * Adds the atoms to the knowledge of the system translator.
     *
     * @param atoms the atoms to be added
     */
    public abstract void addAtomsToKnowledgeBase(Collection<? extends Atom> atoms);

}
