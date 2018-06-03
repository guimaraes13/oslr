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

package br.ufrj.cos.engine.proppr;

import br.ufrj.cos.engine.EngineSystemTranslator;
import br.ufrj.cos.engine.proppr.ground.Ground;
import br.ufrj.cos.engine.proppr.ground.InMemoryGrounder;
import br.ufrj.cos.engine.proppr.ground.InferenceExampleIterable;
import br.ufrj.cos.engine.proppr.query.answerer.Answer;
import br.ufrj.cos.engine.proppr.query.answerer.InMemoryQueryAnswerer;
import br.ufrj.cos.engine.proppr.query.answerer.QueryIterable;
import br.ufrj.cos.knowledge.Knowledge;
import br.ufrj.cos.knowledge.base.FunctionsPlugin;
import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.logic.*;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.IterableConverter;
import br.ufrj.cos.util.LanguageUtils;
import br.ufrj.cos.util.log.FileIOLog;
import edu.cmu.ml.proppr.Trainer;
import edu.cmu.ml.proppr.examples.GroundedExample;
import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.graph.ArrayLearningGraphBuilder;
import edu.cmu.ml.proppr.graph.InferenceGraph;
import edu.cmu.ml.proppr.learn.RegularizationSchedule;
import edu.cmu.ml.proppr.learn.Regularize;
import edu.cmu.ml.proppr.learn.RegularizeL2;
import edu.cmu.ml.proppr.learn.SRW;
import edu.cmu.ml.proppr.learn.tools.ClippedExp;
import edu.cmu.ml.proppr.learn.tools.SquashingFunction;
import edu.cmu.ml.proppr.prove.DprProver;
import edu.cmu.ml.proppr.prove.Prover;
import edu.cmu.ml.proppr.prove.wam.*;
import edu.cmu.ml.proppr.prove.wam.plugins.FactsPlugin;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.*;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.math.ParamVector;
import edu.cmu.ml.proppr.util.math.SimpleParamVector;
import edu.cmu.ml.proppr.util.multithreading.Multithreading;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static br.ufrj.cos.engine.proppr.ProPprUtils.getLabelForRule;
import static br.ufrj.cos.util.log.EngineSystemLog.*;
import static edu.cmu.ml.proppr.Trainer.DEFAULT_CAPACITY;
import static edu.cmu.ml.proppr.Trainer.DEFAULT_LOAD;

/**
 * Translator to convert the system's syntax to ProPPR, and vice versa.
 * <p>
 * Created on 25/04/17.
 *
 * @author Victor Guimarães
 */
public class ProPprEngineSystemTranslator<P extends ProofGraph> extends EngineSystemTranslator {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * The facts plugins name.
     */
    public static final String FACTS_PLUGIN_NAME = "facts";
    /**
     * The functions plugins name.
     */
    public static final String FUNCTIONS_PLUGIN_NAME = "functions";
    /**
     * No limits on the number of solutions.
     */
    public static final int NO_MAX_SOLUTIONS = -1;
    /**
     * Name of the saved parameters file.
     */
    public static final String SAVED_PARAMETERS_FILE_NAME = "savedParameters.wts";
    /**
     * Name of the saved feature theory file.
     */
    public static final String SAVED_FEATURE_THEORY = "savedFeatureTheory.pl";
    /**
     * The default empty query array.
     */
    public static final Query[] EMPTY_QUERY = new Query[0];
    /**
     * The map of predicates by the string on the form p/n, where p is the name of the predicate and n is its arity.
     */
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Map<String, Predicate> PREDICATE_MAP = new HashMap<>();
    /**
     * If is to use ternay index, makes an more efficient cache for predicates with arity.
     */
    public boolean useTernayIndex = false;
    /**
     * The number of training epochs per training.
     */
    public int numberOfTrainingEpochs = 5;
    /**
     * The number of threads this class is allowed to use.
     */
    public int numberOfThreads = 1;
    /**
     * If it is to normalizeAnswers.
     */
    public boolean normalizeAnswers = true;
    /**
     * The {@link APROptions}.
     */
    public APROptions aprOptions = new APROptions();
    /**
     * The {@link Regularize}.
     */
    public Regularize regularize = new RegularizeL2();
    /**
     * The {@link Prover}
     */
    public Prover<P> prover;
    /**
     * The {@link SquashingFunction}
     */
    public SquashingFunction<Goal> squashingFunction = new ClippedExp<>();
    /**
     * The {@link SRW} options.
     */
    protected SRW srw = new SRW();
    // Input
    protected FactsPlugin factsPlugin;
    protected FunctionsPlugin functionsPlugin = new FunctionsPlugin(aprOptions, FUNCTIONS_PLUGIN_NAME);
    protected WamProgram program;

    // Processing
    protected InMemoryGrounder<P> grounder;
    protected Trainer trainer;
    protected InMemoryQueryAnswerer<P> answerer;

    // Parameters
    protected ParamVector<String, ?> currentParamVector;
    protected ParamVector<String, ?> savedParamVector;
    protected Collection<Rule> featureRules;

    /**
     * Converts an {@link Atom} to a {@link Query}.
     *
     * @param atom the {@link Atom}
     * @return the {@link Query}
     */
    public static Query atomToQuery(Atom atom) {
        return new Query(ProPprUtils.atomToGoal(atom, new HashMap<>()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void initialize() {
        super.initialize();
        if (this.prover == null) { this.prover = (Prover<P>) new DprProver(aprOptions); }
        this.prover.apr = aprOptions;
        this.grounder = new InMemoryGrounder<>(numberOfThreads, Multithreading.DEFAULT_THROTTLE, aprOptions, prover,
                                               program, functionsPlugin, factsPlugin);
        this.srw = new SRW(new SRWOptions(aprOptions, squashingFunction));
        //IMPROVE: set srw random seed
        this.srw.setRegularizer(new RegularizationSchedule(this.srw, regularize));
        this.trainer = new Trainer(srw, numberOfThreads, Multithreading.DEFAULT_THROTTLE);
        this.savedParamVector = new SimpleParamVector<>(new ConcurrentHashMap<String, Double>(DEFAULT_CAPACITY,
                                                                                              DEFAULT_LOAD,
                                                                                              numberOfThreads));
        this.currentParamVector = new SimpleParamVector<>(new ConcurrentHashMap<String, Double>(DEFAULT_CAPACITY,
                                                                                                DEFAULT_LOAD,
                                                                                                numberOfThreads));
        this.answerer = buildAnswerer();
        answerer.addParams(prover, savedParamVector, squashingFunction);
    }

    @Override
    protected synchronized EngineSystemTranslator initialValue() {
        ProPprEngineSystemTranslator copy = new ProPprEngineSystemTranslator();
        copy.useTernayIndex = this.useTernayIndex;
        copy.numberOfTrainingEpochs = this.numberOfTrainingEpochs;
        copy.numberOfThreads = this.numberOfThreads;
        copy.normalizeAnswers = this.normalizeAnswers;
        copy.aprOptions = this.aprOptions;
        copy.srw = new SRW(new SRWOptions(aprOptions, squashingFunction));
        copy.srw.setRegularizer(new RegularizationSchedule(this.srw, regularize));
        copy.regularize = regularize;
        copy.prover = this.prover.copy();
        copy.squashingFunction = this.squashingFunction;
        copy.knowledgeBase = this.knowledgeBase;
        copy.setTheory(this.theory);
        copy.initialize();
        return copy;
    }

    @Override
    public synchronized Set<Atom> groundRelevants(Collection<Term> terms) {
        Term clauseTerm;
        Set<InferenceExample> inferenceExamples = new HashSet<>();
        Map<String, Set<Atom>> coveredGoals = new HashMap<>();
        for (HornClause clause : theory) {
            if (isToSkipClause(clause.getHead(), coveredGoals)) { continue; }
            for (int i = 0; i < clause.getHead().getArity(); i++) {
                clauseTerm = clause.getHead().getTerms().get(i);
                if (!clauseTerm.isConstant()) {
                    for (Term relevant : terms) {
                        inferenceExamples.add(buildRelevantExample(clause.getHead(), relevant, i));
                    }
                }
            }
        }
        return getGroundedAtoms(inferenceExamples);
    }

    /**
     * Checks if the goal of the clause has been already covered by another clause. If it has, skips the clause.
     * <p>
     * In addition, it adds the goal to the coveredGoal, if it is not covered yet.
     *
     * @param goal         the goal
     * @param coveredGoals the goals already covered
     * @return {@code true} if the goal of the clause has been already covered by another clause
     */
    protected static boolean isToSkipClause(Atom goal, Map<String, Set<Atom>> coveredGoals) {
        String label = goal.getPredicate().toString();
        Set<Atom> atoms = coveredGoals.get(label);
        if (atoms == null) {
            atoms = new HashSet<>();
            atoms.add(goal);
            coveredGoals.put(label, atoms);
            return false;
        } else {
            for (Atom atom : atoms) {
                if (LanguageUtils.isAtomUnifiableToGoal(atom, goal)) {
                    return true;
                }
            }
            atoms.add(goal);
            return false;
        }
    }

    /**
     * Builds the relevant {@link InferenceExample} that servers as a ProPPR's query to retrieve relevant
     * inferred {@link Atom}s.
     * <p>
     * This is done by replacing a variable {@link Term} of the goal by a relevant {@link Term}, this ensures that
     * only relevant grounds will be inferred, avoiding unnecessary grounding.
     *
     * @param goalAtom     the goal of the query
     * @param relevantTerm the relevant {@link Term} to replace an {@link Term} of the goal.
     * @param termIndex    the index of the goal's {@link Term} to be replaced by the relevant
     * @return the relevant {@link InferenceExample}
     */
    protected static InferenceExample buildRelevantExample(Atom goalAtom, Term relevantTerm, int termIndex) {
        List<Term> terms = new ArrayList<>(goalAtom.getTerms());
        terms.set(termIndex, relevantTerm);
        Query query = new Query(ProPprUtils.atomToGoal(goalAtom.getName(), terms, new HashMap<>()));

        return new InferenceExample(query, EMPTY_QUERY, EMPTY_QUERY);
    }

    /**
     * Gets the grounded {@link Atom}s from the {@link InferenceExample}.
     *
     * @param examples the {@link InferenceExample} {@link Iterable}
     * @return the grounded {@link Atom}s
     */
    protected Set<Atom> getGroundedAtoms(Iterable<InferenceExample> examples) {
        Map<Integer, Ground<P>> groundMap = grounder.groundExamples(examples, new SimpleSymbolTable<>());
        Set<Atom> atoms = new HashSet<>();
        for (Ground<?> ground : groundMap.values()) {
            atoms.addAll(groundToAtoms(ground));
        }
        return atoms;
    }

    /**
     * Converts the grounded of a {@link Ground} to {@link Atom}s.
     *
     * @param ground the {@link Ground}
     * @return the {@link Atom}s
     */
    @SuppressWarnings("OverlyCoupledMethod")
    protected static Collection<Atom> groundToAtoms(Ground ground) {
        ProofGraph proofGraph = ground.getProofGraph();
        GroundedExample groundedExample = ground.getGroundedExample();
        InferenceGraph graph = groundedExample.getGraph();

        State state;
        Query query;
        Set<Atom> atoms = new HashSet<>();
        for (int i = 1; i < graph.nodeSize() + 1; i++) {
            state = graph.getState(i);
            if (state.isCompleted()) {
                query = proofGraph.fill(state);
                for (Goal goal : query.getRhs()) {
                    atoms.add(goalToAtom(goal));
                }
            }
        }
        return atoms;
    }

    /**
     * Converts a {@link Goal} to an {@link Atom}.
     *
     * @param goal the {@link Goal}
     * @return the {@link Atom}
     */
    @SuppressWarnings("OverlyCoupledMethod")
    protected static Atom goalToAtom(Goal goal) {
        List<Term> terms = new ArrayList<>(goal.getArity());
        for (Argument argument : goal.getArgs()) {
            if (argument.isConstant()) {
                terms.add(new Constant(argument.getName()));
            } else {
                terms.add(new Variable(argument.getName()));
            }
        }
        Predicate value = PREDICATE_MAP.computeIfAbsent(LanguageUtils.formatPredicate(goal.getFunctor(), terms.size()),
                                                        k -> new Predicate(goal.getFunctor(), terms.size()));
        return new Atom(value, terms);
    }

    @Override
    public Set<Atom> groundExamples(Example... examples) {
        return getGroundedAtoms(new InferenceExampleIterable(examples));
    }

    @Override
    public synchronized void trainParameters(Example... examples) {
        logger.debug(TRAINING_PARAMETERS);
        currentParamVector = trainParameters(new InferenceExampleIterable(examples), savedParamVector, grounder);
    }

    @Override
    public synchronized void trainParameters(Iterable<? extends Example> examples) {
        logger.debug(TRAINING_PARAMETERS);
        currentParamVector = trainParameters(new InferenceExampleIterable(examples), savedParamVector, grounder);
    }

    @Override
    public synchronized void saveTrainedParameters() {
        logger.debug(SAVING_TRAINED_PARAMETERS_AS_CURRENT);
        savedParamVector = currentParamVector;
        answerer.addParams(prover, savedParamVector, squashingFunction);
    }

    @Override
    public Map<Example, Map<Atom, Double>> inferExamples(Example... examples) {
        return inferExamples(new QueryIterable(examples), answerer);
    }

    @Override
    public Map<Example, Map<Atom, Double>> inferExamples(Iterable<? extends Example> examples) {
        return inferExamples(new QueryIterable(examples), answerer);
    }

    @Override
    public Map<Example, Map<Atom, Double>> inferExamples(Theory theory,
                                                         Example... examples) {
        return inferWithTheoryExamples(theory, new QueryIterable(examples));
    }

    @Override
    public Map<Example, Map<Atom, Double>> inferExamples(Theory theory,
                                                         Iterable<? extends Example> examples) {
        return inferWithTheoryExamples(theory, new QueryIterable(examples));
    }

    @Override
    public Map<Example, Map<Atom, Double>> inferExamples(HornClause clause, Iterable<? extends Example> examples) {
        return inferWithTheoryExamples(Collections.singleton(clause), new QueryIterable(examples));
    }

    @Override
    public Map<Example, Map<Atom, Double>> inferExamples(Iterable<? extends HornClause> appendClauses,
                                                         Iterable<? extends Example> examples) {
        return inferExamplesAppendingClauses(appendClauses, new QueryIterable(examples));
    }

    @Override
    public Map<Example, Map<Atom, Double>> inferExamples(Iterable<? extends HornClause> appendClauses,
                                                         Example... examples) {
        return inferExamplesAppendingClauses(appendClauses, new QueryIterable(examples));
    }

    @Override
    public Map<Example, Map<Atom, Double>> inferExampleTrainingParameters(
            Iterable<? extends Example> examples) {

        ParamVector<String, ?> parameters = trainParameters(new InferenceExampleIterable(examples), savedParamVector,
                                                            grounder);
        InMemoryQueryAnswerer<P> answerer = buildAnswerer(parameters, program);
        return inferExamples(new QueryIterable(examples), answerer);
    }

    /**
     * After calling this method, the {@link #initialize()} method must be called, for the changes in the knowledge
     * base to take effect.
     * {@inheritDoc}
     *
     * @param functionBase the {@link KnowledgeBase}
     */
    @Override
    public synchronized void setFunctionBase(Knowledge<FunctionalSymbol> functionBase) {
        this.functionBase = functionBase;
        this.functionsPlugin = buildFunctionPlugin(aprOptions);
        addFunctionsToKnowledgeBase(functionBase);
    }

    @Override
    public Map<Example, Map<Atom, Double>> inferExampleTrainingParameters
            (Theory theory, Iterable<? extends Example> examples) {
        WamProgram wamProgram = ProPprUtils.compileTheory(theory);
        return inferExamplesTrainingParameters(examples, wamProgram);
    }

    @Override
    public Map<Example, Map<Atom, Double>> inferExampleTrainingParameters
            (Iterable<? extends HornClause> appendClauses, Iterable<? extends Example> examples) {
        WamProgram wamProgram = ProPprUtils.compileTheory(theory);
        ProPprUtils.appendRuleToProgram(appendClauses, wamProgram);
        return inferExamplesTrainingParameters(examples, wamProgram);
    }

    /**
     * After calling this method, the {@link #initialize()} method must be called, for the changes in the knowledge
     * base to take effect.
     * {@inheritDoc}
     *
     * @param knowledgeBase the {@link KnowledgeBase}
     */
    @Override
    public synchronized void setKnowledgeBase(KnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
        this.factsPlugin = buildFactsPlugin(aprOptions, useTernayIndex);
        addAtomsToKnowledgeBase(knowledgeBase);
    }

    /**
     * Builds the {@link FunctionsPlugin}. It is the internal representation of functions used by ProPPR.
     *
     * @param aprOptions the {@link APROptions}
     * @return the {@link FunctionsPlugin}
     */
    protected static FunctionsPlugin buildFunctionPlugin(APROptions aprOptions) {
        return new FunctionsPlugin(aprOptions, FUNCTIONS_PLUGIN_NAME);
    }

    /**
     * Adds the functional symbols from the function base into the plugin.
     *
     * @param functionBase the function base
     */
    protected void addFunctionsToKnowledgeBase(Knowledge<FunctionalSymbol> functionBase) {
        for (FunctionalSymbol functionalSymbol : functionBase) {
            functionsPlugin.addFunctionalSymbol(functionalSymbol);
        }
    }

    /**
     * After calling this method, the {@link #initialize()} method must be called, for the changes in the theory to
     * take effect.
     *
     * @param theory the {@link Theory}
     */
    @Override
    public synchronized void setTheory(Theory theory) {
        this.theory = theory;
        this.featureRules = new HashSet<>();
        this.program = ProPprUtils.compileTheory(theory, featureRules);
        if (this.grounder != null) { this.grounder.setProgram(program); }
        if (this.answerer != null) { this.answerer.setProgram(program); }
    }

    @Override
    public synchronized void saveParameters(File workingDirectory) {
        final File file = new File(workingDirectory, SAVED_PARAMETERS_FILE_NAME);
        logger.debug(SAVING_PARAMETERS_TO_FILE.toString(), file);
        ParamsFile.save(savedParamVector, file, null);
        try {
            FileIOUtils.writeIterableToFile(new File(workingDirectory, SAVED_FEATURE_THEORY), featureRules);
        } catch (IOException e) {
            logger.error(FileIOLog.ERROR_WRITING_FILE.toString(), e);
        }
    }

    @Override
    public synchronized void loadParameters(File workingDirectory) {
        final File file = new File(workingDirectory, SAVED_PARAMETERS_FILE_NAME);
        logger.debug(LOADING_PARAMETERS_FROM_FILE.toString(), file);
        ParamsFile paramsFile = new ParamsFile(file);
        currentParamVector = new SimpleParamVector<>(Dictionary.load(paramsFile, new ConcurrentHashMap<>()));
        saveTrainedParameters();
        loadFeatureClauses(workingDirectory);
    }

    /**
     * Loads the feature clauses from the saved file.
     *
     * @param workingDirectory the working directory
     */
    protected void loadFeatureClauses(File workingDirectory) {
        List<Clause> clauses = new ArrayList<>();
        FileIOUtils.readClausesToList(new File(workingDirectory, SAVED_FEATURE_THEORY), clauses);

        featureRules = new HashSet<>(clauses.size());
        for (Clause clause : clauses) {
            final Rule rule = ProPprUtils.clauseToRule(clause);
            featureRules.add(rule);
            rule.variabilize();
            program.append(new Instruction(Instruction.OP.comment, rule.toString()));
            program.insertLabel(getLabelForRule(rule));
            program.append(rule);
        }
        program.save();
    }

    /**
     * Builds the {@link FactsPlugin}. It is the internal representation of facts used by ProPPR.
     *
     * @param aprOptions     the {@link APROptions}
     * @param useTernayIndex if it should spend more memory to create an optimized index for predicates with arity
     *                       bigger than two
     * @return the {@link FactsPlugin}
     */
    protected static FactsPlugin buildFactsPlugin(APROptions aprOptions, boolean useTernayIndex) {
        FactsPlugin factsPlugin = new FactsPlugin(aprOptions, FACTS_PLUGIN_NAME, useTernayIndex);
        addTrueFalseFacts(factsPlugin);
        return factsPlugin;
    }

    /**
     * Copes the Answerer to infer example in parallel without retraining parameters.
     *
     * @return the copy of the answerer
     */
    protected InMemoryQueryAnswerer<P> buildAnswerer() {
        return new InMemoryQueryAnswerer<>(aprOptions, program, new
                WamPlugin[]{functionsPlugin, factsPlugin}, prover, normalizeAnswers, numberOfThreads, NO_MAX_SOLUTIONS);
    }

    @Override
    public void addAtomsToKnowledgeBase(Collection<? extends Atom> atoms) {
        for (Atom atom : atoms) {
            if (!atom.isGrounded()) {
                continue;
            }
            if (atom instanceof WeightedAtom) {
                factsPlugin.addWeightedFact(atom.getName(), ((WeightedAtom) atom).getWeight(), LanguageUtils
                        .toStringCollectionToArray(atom.getTerms()));
            } else {
                factsPlugin.addFact(atom.getName(), LanguageUtils.toStringCollectionToArray(atom.getTerms()));
            }
        }
    }

    /**
     * Adds the defaults facts to simulates the boolean true and false values in the {@link FactsPlugin}.
     *
     * @param factsPlugin the {@link FactsPlugin}
     */
    protected static void addTrueFalseFacts(FactsPlugin factsPlugin) {
        factsPlugin.addFact(LanguageUtils.TRUE_PREDICATE.getName(), LanguageUtils.TRUE_ARGUMENT);
        // the false argument should be true so the false(false) can not be proved.
        factsPlugin.addFact(LanguageUtils.FALSE_PREDICATE.getName(), LanguageUtils.TRUE_ARGUMENT);
    }

    /**
     * Creates a {@link Map} of the solutions to its correspondent {@link Example}.
     *
     * @param converter the {@link IterableConverter} of the {@link Example}s to {@link Query}is
     * @param answerer  the {@link InMemoryQueryAnswerer}
     * @return the {@link Map} of solutions
     */
    protected static <P extends ProofGraph> Map<Example, Map<Atom, Double>>
    inferExamples(IterableConverter<Example, Query> converter, InMemoryQueryAnswerer<P> answerer) {
        Map<Integer, Answer<P>> solutions = answerer.findSolutions(converter);
        Map<Example, Map<Atom, Double>> mapSolutions = new HashMap<>();
        Map<Atom, Double> atomMap;
        for (Map.Entry<Integer, Answer<P>> entry : solutions.entrySet()) {
            atomMap = new HashMap<>();
            for (Map.Entry<Query, Double> solution : entry.getValue().getSolutions().entrySet()) {
                atomMap.put(goalToAtom(solution.getKey().getRhs()[0]), solution.getValue());
            }
            mapSolutions.put(converter.getCountMap().get(entry.getKey()), atomMap);
        }

        return mapSolutions;
    }

    /**
     * Trains the logic system if the given examples and initial parameters.
     *
     * @param iterable    the examples
     * @param paramVector the initial parameters
     * @param grounder    the {@link InMemoryGrounder}
     * @return the trained parameters
     */
    protected ParamVector<String, ?> trainParameters(InferenceExampleIterable iterable,
                                                     ParamVector<String, ?> paramVector, InMemoryGrounder<P> grounder) {
        SymbolTable<String> symbolTable = new SimpleSymbolTable<>();
        Map<Integer, Ground<P>> map = grounder.groundExamples(iterable, symbolTable);
        return trainer.train(symbolTable,
                             map.values().stream().map(Ground::toString).collect(Collectors.toSet()),
                             new ArrayLearningGraphBuilder(), paramVector.copy(), numberOfTrainingEpochs);
    }

    /**
     * Method to infer the probability of the examples based on the {@link Theory}, {@link KnowledgeBase} and the
     * parameters from the logic engine. The parameters changes due the call of this
     * method will not be stored.
     * <p>
     * This method is useful to evaluate a theory revision without save the parameters.
     *
     * @param theory    a {@link Iterable} of {@link HornClause}s
     * @param converter the iterable to infer
     * @return a {@link Map} of the solutions to its correspondent {@link Example}s.
     */
    protected Map<Example, Map<Atom, Double>> inferWithTheoryExamples(Iterable<? extends HornClause> theory,
                                                                      IterableConverter<Example, Query> converter) {
        if (theory == null) { return null; }
        WamProgram wamProgram = ProPprUtils.compileTheory(theory);
        InMemoryQueryAnswerer<P> answerer = buildAnswerer(wamProgram);
        return inferExamples(converter, answerer);
    }

    /**
     * Copes the Answerer to infer example in parallel..
     *
     * @param parameters the {@link ParamVector}
     * @param program    the {@link WamProgram}
     * @return the copy of the answerer
     */
    protected InMemoryQueryAnswerer<P> buildAnswerer(ParamVector<String, ?> parameters, WamProgram program) {
        Prover<P> prover = this.prover.copy();
        InMemoryQueryAnswerer<P> answerer = new InMemoryQueryAnswerer<>(aprOptions, program, new
                WamPlugin[]{functionsPlugin, factsPlugin}, prover, normalizeAnswers, numberOfThreads, NO_MAX_SOLUTIONS);
        answerer.addParams(prover, parameters, squashingFunction);
        return answerer;
    }

    /**
     * Copes the Answerer to infer example in parallel without retraining parameters.
     *
     * @param program the {@link WamProgram}
     * @return the copy of the answerer
     */
    protected InMemoryQueryAnswerer<P> buildAnswerer(WamProgram program) {
        return new InMemoryQueryAnswerer<>(aprOptions, program, new
                WamPlugin[]{functionsPlugin, factsPlugin}, prover.copy(), normalizeAnswers, numberOfThreads,
                                           NO_MAX_SOLUTIONS);
    }

    /**
     * Method to infer the probability of the examples based on the {@link Theory} (appending new clauses),
     * {@link KnowledgeBase} and the parameters from the logic engine. The parameters changes due the call of this
     * method will not be stored.
     * <p>
     * This method is useful to evaluate a theory revision without save the parameters.
     *
     * @param appendClauses the {@link HornClause} to append
     * @param converter     the iterable to infer
     * @return a {@link Map} of the solutions to its correspondent {@link Example}s.
     */
    protected Map<Example, Map<Atom, Double>> inferExamplesAppendingClauses
    (Iterable<? extends HornClause> appendClauses, IterableConverter<Example, Query> converter) {
        if (appendClauses == null) { return null; }
        WamProgram wamProgram = ProPprUtils.compileTheory(theory);
        ProPprUtils.appendRuleToProgram(appendClauses, wamProgram);
        InMemoryQueryAnswerer<P> answerer = buildAnswerer(wamProgram);
        return inferExamples(converter, answerer);
    }

    /**
     * Builds a new {@link InMemoryGrounder} from the given {@link WamProgram}.
     *
     * @param program the {@link WamProgram}
     * @return the {@link InMemoryGrounder}
     */
    protected InMemoryGrounder<P> buildGrounder(WamProgram program) {
        return new InMemoryGrounder<>(numberOfThreads, Multithreading.DEFAULT_THROTTLE, aprOptions, prover.copy(),
                                      program, functionsPlugin, factsPlugin);
    }

    /**
     * Method to infer the probabilities of the examples in the iterator based on the {@link WamProgram} and
     * {@link KnowledgeBase}, training the parameters before inference. The changes due the call of this
     * method should not be stored.
     * <p>
     * This method is useful to evaluate a theory revision without save the parameters.
     *
     * @param wamProgram the {@link WamProgram}
     * @param examples   the iterable to infer
     * @return a {@link Map} of the solutions to its correspondent {@link Example}.
     */
    protected Map<Example, Map<Atom, Double>> inferExamplesTrainingParameters(Iterable<? extends Example> examples,
                                                                              WamProgram wamProgram) {
        InMemoryGrounder<P> grounder = buildGrounder(wamProgram);
        ParamVector<String, ?> parameters = trainParameters(new InferenceExampleIterable(examples), savedParamVector,
                                                            grounder);
        InMemoryQueryAnswerer<P> answerer = buildAnswerer(parameters, wamProgram);
        return inferExamples(new QueryIterable(examples), answerer);
    }

}
