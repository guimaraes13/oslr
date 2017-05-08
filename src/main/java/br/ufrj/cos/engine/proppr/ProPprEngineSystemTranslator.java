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
import br.ufrj.cos.engine.proppr.ground.Ground;
import br.ufrj.cos.engine.proppr.ground.InMemoryGrounder;
import br.ufrj.cos.engine.proppr.ground.InferenceExampleIterable;
import br.ufrj.cos.engine.proppr.query.answerer.Answer;
import br.ufrj.cos.engine.proppr.query.answerer.InMemoryQueryAnswerer;
import br.ufrj.cos.engine.proppr.query.answerer.QueryIterable;
import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.logic.*;
import br.ufrj.cos.util.IterableConverter;
import br.ufrj.cos.util.LanguageUtils;
import edu.cmu.ml.proppr.Trainer;
import edu.cmu.ml.proppr.examples.GroundedExample;
import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.graph.ArrayLearningGraphBuilder;
import edu.cmu.ml.proppr.graph.InferenceGraph;
import edu.cmu.ml.proppr.learn.SRW;
import edu.cmu.ml.proppr.learn.tools.SquashingFunction;
import edu.cmu.ml.proppr.prove.Prover;
import edu.cmu.ml.proppr.prove.wam.*;
import edu.cmu.ml.proppr.prove.wam.plugins.FactsPlugin;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.SRWOptions;
import edu.cmu.ml.proppr.util.SimpleSymbolTable;
import edu.cmu.ml.proppr.util.SymbolTable;
import edu.cmu.ml.proppr.util.math.ParamVector;
import edu.cmu.ml.proppr.util.math.SimpleParamVector;
import edu.cmu.ml.proppr.util.multithreading.Multithreading;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static edu.cmu.ml.proppr.Trainer.DEFAULT_CAPACITY;
import static edu.cmu.ml.proppr.Trainer.DEFAULT_LOAD;

/**
 * Translator to convert the system's syntax to ProPPR, and vice versa
 * <p>
 * Created on 25/04/17.
 *
 * @author Victor Guimarães
 */
public class ProPprEngineSystemTranslator<P extends ProofGraph> extends EngineSystemTranslator {

    /**
     * The character to separate the predicate name from the ist arity.
     */
    public static final String PREDICATE_ARITY_SEPARATOR = "/";

    protected final WamProgram program;
    protected final FactsPlugin factsPlugin;
    protected final InMemoryGrounder<P> grounder;
    protected final Trainer trainer;
    protected final int numberOfTrainingEpochs;
    protected final Prover<P> prover;
    protected final SquashingFunction<Goal> squashingFunction;

    protected ParamVector<String, ?> currentParamVector;
    protected ParamVector<String, ?> savedParamVector;
    protected SymbolTable<String> symbolTable;

    protected InMemoryQueryAnswerer<P> answerer;

    /**
     * Constructs the class if the minimum required parameters.
     *
     * @param knowledgeBase          the {@link KnowledgeBase}
     * @param theory                 the {@link Theory}
     * @param examples               the {@link Examples}
     * @param numberOfThreads        the number of threads this class is allowed to use
     * @param factsPluginName        the facts plugin's name
     * @param useTernayIndex         if is to use ternay index, makes an more efficient cache for predicates with arity
     * @param prover                 the {@link Prover}
     * @param aprOptions             the {@link APROptions}
     * @param srwOptions             the {@link SRWOptions}
     * @param numberOfTrainingEpochs the number of training epochs per training
     * @param normalize              if it is to normalize
     * @param numberOfSolutions      the number of top solutions to retrieve
     * @param squashingFunction      the {@link SquashingFunction}
     */
    public ProPprEngineSystemTranslator(KnowledgeBase knowledgeBase, Theory theory, Examples examples,
                                        int numberOfThreads, String factsPluginName, boolean useTernayIndex,
                                        Prover<P> prover, APROptions aprOptions, SRW srwOptions,
                                        int numberOfTrainingEpochs, boolean normalize, int numberOfSolutions,
                                        SquashingFunction<Goal> squashingFunction) {
        super(knowledgeBase, theory, examples);
        this.program = compileTheory(theory);
        this.numberOfTrainingEpochs = numberOfTrainingEpochs;
        this.factsPlugin = buildFactsPlugin(aprOptions, factsPluginName, useTernayIndex);
        this.prover = prover;
        this.grounder = new InMemoryGrounder<>(numberOfThreads, Multithreading.DEFAULT_THROTTLE, aprOptions, prover,
                                               program, factsPlugin);
        this.trainer = new Trainer(srwOptions, numberOfThreads, Multithreading.DEFAULT_THROTTLE);
        this.currentParamVector = new SimpleParamVector<>(new ConcurrentHashMap<String, Double>(DEFAULT_CAPACITY,
                                                                                                DEFAULT_LOAD,
                                                                                                numberOfThreads));
        this.symbolTable = new SimpleSymbolTable<>();
        this.answerer = new InMemoryQueryAnswerer<>(aprOptions, program, new WamPlugin[]{factsPlugin}, prover,
                                                    normalize, numberOfThreads, numberOfSolutions);
        this.squashingFunction = squashingFunction;
    }

    /**
     * Compiles a {@link Theory} into a {@link WamProgram}, the internal representation of ProPRR.
     *
     * @param theory the {@link Theory}
     * @return the {@link WamProgram}
     */
    protected static WamProgram compileTheory(Theory theory) {
        WamProgram wamProgram = new WamBaseProgram();
        Rule rule;
        for (HornClause hornClause : theory) {
            rule = clauseToRule(hornClause);
            rule.variabilize();
            wamProgram.append(new Instruction(Instruction.OP.comment, rule.toString()));
            wamProgram.insertLabel(getLabelForRule(rule));
            wamProgram.append(rule);
        }
        wamProgram.save();
        return wamProgram;
    }

    /**
     * Converts a {@link Clause}, from the system representation, to a {@link Rule} in ProPPR's representation.
     *
     * @param clause the {@link Clause}
     * @return the {@link Rule}
     */
    protected static Rule clauseToRule(Clause clause) {
        Goal lhs = null;                //head
        Goal[] rhs = new Goal[0];       //body
        Goal[] features = new Goal[0];  //features

        if (clause instanceof Atom) {
            lhs = atomToGoal((Atom) clause, new HashMap<>());
        } else if (clause instanceof HornClause) {
            HornClause hornClause = (HornClause) clause;
            Map<Term, Integer> variableMap = new HashMap<>();
            lhs = atomToGoal(hornClause.getHead(), variableMap);
            rhs = positiveLiteralsToGoals(hornClause.getBody(), variableMap);
            if (hornClause instanceof FeaturedClause) {
                FeaturedClause featuredClause = (FeaturedClause) hornClause;
                features = atomsToGoals(featuredClause.getFeatures(), variableMap);
            }
        }

        return new Rule(lhs, rhs, features, new Goal[0]);
    }

    /**
     * Gets the label for the {@link Rule}, so it can be proper added to the {@link WamProgram}.
     *
     * @param rule the {@link Rule}
     * @return the label
     */
    protected static String getLabelForRule(Rule rule) {
        return rule.getLhs().getFunctor() + PREDICATE_ARITY_SEPARATOR + rule.getLhs().getArity();
    }

    /**
     * Converts an {@link Iterable} of {@link Literal}s, from the system representation, to {@link Goal}s, from ProPPR's
     * representation; Using a variableMap to map variable names into int number. In addition, filters the negated
     * {@link Literal}s, converting only the non-negated ones. If two variable in a {@link Clause} represents the
     * same logic variable, they must be mapped to the same number, so, use the same variableMap to the whole
     * {@link Clause}.
     *
     * @param literals    {@link Iterable} of {@link Literal}s
     * @param variableMap the variable {@link Map}
     * @return the {@link Goal}s
     */
    protected static Goal[] positiveLiteralsToGoals(Iterable<? extends Literal> literals,
                                                    Map<Term, Integer> variableMap) {
        List<Goal> goals = new ArrayList<>();
        for (Literal literal : literals) {
            if (!literal.isNegated()) {
                goals.add(atomToGoal(literal, variableMap));
            }
        }

        return goals.toArray(new Goal[0]);
    }

    /**
     * Converts an {@link Iterable} of {@link Atom}s, from the system representation, to {@link Goal}s, from ProPPR's
     * representation; Using a variableMap to map variable names into int number. If two variable in a {@link Clause}
     * represents the same logic variable, they must be mapped to the same number, so, use the same variableMap to
     * the whole {@link Clause}.
     *
     * @param atoms       {@link Iterable} of {@link Atom}s
     * @param variableMap the variable {@link Map}
     * @return the {@link Goal}s
     */
    protected static Goal[] atomsToGoals(Iterable<? extends Atom> atoms, Map<Term, Integer> variableMap) {
        List<Goal> goals = new ArrayList<>();
        for (Atom atom : atoms) {
            goals.add(atomToGoal(atom, variableMap));
        }

        return goals.toArray(new Goal[0]);
    }

    /**
     * Converts an {@link Atom} to a {@link Query}.
     *
     * @param atom the {@link Atom}
     * @return the {@link Query}
     */
    public static Query atomToQuery(Atom atom) {
        return new Query(atomToGoal(atom, new HashMap<>()));
    }

    /**
     * Converts an {@link Atom}, from the system representation, to a {@link Goal}, from ProPPR's representation;
     * Using a variableMap to map variable names into int number. If two variable in a {@link Clause} represents the
     * same logic variable, they must be mapped to the same number, so, use the same variableMap to the whole
     * {@link Clause}.
     *
     * @param atom        the {@link Atom}
     * @param variableMap the variable {@link Map}
     * @return the {@link Goal}
     */
    public static Goal atomToGoal(Atom atom, Map<Term, Integer> variableMap) {
        return atomToGoal(atom.getName(), atom.getTerms(), variableMap);
    }

    /**
     * Converts an {@link Atom}, from the system representation, to a {@link Goal}, from ProPPR's representation;
     * Using a variableMap to map variable names into int number. If two variable in a {@link Clause} represents the
     * same logic variable, they must be mapped to the same number, so, use the same variableMap to the whole
     * {@link Clause}.
     *
     * @param name        the {@link Atom}'s name
     * @param terms       the {@link Atom}'s {@link Term}s
     * @param variableMap the variable {@link Map}
     * @return the {@link Goal}
     */
    public static Goal atomToGoal(String name, List<Term> terms, Map<Term, Integer> variableMap) {
        Argument[] arguments = new Argument[terms.size()];
        for (int i = 0; i < arguments.length; i++) {
            if (terms.get(i).isConstant()) {
                arguments[i] = new ConstantArgument(terms.get(i).getName());
            } else {
                if (variableMap == null) {
                    arguments[i] = new VariableArgument(i + 1);
                } else {
                    arguments[i] = new VariableArgument(variableMap.computeIfAbsent(terms.get(i), k ->
                            variableMap.size() + 1));
                }
            }
        }

        return new Goal(name, arguments);
    }

    /**
     * Builds the {@link FactsPlugin}. It is the internal representation of facts used by ProPPR.
     *
     * @param aprOptions      the {@link APROptions}
     * @param factsPluginName the plugin's name
     * @param useTernayIndex  if it should spend more memory to create an optimized index for predicates with arity
     *                        bigger than two
     * @return the {@link FactsPlugin}
     */
    protected FactsPlugin buildFactsPlugin(APROptions aprOptions, String factsPluginName, boolean useTernayIndex) {
        FactsPlugin factsPlugin = new FactsPlugin(aprOptions, factsPluginName, useTernayIndex);

        for (Atom atom : knowledgeBase) {
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

        return factsPlugin;
    }

    @Override
    public Set<Atom> groundRelevants(Collection<Term> terms) {
        Term clauseTerm;
        Set<InferenceExample> inferenceExamples = new HashSet<>();
        Map<String, Set<Atom>> coveredGoals = new HashMap<>();
        for (HornClause clause : theory) {
            if (isToSkipClause(clause.getHead(), coveredGoals)) { continue; }
            for (int i = 0; i < clause.getHead().getTerms().size(); i++) {
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
    protected boolean isToSkipClause(Atom goal, Map<String, Set<Atom>> coveredGoals) {
        String label = getLabelForAtom(goal);
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
        Query query = new Query(atomToGoal(goalAtom.getName(), terms, new HashMap<>()));

        return new InferenceExample(query, new Query[0], new Query[0]);
    }

    /**
     * Gets the grounded {@link Atom}s from the {@link InferenceExample}.
     *
     * @param examples the {@link InferenceExample} {@link Iterable}
     * @return the grounded {@link Atom}s
     */
    protected Set<Atom> getGroundedAtoms(Iterable<InferenceExample> examples) {
        Map<Integer, Ground<P>> groundMap = grounder.groundExamples(examples, symbolTable);
        Set<Atom> atoms = new HashSet<>();
        for (Ground<P> ground : groundMap.values()) {
            atoms.addAll(groundToAtoms(ground));
        }
        return atoms;
    }

    /**
     * Gets the label for the {@link Atom}.
     *
     * @param atom the {@link Atom}
     * @return the label
     */
    protected static String getLabelForAtom(Atom atom) {
        return atom.getName() + PREDICATE_ARITY_SEPARATOR + atom.getTerms().size();
    }

    /**
     * Converts the grounded of a {@link Ground} to {@link Atom}s.
     *
     * @param ground the {@link Ground}
     * @return the {@link Atom}s
     */
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
    public static Atom goalToAtom(Goal goal) {
        List<Term> terms = new ArrayList<>(goal.getArity());
        for (Argument argument : goal.getArgs()) {
            if (argument.isConstant()) {
                terms.add(new Constant(argument.getName()));
            } else {
                terms.add(new Variable(argument.getName()));
            }
        }
        return new Atom(goal.getFunctor(), terms);
    }

    @Override
    public Set<Atom> groundExamples(Example... examples) {
        return getGroundedAtoms(new InferenceExampleIterable(examples));
    }

    @Override
    public void trainParameters(Example... examples) {
        trainParameters(new InferenceExampleIterable(examples), savedParamVector);
    }

    @Override
    public void trainParameters(Iterable<? extends Example> examples) {
        trainParameters(new InferenceExampleIterable(examples), savedParamVector);
    }

    @Override
    public void saveTrainedParameters() {
        savedParamVector = currentParamVector;
        answerer.addParams(prover, savedParamVector, squashingFunction);
    }

    @Override
    public Map<Example, Map<Atom, Double>> inferExamples(Example... examples) {
        IterableConverter<Example, Query> converter = new QueryIterable(examples);
        return getWeightedSolutions(converter);
    }

    @Override
    public Map<Example, Map<Atom, Double>> inferExamples(Iterable<? extends Example> examples) {
        IterableConverter<Example, Query> converter = new QueryIterable(examples);
        return getWeightedSolutions(converter);
    }

    @Override
    public Map<Example, Map<Atom, Double>> inferExampleWithLastParameters(Iterable<? extends Example> examples) {
        answerer.addParams(prover, currentParamVector, squashingFunction);
        Map<Example, Map<Atom, Double>> inferExample = inferExamples(examples);
        answerer.addParams(prover, savedParamVector, squashingFunction);
        return inferExample;
    }

    /**
     * Creates a {@link Map} of the solutions to its correspondent {@link Example}.
     *
     * @param converter the {@link IterableConverter} of the {@link Example}s to {@link Query}is
     * @return the {@link Map} of solutions
     */
    protected Map<Example, Map<Atom, Double>> getWeightedSolutions(IterableConverter<Example, Query> converter) {
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
     */
    protected void trainParameters(InferenceExampleIterable iterable, ParamVector<String, ?> paramVector) {
        Map<Integer, Ground<P>> map = grounder.groundExamples(iterable, symbolTable);
        currentParamVector = trainer.train(symbolTable,
                                           map.values().stream().map(Ground::toString).collect(Collectors.toSet()),
                                           new ArrayLearningGraphBuilder(), paramVector, numberOfTrainingEpochs);
    }

}
