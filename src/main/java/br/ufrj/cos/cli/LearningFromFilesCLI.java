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

package br.ufrj.cos.cli;

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.engine.EngineSystemTranslator;
import br.ufrj.cos.engine.proppr.ProPprEngineSystemTranslator;
import br.ufrj.cos.knowledge.KnowledgeException;
import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.example.ProPprExample;
import br.ufrj.cos.knowledge.filter.ClausePredicate;
import br.ufrj.cos.knowledge.filter.GroundedFactPredicate;
import br.ufrj.cos.knowledge.manager.IncomingExampleManager;
import br.ufrj.cos.knowledge.manager.ReviseAllIncomingExample;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.TheoryEvaluator;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.evaluation.metric.logic.AccuracyMetric;
import br.ufrj.cos.knowledge.theory.evaluation.metric.logic.F1ScoreMetric;
import br.ufrj.cos.knowledge.theory.evaluation.metric.logic.PrecisionMetric;
import br.ufrj.cos.knowledge.theory.evaluation.metric.logic.RecallMetric;
import br.ufrj.cos.knowledge.theory.evaluation.metric.probabilistic.LikelihoodMetric;
import br.ufrj.cos.knowledge.theory.evaluation.metric.probabilistic.LogLikelihoodMetric;
import br.ufrj.cos.knowledge.theory.evaluation.metric.probabilistic.RocCurveMetric;
import br.ufrj.cos.knowledge.theory.manager.TheoryRevisionManager;
import br.ufrj.cos.knowledge.theory.manager.revision.RevisionManager;
import br.ufrj.cos.knowledge.theory.manager.revision.RevisionOperatorEvaluator;
import br.ufrj.cos.knowledge.theory.manager.revision.RevisionOperatorSelector;
import br.ufrj.cos.knowledge.theory.manager.revision.SelectFirstRevisionOperator;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.generalization.BottomClauseBoundedRule;
import br.ufrj.cos.knowledge.theory.manager.revision.point.IndependentSampleSelector;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.parser.example.ExampleParser;
import br.ufrj.cos.logic.parser.knowledge.KnowledgeParser;
import br.ufrj.cos.logic.parser.knowledge.ParseException;
import br.ufrj.cos.util.*;
import br.ufrj.cos.util.time.TimeUtils;
import com.esotericsoftware.yamlbeans.YamlException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.*;

import static br.ufrj.cos.util.log.FileIOLog.ERROR_READING_FILE;
import static br.ufrj.cos.util.log.FileIOLog.READING_INPUT_FILES;
import static br.ufrj.cos.util.log.GeneralLog.*;
import static br.ufrj.cos.util.log.InferenceLog.EVALUATION_UNDER_METRIC;
import static br.ufrj.cos.util.log.ParsingLog.ERROR_READING_INPUT_FILES;
import static br.ufrj.cos.util.log.PreRevisionLog.PASSING_EXAMPLE_OF_TOTAL_REVISION;
import static br.ufrj.cos.util.log.SystemLog.*;

/**
 * A Command Line Interface which allows experiments of learning from files.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings({"CanBeFinal"})
public class LearningFromFilesCLI extends CommandLineInterface {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    /**
     * The default yaml configuration file.
     */
    public static final String DEFAULT_YAML_CONFIGURATION_FILE = "default.yml";
    /**
     * The name of the saved theory file.
     */
    public static final String THEORY_FILE_NAME = "theory.pl";
    /**
     * The knowledge base collection class name.
     */
    public String knowledgeBaseCollectionClassName = ArrayList.class.getName();
    /**
     * The knowledge base predicate name.
     */
    public String knowledgeBasePredicateClassName = GroundedFactPredicate.class.getName();
    /**
     * The most generic {@link Atom} subclass name allowed in the knowledge base.
     */
    public String knowledgeBaseAncestralClassName = Atom.class.getName();
    /**
     * The theory collection class name.
     */
    public String theoryCollectionClassName = ArrayList.class.getName();
    /**
     * The theory predicate class name.
     */
    public String theoryPredicateClassName = null;
    /**
     * The theory predicate class.
     */
    public Class<? extends ClausePredicate> theoryPredicateClass = null;
    /**
     * The most generic {@link HornClause} subclass name allowed in the theory.
     */
    public String theoryBaseAncestralClassName = null;
    /**
     * The most generic {@link HornClause} subclass allowed in the theory.
     */
    public Class<? extends HornClause> theoryBaseAncestralClass = null;
    /**
     * Input knowledge base files.
     */
    public String[] knowledgeBaseFilePaths;
    /**
     * Input theory files.
     */
    public String[] theoryFilePaths;
    /**
     * Input example files.
     */
    public String[] exampleFilePaths;
    /**
     * The evaluation metrics for the {@link TheoryEvaluator}.
     */
    @SuppressWarnings("unused")
    public TheoryMetric[] theoryMetrics;
    /**
     * The {@link RevisionOperatorEvaluator}s.
     */
    @SuppressWarnings("unused")
    public RevisionOperatorEvaluator[] revisionOperatorEvaluators;
    /**
     * The {@link RevisionOperatorSelector}.
     */
    public RevisionOperatorSelector revisionOperatorSelector;
    /**
     * The {@link RevisionManager}.
     */
    public RevisionManager revisionManager;
    /**
     * The {@link IncomingExampleManager}.
     */
    public IncomingExampleManager incomingExampleManager;
    /**
     * The {@link TheoryEvaluator}.
     */
    public TheoryEvaluator theoryEvaluator;
    /**
     * The {@link TheoryRevisionManager}.
     */
    public TheoryRevisionManager theoryRevisionManager;
    /**
     * The engine system.
     */
    public EngineSystemTranslator engineSystemTranslator;
    /**
     * If the system will be executed in parallel and thread access control will be necessary.
     * <p>
     * If it is {@code true}, thread local instances of the {@link EngineSystemTranslator} will be passed on methods
     * that evaluates examples retraining parameters or changing the {@link Theory}.
     */
    public boolean controlConcurrence = false;
    /**
     * If is to load pre trained parameters.
     * <p>
     * If this model has been already trained, it is possible that it has saved some parameter files.
     * <br>
     * If this option is {@code true}, this files will be loaded during the current training.
     * <br>
     * If it is {@code false}, the files will not be loaded (and will possible be overwritten).
     */
    public boolean loadedPreTrainedParameters = false;
    /**
     * The knowledge base collection class.
     */
    protected Class<? extends Collection> knowledgeBaseCollectionClass;
    /**
     * The knowledge base predicate.
     */
    protected Class<? extends ClausePredicate> knowledgeBasePredicateClass;

    //Processing fields
    /**
     * The most generic {@link Atom} subclass allowed in the knowledge base.
     */
    protected Class<? extends Atom> knowledgeBaseAncestralClass;
    /**
     * The theory collection class.
     */
    protected Class<? extends Collection> theoryCollectionClass;
    /**
     * Knowledge base representation.
     */
    protected KnowledgeBase knowledgeBase;
    /**
     * Theory representation.
     */
    protected Theory theory;
    /**
     * Examples representation.
     */
    protected Examples examples;
    /**
     * The learning system.
     */
    protected LearningSystem learningSystem;

    /**
     * The integer number format.
     */
    protected NumberFormat integerFormat;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Locale.setDefault(new Locale(DEFAULT_LANGUAGE, DEFAULT_COUNTRY));
        try {
            CommandLineInterface main = new LearningFromFilesCLI();
            main = main.parseOptions(args);
            run(main, args);
        } catch (Exception e) {
            logger.error(ERROR_MAIN_PROGRAM, e);
        } finally {
            logger.fatal(PROGRAM_END);
        }
    }

    /**
     * Builds the {@link Examples} from the input files.
     *
     * @param exampleFilePaths the examples paths
     * @return the examples
     * @throws InstantiationException if an error occurs when instantiating a new set
     * @throws IllegalAccessException if an error occurs when instantiating a new set
     * @throws FileNotFoundException  if a file does not exists
     */
    protected static Examples buildExampleSet(String[] exampleFilePaths) throws InstantiationException,
            IllegalAccessException, FileNotFoundException {
        List<AtomExample> atomExamples = new ArrayList<>();
        List<ProPprExample> proPprExamples = new ArrayList<>();
        logger.trace(READING_INPUT_FILES);
        File[] files = FileIOUtils.readPathsToFiles(exampleFilePaths, CommandLineOptions.EXAMPLES.getOptionName());
        for (File file : files) {
            readExamplesToLists(file, atomExamples, proPprExamples);
        }
        logger.info(EXAMPLES_SIZE.toString(), atomExamples.size() + proPprExamples.size());

        return new Examples(proPprExamples, atomExamples);
    }

    /**
     * Parses the {@link File}'s iterator and appends they to the correspondent {@link List}.
     *
     * @param file           the {@link File} to parse
     * @param atomExamples   the {@link List} to the ProbLog like iterator
     * @param proPprExamples the {@link List} to the ProPPR like iterator
     */
    protected static void readExamplesToLists(File file,
                                              List<AtomExample> atomExamples,
                                              List<ProPprExample> proPprExamples) {
        try {
            BufferedReader reader;
            ExampleParser parser;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), FileIOUtils
                    .DEFAULT_INPUT_ENCODE));
            parser = new ExampleParser(reader);
            parser.parseExamplesAppend(atomExamples, proPprExamples);
        } catch (UnsupportedEncodingException | FileNotFoundException | br.ufrj.cos.logic.parser.example
                .ParseException e) {
            logger.error(ERROR_READING_FILE.toString(), e);
        }
    }

    @Override
    public void run() {
        try {
            long begin = TimeUtils.getNanoTime();
            build();
            reviseExamples();
            saveParameters();
            printMetrics();
            long end = TimeUtils.getNanoTime();
            logger.warn(TOTAL_PROGRAM_TIME.toString(), TimeUtils.formatNanoDifference(begin, end));
        } catch (ReflectiveOperationException e) {
            logger.error(ERROR_READING_INPUT_FILES, e);
        } catch (InitializationException e) {
            logger.error(ERROR_INITIALIZING_COMPONENTS, e);
        } catch (IOException e) {
            logger.error(ERROR_READING_CONFIGURATION_FILE, e);
        }
    }

    /**
     * Builds the default {@link RevisionOperatorEvaluator}s.
     *
     * @return the default {@link RevisionOperatorEvaluator}s
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected static List<RevisionOperatorEvaluator> defaultRevisionOperator() throws InitializationException {
        List<RevisionOperatorEvaluator> operatorEvaluator = new ArrayList<>();
        BottomClauseBoundedRule bottomClause = new BottomClauseBoundedRule();
        bottomClause.setTheoryMetric(new F1ScoreMetric());
        operatorEvaluator.add(new RevisionOperatorEvaluator(bottomClause));
        return operatorEvaluator;
    }

    /**
     * Builds the default {@link TheoryMetric}s.
     *
     * @return the default {@link TheoryMetric}s
     */
    @SuppressWarnings("OverlyCoupledMethod")
    protected static List<TheoryMetric> defaultTheoryMetrics() {
        List<TheoryMetric> metrics = new ArrayList<>();
        metrics.add(new AccuracyMetric());
        metrics.add(new PrecisionMetric());
        metrics.add(new RecallMetric());
        metrics.add(new F1ScoreMetric());

        metrics.add(new LikelihoodMetric());
        metrics.add(new LogLikelihoodMetric());
        metrics.add(new RocCurveMetric());
        return metrics;
    }

    /**
     * Parses the {@link File}'s {@link Clause}s and appends they to the {@link List}.
     *
     * @param file    the {@link File} to parse
     * @param clauses the {@link List} to append to
     */
    protected static void readClausesToList(File file, List<Clause> clauses) {
        try {
            BufferedReader reader;
            KnowledgeParser parser;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), FileIOUtils
                    .DEFAULT_INPUT_ENCODE));
            parser = new KnowledgeParser(reader);
            parser.parseKnowledgeAppend(clauses);
        } catch (UnsupportedEncodingException | FileNotFoundException | ParseException e) {
            logger.error(ERROR_READING_FILE.toString(), e);
        }
    }

    @Override
    public void initialize() throws InitializationException {
        integerFormat = NumberFormat.getIntegerInstance();
        instantiateClasses();
        saveConfigurations();
    }

    /**
     * Instantiates the necessary classes objects.
     *
     * @throws InitializationException if some thing goes wrong
     */
    @SuppressWarnings("unchecked")
    public void instantiateClasses() throws InitializationException {
        try {
            knowledgeBaseCollectionClass = (Class<? extends Collection>)
                    Class.forName(knowledgeBaseCollectionClassName);
            knowledgeBasePredicateClass = (Class<? extends ClausePredicate>)
                    Class.forName(knowledgeBasePredicateClassName);
            knowledgeBaseAncestralClass = (Class<? extends Atom>) Class.forName(knowledgeBaseAncestralClassName);
            theoryCollectionClass = (Class<? extends Collection>) Class.forName(theoryCollectionClassName);
            if (theoryPredicateClassName != null && !theoryPredicateClassName.isEmpty()) {
                theoryPredicateClass = (Class<? extends ClausePredicate>) Class.forName(theoryPredicateClassName);
            }
            if (theoryBaseAncestralClassName != null && !theoryBaseAncestralClassName.isEmpty()) {
                theoryBaseAncestralClass = (Class<? extends HornClause>) Class.forName(theoryBaseAncestralClassName);
            }
        } catch (ClassNotFoundException e) {
            throw new InitializationException(ExceptionMessages.ERROR_GETTING_CLASS_BY_NAME.toString(), e);
        }
    }

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        if (options == null) { options = new Options(); }

        options.addOption(CommandLineOptions.KNOWLEDGE_BASE.getOption());
        options.addOption(CommandLineOptions.THEORY.getOption());
        options.addOption(CommandLineOptions.EXAMPLES.getOption());
        options.addOption(CommandLineOptions.YAML.getOption());
        options.addOption(CommandLineOptions.OUTPUT_DIRECTORY.getOption());
    }

    @Override
    public CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        try {
            super.parseOptions(commandLine);
            LearningFromFilesCLI cli = readYamlFile(commandLine, this.getClass(), DEFAULT_YAML_CONFIGURATION_FILE);
            cli.knowledgeBaseFilePaths = getFilesFromOption(commandLine,
                                                            CommandLineOptions.KNOWLEDGE_BASE.getOptionName());
            cli.theoryFilePaths = getFilesFromOption(commandLine, CommandLineOptions.THEORY.getOptionName());
            cli.exampleFilePaths = getFilesFromOption(commandLine, CommandLineOptions.EXAMPLES.getOptionName());
            cli.outputDirectoryPath = commandLine.getOptionValue(CommandLineOptions.OUTPUT_DIRECTORY.getOptionName());
            return cli;
        } catch (FileNotFoundException | YamlException e) {
            throw new CommandLineInterrogationException(e);
        }
    }

    /**
     * Logs the metrics.
     */
    protected void printMetrics() {
        List<Map.Entry<TheoryMetric, Double>> evaluations = new ArrayList<>(learningSystem.evaluate(examples)
                                                                                    .entrySet());
        evaluations.sort(Comparator.comparing(o -> o.getKey().toString()));
        for (Map.Entry<TheoryMetric, Double> entry : evaluations) {
            logger.warn(EVALUATION_UNDER_METRIC.toString(), entry.getKey(), entry.getValue());
        }
    }

    /**
     * Builds this class and all its properties.
     *
     * @throws IllegalAccessException  if an error occurs when instantiating a new object by reflection
     * @throws IOException             if an error occurs with the file
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void build() throws ReflectiveOperationException, IOException, InitializationException {
        buildKnowledgeBase();
        buildTheory();
        buildExamples();
        buildEngineSystemTranslator();
        buildLearningSystem();
    }

    /**
     * Builds the examples
     *
     * @throws InstantiationException if an error occurs when instantiating a new set
     * @throws IllegalAccessException if an error occurs when instantiating a new set
     * @throws FileNotFoundException  if a file does not exists
     */
    protected void buildExamples() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        examples = buildExampleSet(exampleFilePaths);
    }

    /**
     * Builds the {@link LearningSystem}.
     *
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void buildLearningSystem() throws InitializationException {
        logger.info(BUILDING_LEARNING_SYSTEM.toString(), LearningSystem.class.getSimpleName());
        learningSystem = new LearningSystem(knowledgeBase, theory, new Examples(), engineSystemTranslator);
        learningSystem.concurrent = controlConcurrence;

        List<TheoryMetric> theoryMetrics = buildMetrics();
        buildOperatorSelector();

        buildIncomingExampleManager();
        buildTheoryEvaluator(theoryMetrics);
        buildTheoryRevisionManager();
        learningSystem.initialize();
    }

    /**
     * Builds the {@link EngineSystemTranslator}.
     *
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void buildEngineSystemTranslator() throws InitializationException {
        if (engineSystemTranslator == null) { engineSystemTranslator = new ProPprEngineSystemTranslator<>(); }
        logger.info(BUILDING_ENGINE_SYSTEM_TRANSLATOR.toString(),
                    engineSystemTranslator.getClass().getSimpleName());

        engineSystemTranslator.setKnowledgeBase(knowledgeBase);
        engineSystemTranslator.setTheory(theory);
        engineSystemTranslator.initialize();
        if (loadedPreTrainedParameters) { engineSystemTranslator.loadParameters(outputDirectory); }
    }

    /**
     * Call the method to revise the examples
     */
    protected void reviseExamples() {
        //IMPROVE: delegate this function to the ExampleStream
        int count = 1;
        final int size = examples.size();
        for (Example example : examples) {
            logger.trace(PASSING_EXAMPLE_OF_TOTAL_REVISION.toString(), integerFormat.format(count), integerFormat
                    .format(size));
            learningSystem.incomingExampleManager.incomingExamples(example);
            count++;
        }
    }

    /**
     * Saves the parameters to files.
     *
     * @throws IOException if an error occurs with the file
     */
    protected void saveParameters() throws IOException {
        String theoryContent = LanguageUtils.theoryToString(learningSystem.getTheory());
        File theoryFile = new File(outputDirectory, THEORY_FILE_NAME);
        FileIOUtils.writeStringToFile(theoryContent, theoryFile);
        logger.info(THEORY_FILE.toString(), theoryFile.getAbsolutePath(), theoryContent);
        learningSystem.saveParameters(outputDirectory);
    }

    /**
     * Initializes the {@link TheoryRevisionManager}.
     *
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void buildTheoryRevisionManager() throws InitializationException {
        if (theoryRevisionManager == null) {
            theoryRevisionManager = new TheoryRevisionManager();
        }
        buildRevisionManager();

        theoryRevisionManager.setRevisionManager(revisionManager);
        learningSystem.theoryRevisionManager = theoryRevisionManager;
    }

    /**
     * Initializes the {@link TheoryEvaluator}.
     *
     * @param theoryMetrics the {@link TheoryMetric}s
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void buildTheoryEvaluator(List<TheoryMetric> theoryMetrics) throws InitializationException {
        if (theoryEvaluator == null) {
            theoryEvaluator = new TheoryEvaluator();
        }
        theoryEvaluator.setTheoryMetrics(theoryMetrics);
        learningSystem.theoryEvaluator = theoryEvaluator;
    }

    /**
     * Initializes the {@link IncomingExampleManager}.
     *
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void buildIncomingExampleManager() throws InitializationException {
        if (incomingExampleManager == null) {
            incomingExampleManager = new ReviseAllIncomingExample(learningSystem, new IndependentSampleSelector());
        } else {
            incomingExampleManager.setLearningSystem(learningSystem);
        }
        learningSystem.incomingExampleManager = incomingExampleManager;
    }

    /**
     * Initializes the {@link RevisionManager}.
     *
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void buildRevisionManager() throws InitializationException {
        if (revisionManager == null) {
            revisionManager = new RevisionManager();
        }
        revisionManager.setOperatorSelector(revisionOperatorSelector);
    }

    /**
     * Initializes the {@link RevisionOperatorSelector}.
     *
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void buildOperatorSelector() throws InitializationException {
        if (revisionOperatorSelector == null) {
            revisionOperatorSelector = new SelectFirstRevisionOperator();
        }
        if (!revisionOperatorSelector.isOperatorEvaluatorsSetted()) {
            revisionOperatorSelector.setOperatorEvaluators(buildOperators());
        }
    }

    /**
     * Initializes the {@link RevisionOperatorEvaluator}s.
     *
     * @return the {@link RevisionOperatorEvaluator}s
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected List<RevisionOperatorEvaluator> buildOperators() throws InitializationException {
        List<RevisionOperatorEvaluator> operatorEvaluator;
        if (revisionOperatorEvaluators == null || revisionOperatorEvaluators.length == 0) {
            operatorEvaluator = defaultRevisionOperator();
        } else {
            operatorEvaluator = Arrays.asList(revisionOperatorEvaluators);
        }
        for (RevisionOperatorEvaluator operator : operatorEvaluator) {
            operator.setLearningSystem(learningSystem);
        }
        return operatorEvaluator;
    }

    /**
     * Initializes the {@link TheoryMetric}s.
     *
     * @return the {@link TheoryMetric}s
     */
    protected List<TheoryMetric> buildMetrics() {
        return (theoryMetrics == null || theoryMetrics.length == 0 ? defaultTheoryMetrics() :
                Arrays.asList(theoryMetrics));
    }

    /**
     * Reads the input {@link File}s to a {@link List} of {@link Clause}s.
     *
     * @param inputFiles the input {@link File}s
     * @return the {@link List} of {@link Clause}s
     */
    protected static List<Clause> readInputKnowledge(File[] inputFiles) {
        List<Clause> clauses = new ArrayList<>();
        logger.trace(READING_INPUT_FILES);
        for (File file : inputFiles) {
            readClausesToList(file, clauses);
        }
        logger.debug(READ_CLAUSE_SIZE.toString(), clauses.size());
        return clauses;
    }

    /**
     * Builds the {@link KnowledgeBase} from the input files.
     *
     * @throws IllegalAccessException if an error occurs when instantiating a new object by reflection
     * @throws InstantiationException if an error occurs when instantiating a new object by reflection
     * @throws FileNotFoundException  if a file does not exists
     */
    protected void buildKnowledgeBase() throws IllegalAccessException, InstantiationException, FileNotFoundException {
        List<Clause> clauses = readInputKnowledge(FileIOUtils.readPathsToFiles(knowledgeBaseFilePaths,
                                                                               CommandLineOptions.KNOWLEDGE_BASE
                                                                                       .getOptionName()));

        ClausePredicate predicate = knowledgeBasePredicateClass.newInstance();
        logger.debug(CREATING_KNOWLEDGE_BASE_WITH_PREDICATE.toString(), predicate);
        knowledgeBase = new KnowledgeBase(knowledgeBaseCollectionClass.newInstance(), predicate);

        knowledgeBase.addAll(clauses, knowledgeBaseAncestralClass);
        logger.info(KNOWLEDGE_BASE_SIZE.toString(), knowledgeBase.size());
    }

    /**
     * Builds the {@link Theory} from the input files.
     *
     * @throws NoSuchMethodException     if an error occurs when instantiating a new object by reflection
     * @throws IllegalAccessException    if an error occurs when instantiating a new object by reflection
     * @throws InvocationTargetException if an error occurs when instantiating a new object by reflection
     * @throws InstantiationException    if an error occurs when instantiating a new object by reflection
     * @throws FileNotFoundException     if a file does not exists
     */
    protected void buildTheory() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException, FileNotFoundException {
        List<Clause> clauses = readInputKnowledge(FileIOUtils.readPathsToFiles(theoryFilePaths,
                                                                               CommandLineOptions.THEORY
                                                                                         .getOptionName()));

        ClausePredicate predicate = null;
        if (theoryPredicateClass != null && theoryBaseAncestralClass != null) {
            predicate = theoryPredicateClass.getConstructor(theoryBaseAncestralClass.getClass())
                    .newInstance(theoryBaseAncestralClass);
            logger.debug(CREATING_THEORY_WITH_PREDICATE.toString(), predicate);
        }

        if (theoryBaseAncestralClass == null) {
            theoryBaseAncestralClass = HornClause.class;
        }

        theory = new Theory(theoryCollectionClass.newInstance(), predicate);
        theory.addAll(clauses, theoryBaseAncestralClass);
        logger.info(THEORY_SIZE.toString(), theory.size());
    }

    /**
     * Sets the {@link EngineSystemTranslator} if it is not yet set. If it is already set, throws an error.
     *
     * @param engineSystemTranslator the {@link EngineSystemTranslator}
     * @throws KnowledgeException if the {@link EngineSystemTranslator} is already set
     */
    public void setEngineSystemTranslator(EngineSystemTranslator engineSystemTranslator) throws KnowledgeException {
        if (isEngineSystemTranslatorSet()) {
            throw new KnowledgeException(
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
                                                 EngineSystemTranslator.class.getSimpleName()));
        }
        this.engineSystemTranslator = engineSystemTranslator;
    }

    /**
     * Checks if the {@link EngineSystemTranslator} is already set.
     *
     * @return {@code true} if it is, otherwise {@code false}
     */
    public boolean isEngineSystemTranslatorSet() {
        return this.engineSystemTranslator != null;
    }

    @Override
    public String toString() {
        String description = "\t" +
                "Settings:" +
                "\n" +
                "\t" +
                "Knowledge base files:\t" +
                Arrays.deepToString(knowledgeBaseFilePaths) +
                "\n" +
                "\t" +
                "Theory files:\t\t\t" +
                Arrays.deepToString(theoryFilePaths) +
                "\n" +
                "\t" +
                "Example files:\t\t\t" +
                Arrays.deepToString(exampleFilePaths);

        return description.trim();
    }

}