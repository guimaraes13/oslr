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
import br.ufrj.cos.knowledge.theory.manager.TheoryRevisionManager;
import br.ufrj.cos.knowledge.theory.manager.revision.RevisionManager;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.RevisionOperatorEvaluator;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.RevisionOperatorSelector;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.SelectFirstRevisionOperator;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.generalization.BottomClauseBoundedRule;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.parser.example.ExampleParser;
import br.ufrj.cos.logic.parser.knowledge.KnowledgeParser;
import br.ufrj.cos.logic.parser.knowledge.ParseException;
import br.ufrj.cos.util.*;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A Command Line Interface which allows experiments of learning from files.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("CanBeFinal")
public class LearningFromFilesCLI extends CommandLineInterface {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * The default yaml configuration file.
     */
    public static final String DEFAULT_YAML_CONFIGURATION_FILE = "src/main/resources/default.yml";

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
    public File[] knowledgeBaseFiles;
    /**
     * Input theory files.
     */
    public File[] theoryFiles;
    /**
     * Input example files.
     */
    public File[] exampleFiles;
    /**
     * The evaluation metrics for the {@link TheoryEvaluator}.
     */
    public TheoryMetric[] theoryMetrics;

    //Input fields
    /**
     * The {@link RevisionOperatorEvaluator}s.
     */
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
     * The knowledge base collection class.
     */
    protected Class<? extends Collection> knowledgeBaseCollectionClass;
    /**
     * The knowledge base predicate.
     */
    protected Class<? extends ClausePredicate> knowledgeBasePredicateClass;
    /**
     * The most generic {@link Atom} subclass allowed in the knowledge base.
     */
    protected Class<? extends Atom> knowledgeBaseAncestralClass;
    /**
     * The theory collection class.
     */
    protected Class<? extends Collection> theoryCollectionClass;

    //Processing fields
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
     * Parses the {@link File}'s {@link Clause}s and appends they to the {@link List}.
     *
     * @param file    the {@link File} to parse
     * @param clauses the {@link List} to append to
     */
    protected static void readClausesToList(File file, List<Clause> clauses) {
        try {
            BufferedReader reader;
            KnowledgeParser parser;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), LanguageUtils
                    .DEFAULT_INPUT_ENCODE));
            parser = new KnowledgeParser(reader);
            parser.parseKnowledgeAppend(clauses);
        } catch (UnsupportedEncodingException | FileNotFoundException | ParseException e) {
            logger.error(LogMessages.ERROR_READING_FILE.toString(), file.getAbsoluteFile(), e);
        }
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
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), LanguageUtils
                    .DEFAULT_INPUT_ENCODE));
            parser = new ExampleParser(reader);
            parser.parseExamplesAppend(atomExamples, proPprExamples);
        } catch (UnsupportedEncodingException | FileNotFoundException | br.ufrj.cos.logic.parser.example
                .ParseException e) {
            logger.error(LogMessages.ERROR_READING_FILE.toString(), file.getAbsoluteFile(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() throws InitializationException {
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
        if (options == null) { options = new Options(); }

        options.addOption(CommandLineOptions.HELP.getOption());
        options.addOption(CommandLineOptions.KNOWLEDGE_BASE.getOption());
        options.addOption(CommandLineOptions.THEORY.getOption());
        options.addOption(CommandLineOptions.EXAMPLES.getOption());
        options.addOption(CommandLineOptions.YAML.getOption());
    }

    @Override
    public CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        try {
            if (commandLine.hasOption(CommandLineOptions.HELP.getOptionName())) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(this.getClass().getSimpleName(), options, true);
                System.exit(0);
            }
            LearningFromFilesCLI cli = readYamlFile(commandLine);
            cli.knowledgeBaseFiles = getFilesFromOption(commandLine, CommandLineOptions.KNOWLEDGE_BASE.getOptionName());
            cli.theoryFiles = getFilesFromOption(commandLine, CommandLineOptions.THEORY.getOptionName());
            cli.exampleFiles = getFilesFromOption(commandLine, CommandLineOptions.EXAMPLES.getOptionName());
            return cli;
        } catch (Exception e) {
            throw new CommandLineInterrogationException(e);
        }
    }

    /**
     * Reads the yaml configuration file and returns a version of this class from it.
     *
     * @param commandLine the {@link CommandLine}
     * @return the read {@link LearningFromFilesCLI}
     * @throws FileNotFoundException if the file does not exists
     * @throws YamlException         if an error occurs when reading the yaml
     */
    protected LearningFromFilesCLI readYamlFile(CommandLine commandLine) throws FileNotFoundException, YamlException {
        File yamlFile;
        if (commandLine.hasOption(CommandLineOptions.YAML.getOptionName())) {
            yamlFile = new File(commandLine.getOptionValue(CommandLineOptions.YAML.getOptionName()));
        } else {
            yamlFile = new File(DEFAULT_YAML_CONFIGURATION_FILE);
        }
        YamlReader reader = new YamlReader(new FileReader(yamlFile));
        LearningFromFilesCLI cli = reader.read(LearningFromFilesCLI.class);
        cli.configurationFilePath = yamlFile.getAbsolutePath();
        return cli;
    }

    /**
     * Gets the correspondent {@link File}s from the parsed {@link CommandLine}.
     *
     * @param commandLine the parsed command line
     * @param optionName  the {@link Option} to get the parsed {@link String}s.
     * @return the {@link File}s
     * @throws FileNotFoundException if a file does not exists
     */
    protected File[] getFilesFromOption(CommandLine commandLine, String optionName) throws FileNotFoundException {
        if (commandLine.hasOption(optionName)) {
            return readPathsToFiles(commandLine.getOptionValues(optionName), options.getOption(optionName)
                    .getLongOpt());
        }

        return new File[0];
    }

    /**
     * Reads the file paths to {@link File} objects.
     *
     * @param paths     the file paths
     * @param inputName the input name
     * @return the {@link File}s
     * @throws FileNotFoundException if a file does not exists
     */
    protected File[] readPathsToFiles(String paths[], String inputName) throws FileNotFoundException {
        File[] files = new File[paths.length];
        File file;
        for (int i = 0; i < paths.length; i++) {
            file = new File(paths[i]);
            if (file.exists()) {
                files[i] = file;
            } else {
                throw new FileNotFoundException(String.format(ExceptionMessages.FILE_NOT_EXISTS.toString(), file
                        .getAbsoluteFile(), inputName));
            }
        }

        return files;
    }

    @Override
    public void run() {
        try {
            logConfigurations();
            buildKnowledgeBase();
            buildTheory();
            buildExampleSet();
            buildEngineSystemTranslator();
            buildLearningSystem();
            //TODO: test the revision
            reviseExamples();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException
                e) {
            logger.error(LogMessages.ERROR_READING_INPUT_FILES, e);
        } catch (InitializationException e) {
            logger.error(LogMessages.ERROR_INITIALIZING_COMPONENTS, e);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            logger.error(LogMessages.ERROR_READING_CONFIGURATION_FILE, e);
        }
    }

    /**
     * Logs the configurations from the configuration file.
     *
     * @throws FileNotFoundException        if the file does not exists
     * @throws UnsupportedEncodingException if the encoding is not supported
     */
    protected void logConfigurations() throws FileNotFoundException, UnsupportedEncodingException {
        logger.info(LogMessages.CONFIGURATION_FILE.toString(), configurationFilePath, LanguageUtils.readFileToString
                (configurationFilePath));
    }

    /**
     * Call the method to revise the examples
     */
    protected void reviseExamples() {
        for (Example example : examples) {
            try {
                learningSystem.incomingExampleManager.incomingExamples(example);
            } catch (TheoryRevisionException e) {
                logger.error(LogMessages.ERROR_REVISING_THEORY, e);
            }
        }
    }

    /**
     * Builds the {@link LearningSystem}.
     *
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void buildLearningSystem() throws InitializationException {
        logger.info(LogMessages.BUILDING_LEARNING_SYSTEM.toString(), LearningSystem.class.getSimpleName());
        learningSystem = new LearningSystem(knowledgeBase, theory, examples, engineSystemTranslator);
        learningSystem.concurrent = controlConcurrence;
        List<TheoryMetric> theoryMetrics = initializeMetrics();
        List<RevisionOperatorEvaluator> operatorEvaluator = initializeOperators();
        initializeOperatorSelector(operatorEvaluator);
        initializeRevisionManager();
        initializeIncomingExampleManager();
        learningSystem.incomingExampleManager = incomingExampleManager;
        initializeTheoryEvaluator(theoryMetrics);
        learningSystem.theoryEvaluator = theoryEvaluator;
        initializeTheoryRevisionManager(revisionManager);
        learningSystem.theoryRevisionManager = theoryRevisionManager;
    }

    /**
     * Initializes the {@link TheoryRevisionManager}.
     *
     * @param revisionManager the {@link RevisionManager}
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void initializeTheoryRevisionManager(RevisionManager revisionManager) throws InitializationException {
        if (theoryRevisionManager == null) {
            theoryRevisionManager = new TheoryRevisionManager();
        }
        theoryRevisionManager.setLearningSystem(learningSystem);
        theoryRevisionManager.setRevisionManager(revisionManager);
        theoryRevisionManager.initialize();
    }

    /**
     * Initializes the {@link TheoryEvaluator}.
     *
     * @param theoryMetrics the {@link TheoryMetric}s
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void initializeTheoryEvaluator(List<TheoryMetric> theoryMetrics) throws InitializationException {
        if (theoryEvaluator == null) {
            theoryEvaluator = new TheoryEvaluator();
        }
        theoryEvaluator.setLearningSystem(learningSystem);
        theoryEvaluator.setTheoryMetrics(theoryMetrics);
        theoryEvaluator.initialize();
    }

    /**
     * Initializes the {@link IncomingExampleManager}.
     *
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void initializeIncomingExampleManager() throws InitializationException {
        if (incomingExampleManager == null) {
            incomingExampleManager = new ReviseAllIncomingExample();
        }
        incomingExampleManager.setLearningSystem(learningSystem);
        incomingExampleManager.initialize();
    }

    /**
     * Initializes the {@link RevisionManager}.
     *
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void initializeRevisionManager() throws InitializationException {
        if (revisionManager == null) {
            revisionManager = new RevisionManager();
        }
        revisionManager.setOperatorSelector(revisionOperatorSelector);
        revisionManager.initialize();
    }

    /**
     * Initializes the {@link RevisionOperatorSelector}.
     *
     * @param operatorEvaluator the {@link RevisionOperatorEvaluator}s
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void initializeOperatorSelector(
            Collection<RevisionOperatorEvaluator> operatorEvaluator) throws InitializationException {
        if (revisionOperatorSelector == null) {
            revisionOperatorSelector = new SelectFirstRevisionOperator();
        }
        revisionOperatorSelector.setOperatorEvaluators(operatorEvaluator);
        revisionOperatorSelector.initialize();
    }

    /**
     * Initializes the {@link RevisionOperatorEvaluator}s.
     *
     * @return the {@link RevisionOperatorEvaluator}s
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected List<RevisionOperatorEvaluator> initializeOperators() throws InitializationException {
        List<RevisionOperatorEvaluator> operatorEvaluator;
        if (revisionOperatorEvaluators == null || revisionOperatorEvaluators.length == 0) {
            operatorEvaluator = defaultRevisionOperator();
        } else {
            operatorEvaluator = Arrays.asList(revisionOperatorEvaluators);
        }
        for (RevisionOperatorEvaluator operator : operatorEvaluator) {
            operator.setLearningSystem(learningSystem);
            operator.initialize();
        }
        return operatorEvaluator;
    }

    /**
     * Builds the default {@link RevisionOperatorEvaluator}s.
     *
     * @return the default {@link RevisionOperatorEvaluator}s
     */
    protected List<RevisionOperatorEvaluator> defaultRevisionOperator() {
        List<RevisionOperatorEvaluator> operatorEvaluator = new ArrayList<>();
        BottomClauseBoundedRule bottomClause = new BottomClauseBoundedRule();
        bottomClause.internalMetric = new F1ScoreMetric();
        operatorEvaluator.add(new RevisionOperatorEvaluator(bottomClause));
        return operatorEvaluator;
    }

    /**
     * Initializes the {@link TheoryMetric}s.
     *
     * @return the {@link TheoryMetric}s
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected List<TheoryMetric> initializeMetrics() throws InitializationException {
        List<TheoryMetric> metrics = (theoryMetrics == null || theoryMetrics.length == 0 ? defaultTheoryMetrics() :
                Arrays.asList(theoryMetrics));
        for (TheoryMetric metric : metrics) {
            metric.initialize();
        }
        return metrics;
    }

    /**
     * Builds the default {@link TheoryMetric}s.
     *
     * @return the default {@link TheoryMetric}s
     */
    protected List<TheoryMetric> defaultTheoryMetrics() {
        List<TheoryMetric> metrics = new ArrayList<>();
        metrics.add(new AccuracyMetric());
        metrics.add(new PrecisionMetric());
        metrics.add(new RecallMetric());
        metrics.add(new F1ScoreMetric());

        metrics.add(new LikelihoodMetric());
        metrics.add(new LogLikelihoodMetric());
        return metrics;
    }

    /**
     * Builds the {@link EngineSystemTranslator}.
     *
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void buildEngineSystemTranslator() throws InitializationException {
        if (engineSystemTranslator == null) { engineSystemTranslator = new ProPprEngineSystemTranslator<>(); }
        logger.info(LogMessages.BUILDING_ENGINE_SYSTEM_TRANSLATOR.toString(),
                    engineSystemTranslator.getClass().getSimpleName());

        engineSystemTranslator.setKnowledgeBase(knowledgeBase);
        engineSystemTranslator.setTheory(theory);
        engineSystemTranslator.initialize();
    }

    /**
     * Builds the {@link KnowledgeBase} from the input files.
     *
     * @throws IllegalAccessException if an error occurs when instantiating a new object by reflection
     * @throws InstantiationException if an error occurs when instantiating a new object by reflection
     */
    protected void buildKnowledgeBase() throws IllegalAccessException, InstantiationException {
        List<Clause> clauses = readInputKnowledge(knowledgeBaseFiles);
        ClausePredicate predicate = knowledgeBasePredicateClass.newInstance();
        logger.debug(LogMessages.CREATING_KNOWLEDGE_BASE_WITH_PREDICATE.toString(), predicate);

        knowledgeBase = new KnowledgeBase(knowledgeBaseCollectionClass.newInstance(), predicate);
        knowledgeBase.addAll(clauses, knowledgeBaseAncestralClass);
        logger.info(LogMessages.KNOWLEDGE_BASE_SIZE.toString(), knowledgeBase.size());
    }

    /**
     * Builds the {@link Theory} from the input files.
     *
     * @throws NoSuchMethodException     if an error occurs when instantiating a new object by reflection
     * @throws IllegalAccessException    if an error occurs when instantiating a new object by reflection
     * @throws InvocationTargetException if an error occurs when instantiating a new object by reflection
     * @throws InstantiationException    if an error occurs when instantiating a new object by reflection
     */
    protected void buildTheory() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        List<Clause> clauses = readInputKnowledge(theoryFiles);

        ClausePredicate predicate = null;
        if (theoryPredicateClass != null && theoryBaseAncestralClass != null) {
            predicate = theoryPredicateClass.getConstructor(theoryBaseAncestralClass.getClass())
                    .newInstance(theoryBaseAncestralClass);
            logger.debug(LogMessages.CREATING_THEORY_WITH_PREDICATE.toString(), predicate);
        }

        theory = new Theory(theoryCollectionClass.newInstance(), predicate);
        theory.addAll(clauses, theoryBaseAncestralClass);
        logger.info(LogMessages.THEORY_SIZE.toString(), theory.size());
    }

    /**
     * Builds the {@link Examples} from the input files.
     *
     * @throws InstantiationException if an error occurs when instantiating a new set
     * @throws IllegalAccessException if an error occurs when instantiating a new set
     */
    protected void buildExampleSet() throws InstantiationException, IllegalAccessException {
        List<InputStream> inputStreams = new ArrayList<>();
        List<AtomExample> atomExamples = new ArrayList<>();
        List<ProPprExample> proPprExamples = new ArrayList<>();
        logger.trace(LogMessages.READING_INPUT_FILES);
        for (File file : exampleFiles) {
            readExamplesToLists(file, atomExamples, proPprExamples);
        }
        logger.info(LogMessages.EXAMPLES_SIZE.toString(), atomExamples.size() + proPprExamples.size());

        examples = new Examples(proPprExamples, atomExamples);
    }

    /**
     * Reads the input {@link File}s to a {@link List} of {@link Clause}s.
     *
     * @param inputFiles the input {@link File}s
     * @return the {@link List} of {@link Clause}s
     */
    protected List<Clause> readInputKnowledge(File[] inputFiles) {
        List<InputStream> inputStreams = new ArrayList<>();
        List<Clause> clauses = new ArrayList<>();
        logger.trace(LogMessages.READING_INPUT_FILES);
        for (File file : inputFiles) {
            readClausesToList(file, clauses);
        }
        logger.debug(LogMessages.READ_CLAUSE_SIZE.toString(), clauses.size());
        return clauses;
    }

    /**
     * Sets the {@link EngineSystemTranslator} if it is not yet set. If it is already set, throws an error.
     *
     * @param engineSystemTranslator the {@link EngineSystemTranslator}
     * @throws KnowledgeException if the {@link EngineSystemTranslator} is already set
     */
    public void setEngineSystemTranslator(EngineSystemTranslator engineSystemTranslator) throws KnowledgeException {
        if (isEngineSystemTranslatorSet()) {
            throw new KnowledgeException(String.format(ExceptionMessages.ERROR_RESET_FIELD_NOT_ALLOWED.toString(),
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
                Arrays.deepToString(knowledgeBaseFiles) +
                "\n" +
                "\t" +
                "Theory files:\t\t\t" +
                Arrays.deepToString(theoryFiles) +
                "\n" +
                "\t" +
                "Example files:\t\t\t" +
                Arrays.deepToString(exampleFiles);

        return description.trim();
    }

}
