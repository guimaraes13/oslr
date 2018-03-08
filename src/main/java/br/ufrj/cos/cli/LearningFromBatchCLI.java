/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2018 Victor Guimarães
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
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
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
import br.ufrj.cos.knowledge.theory.manager.feature.DumbFeatureGenerator;
import br.ufrj.cos.knowledge.theory.manager.feature.FeatureGenerator;
import br.ufrj.cos.knowledge.theory.manager.revision.RevisionManager;
import br.ufrj.cos.knowledge.theory.manager.revision.RevisionOperatorEvaluator;
import br.ufrj.cos.knowledge.theory.manager.revision.RevisionOperatorSelector;
import br.ufrj.cos.knowledge.theory.manager.revision.SelectFirstRevisionOperator;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.generalization.BottomClauseBoundedRule;
import br.ufrj.cos.knowledge.theory.manager.revision.point.IndependentSampleSelector;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.util.*;
import br.ufrj.cos.util.statistics.RunStatistics;
import br.ufrj.cos.util.time.RunTimeStamp;
import br.ufrj.cos.util.time.TimeMeasure;
import com.esotericsoftware.yamlbeans.YamlException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.*;

import static br.ufrj.cos.util.log.GeneralLog.*;
import static br.ufrj.cos.util.log.IterationLog.*;
import static br.ufrj.cos.util.log.PreRevisionLog.PASSING_EXAMPLE_OF_TOTAL_REVISION;
import static br.ufrj.cos.util.log.SystemLog.*;
import static br.ufrj.cos.util.time.TimeUtils.formatNanoDifference;

/**
 * A Command Line Interface which allows experiments of learning from files.
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings({"CanBeFinal"})
public class LearningFromBatchCLI extends CommandLineInterface {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    /**
     * The default yaml configuration file.
     */
    public static final String DEFAULT_YAML_CONFIGURATION_FILE = "default.yml";
    /**
     * The statistic output file name.
     */
    public static final String STATISTICS_FILE_NAME = "statistics.yaml";
    /**
     * The name of the saved theory file.
     */
    public static final String THEORY_FILE_NAME = "theory.pl";
    /**
     * The name of the file to save the train inference.
     */
    public static final String TRAIN_INFERENCE_FILE_NAME = "inference.train.tsv";
    /**
     * The name of the file to save the test inference.
     */
    public static final String TEST_INFERENCE_FILE_NAME = "inference.test.tsv";
    /**
     * The default size of the batches.
     */
    public static final int DEFAULT_MINI_BATCH_SIZE = 1;
    private static final String[] STRINGS = new String[0];
    /**
     * The default value of {@link #trainParametersOnRemainingExamples}.
     */
    @SuppressWarnings("ConstantNamingConvention")
    public static final boolean DEFAULT_TRAIN_PARAMETERS_ON_REMAINING_EXAMPLES = false;
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
    public String[] knowledgeBaseFilePaths = STRINGS;
    /**
     * Input theory files.
     */
    public String[] theoryFilePaths = STRINGS;
    /**
     * Input example files.
     */
    public String[] exampleFilePaths = STRINGS;
    /**
     * Input test files.
     */
    public String[] testFilePaths = STRINGS;
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
     * The {@link FeatureGenerator}.
     */
    public FeatureGenerator featureGenerator;
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
     * If {@code true}, passes all the examples at once to the learning system. If {@code false}, passes a example at
     * a time. Default is {@code false}.
     */
    public boolean passAllExampleAtOnce = false;
    /**
     * The size of the batch, the incoming examples will be grouped in batches, of this size, to be passed to
     * revision.
     */
    @SuppressWarnings("CanBeFinal")
    public int examplesBatchSize = DEFAULT_MINI_BATCH_SIZE;
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
     * The train examples.
     */
    protected Examples trainExamples;
    /**
     * The test examples.
     */
    protected Examples testExamples;
    /**
     * The learning system.
     */
    protected LearningSystem learningSystem;

    /**
     * The integer number format.
     */
    protected NumberFormat integerFormat;

    private TimeMeasure<RunTimeStamp> timeMeasure;
    private RunStatistics<RunTimeStamp> runStatistics;

    /**
     * If true, train the parameters of the {@link EngineSystemTranslator} on the remaining examples.
     */
    public boolean trainParametersOnRemainingExamples = DEFAULT_TRAIN_PARAMETERS_ON_REMAINING_EXAMPLES;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new LearningFromBatchCLI();
        mainProgram(instance, logger, args);
    }

    @Override
    public void run() {
        try {
            timeMeasure.measure(RunTimeStamp.BEGIN_TRAIN);
            reviseExamples();
            trainRemainingExamples();
            timeMeasure.measure(RunTimeStamp.END_TRAIN);
            timeMeasure.measure(RunTimeStamp.BEGIN_EVALUATION);
            evaluateModel();
            timeMeasure.measure(RunTimeStamp.END_EVALUATION);
            timeMeasure.measure(RunTimeStamp.BEGIN_DISK_OUTPUT);
            saveParameters();
            timeMeasure.measure(RunTimeStamp.END_DISK_OUTPUT);
            timeMeasure.endMeasure(RunTimeStamp.END);
            logger.warn(runStatistics);
            logElapsedTimes();
            saveStatistics();
        } catch (IOException e) {
            logger.error(ERROR_READING_CONFIGURATION_FILE, e);
        }
    }

    /**
     * Trains the parameters of the {@link EngineSystemTranslator} on the remaining examples.
     */
    protected void trainRemainingExamples() {
        if (!trainParametersOnRemainingExamples) { return; }
        Collection<? extends Example> remainingExamples = incomingExampleManager.getRemainingExamples();
        logger.info(BEGIN_TRAINING_REMAINING_EXAMPLES.toString(), integerFormat.format(remainingExamples.size()));
        learningSystem.trainParameters(trainExamples);
        learningSystem.saveTrainedParameters();
        logger.info(END_TRAINING_REMAINING_EXAMPLES.toString());
    }

    /**
     * Call the method to revise the examples
     */
    protected void reviseExamples() {
        //IMPROVE: delegate this function to the ExampleStream
        logger.info(BEGIN_REVISION_EXAMPLE.toString(), integerFormat.format(trainExamples.size()));
        if (passAllExampleAtOnce) {
            learningSystem.incomingExampleManager.incomingExamples(trainExamples);
        } else if (examplesBatchSize > 1) {
            passBatchExamplesToRevise();
        } else {
            passEachExampleAtTime();
        }
        logger.info(END_REVISION_EXAMPLE.toString());
    }

    /**
     * Passes the examples to revise.
     */
    protected void passBatchExamplesToRevise() {
        final IterableSize<? extends Example> currentExamples = new IterableSize<>(examplesBatchSize, trainExamples);
        final int size = trainExamples.size();
        int count = Math.min(size, examplesBatchSize);
        for (int i = 0; i < size / examplesBatchSize; i++) {
            logger.debug(PASSING_EXAMPLE_OF_TOTAL_REVISION.toString(), integerFormat.format(count),
                         integerFormat.format(size));
            learningSystem.incomingExampleManager.incomingExamples(currentExamples);
            currentExamples.reset();
            count += examplesBatchSize;
        }
        logger.debug(PASSING_EXAMPLE_OF_TOTAL_REVISION.toString(), integerFormat.format(size),
                     integerFormat.format(size));
        learningSystem.incomingExampleManager.incomingExamples(currentExamples);
    }

    /**
     * Passes a example at a time to the learning system.
     */
    protected void passEachExampleAtTime() {
        int count = 1;
        final int size = trainExamples.size();
        for (Example example : trainExamples) {
            logger.trace(PASSING_EXAMPLE_OF_TOTAL_REVISION.toString(), integerFormat.format(count), integerFormat
                    .format(size));
            learningSystem.incomingExampleManager.incomingExamples(example);
            count++;
        }
    }

    /**
     * Evaluates the model.
     */
    protected void evaluateModel() {
        Map<Example, Map<Atom, Double>> inferredExamples = learningSystem.inferExamples(trainExamples);
        runStatistics.setTrainEvaluation(learningSystem.evaluate(trainExamples, inferredExamples));
        FileIOUtils.saveInferencesToTsvFile(inferredExamples, trainExamples,
                                            new File(outputDirectory, TRAIN_INFERENCE_FILE_NAME));
        if (!testExamples.isEmpty()) {
            inferredExamples = learningSystem.inferExamples(testExamples);
            runStatistics.setTestEvaluation(learningSystem.evaluate(testExamples, inferredExamples));
            FileIOUtils.saveInferencesToTsvFile(inferredExamples, testExamples,
                                                new File(outputDirectory, TEST_INFERENCE_FILE_NAME));
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
     * Logs the elapsed times of the run.
     */
    private void logElapsedTimes() {
        long initializeTime = timeMeasure.timeBetweenStamps(RunTimeStamp.BEGIN_INITIALIZE, RunTimeStamp.END_INITIALIZE);
        long trainingTime = timeMeasure.timeBetweenStamps(RunTimeStamp.BEGIN_TRAIN, RunTimeStamp.END_TRAIN);
        long evaluationTime = timeMeasure.timeBetweenStamps(RunTimeStamp.BEGIN_EVALUATION, RunTimeStamp.END_EVALUATION);
        long outputTime = timeMeasure.timeBetweenStamps(RunTimeStamp.BEGIN_DISK_OUTPUT, RunTimeStamp.END_DISK_OUTPUT);
        long totalProgramTime = timeMeasure.timeBetweenStamps(RunTimeStamp.BEGIN, RunTimeStamp.END);

        logger.warn(TOTAL_INITIALIZATION_TIME.toString(), formatNanoDifference(initializeTime));
        logger.warn(TOTAL_TRAINING_TIME.toString(), formatNanoDifference(trainingTime));
        logger.warn(TOTAL_EVALUATION_TIME.toString(), formatNanoDifference(evaluationTime));
        logger.warn(TOTAL_OUTPUT_TIME.toString(), formatNanoDifference(outputTime));
        logger.warn(TOTAL_PROGRAM_TIME.toString(), formatNanoDifference(totalProgramTime));
    }

    /**
     * Saves the statistics of the run to a yaml file.
     */
    private void saveStatistics() {
        try {
            RunStatistics<String> statistics = new RunStatistics<>();

            statistics.setKnowledgeSize(runStatistics.getKnowledgeSize());
            statistics.setExamplesSize(runStatistics.getExamplesSize());
            statistics.setTestSize(runStatistics.getTestSize());

            statistics.setTrainEvaluation(runStatistics.getTrainEvaluation());
            statistics.setTestEvaluation(runStatistics.getTestEvaluation());

            statistics.setTimeMeasure(timeMeasure.convertTimeMeasure(RunTimeStamp::name));

            FileIOUtils.writeObjectToYamlFile(statistics, getStatisticsFile());
        } catch (IOException e) {
            logger.error(ERROR_WRITING_STATISTICS_FILE, e);
        }
    }

    /**
     * Gets the statistics file name.
     *
     * @return the statistics file name
     */
    public File getStatisticsFile() {
        return new File(outputDirectory, STATISTICS_FILE_NAME);
    }

    @Override
    public void initialize() throws InitializationException {
        timeMeasure = new TimeMeasure<>();
        timeMeasure.measure(RunTimeStamp.BEGIN);
        timeMeasure.measure(RunTimeStamp.BEGIN_INITIALIZE);
        super.initialize();
        integerFormat = NumberFormat.getIntegerInstance();
        instantiateClasses();
        saveConfigurations();
        runStatistics = new RunStatistics<>();
        try {
            build();
        } catch (ReflectiveOperationException | IOException e) {
            throw new InitializationException(ExceptionMessages.ERROR_BUILD_LEARNING_SYSTEM.toString(), e);
        }
        runStatistics.setTimeMeasure(timeMeasure);
        timeMeasure.measure(RunTimeStamp.END_INITIALIZE);
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
     * Builds the {@link KnowledgeBase} from the input files.
     *
     * @throws IllegalAccessException if an error occurs when instantiating a new object by reflection
     * @throws InstantiationException if an error occurs when instantiating a new object by reflection
     * @throws FileNotFoundException  if a file does not exists
     */
    protected void buildKnowledgeBase() throws IllegalAccessException, InstantiationException, FileNotFoundException {
        List<Clause> clauses = FileIOUtils.readInputKnowledge(FileIOUtils.readPathsToFiles(knowledgeBaseFilePaths,
                                                                                           CommandLineOptions
                                                                                                   .KNOWLEDGE_BASE
                                                                                                   .getOptionName()));

        ClausePredicate predicate = knowledgeBasePredicateClass.newInstance();
        logger.debug(CREATING_KNOWLEDGE_BASE_WITH_PREDICATE.toString(), predicate);
        knowledgeBase = new KnowledgeBase(knowledgeBaseCollectionClass.newInstance(), predicate);

        knowledgeBase.addAll(clauses, knowledgeBaseAncestralClass);
        runStatistics.setKnowledgeSize(knowledgeBase.size());
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
        List<Clause> clauses = FileIOUtils.readInputKnowledge(
                FileIOUtils.readPathsToFiles(theoryFilePaths, CommandLineOptions.THEORY.getOptionName()));

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
     * Builds the examples
     *
     * @throws InstantiationException if an error occurs when instantiating a new set
     * @throws IllegalAccessException if an error occurs when instantiating a new set
     * @throws FileNotFoundException  if a file does not exists
     */
    protected void buildExamples() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        trainExamples = FileIOUtils.buildExampleSet(exampleFilePaths);
        testExamples = FileIOUtils.buildExampleSet(testFilePaths);
        runStatistics.setExamplesSize((int) trainExamples.stream().mapToLong(e -> e.getGroundedQuery().size()).sum());
        runStatistics.setTestSize((int) testExamples.stream().mapToLong(e -> e.getGroundedQuery().size()).sum());
    }

    /**
     * Builds the {@link EngineSystemTranslator}.
     */
    protected void buildEngineSystemTranslator() {
        if (engineSystemTranslator == null) { engineSystemTranslator = new ProPprEngineSystemTranslator<>(); }
        logger.info(BUILDING_ENGINE_SYSTEM_TRANSLATOR.toString(),
                    engineSystemTranslator.getClass().getSimpleName());

        engineSystemTranslator.setKnowledgeBase(knowledgeBase);
        engineSystemTranslator.setTheory(theory);
        engineSystemTranslator.initialize();
        if (loadedPreTrainedParameters) { engineSystemTranslator.loadParameters(outputDirectory); }
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
        buildFeatureGenerator();
        buildOperatorSelector();

        buildIncomingExampleManager();
        buildTheoryEvaluator(theoryMetrics);
        buildTheoryRevisionManager();
        learningSystem.initialize();
    }

    /**
     * Initializes the {@link FeatureGenerator}.
     *
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected void buildFeatureGenerator() throws InitializationException {
        if (featureGenerator == null) {
            //noinspection deprecation
            featureGenerator = new DumbFeatureGenerator();
        }
        featureGenerator.setLearningSystem(learningSystem);
        featureGenerator.initialize();
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
            operator.setFeatureGenerator(featureGenerator);
        }
        return operatorEvaluator;
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

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        if (options == null) { options = new Options(); }

        options.addOption(CommandLineOptions.KNOWLEDGE_BASE.getOption());
        options.addOption(CommandLineOptions.THEORY.getOption());
        options.addOption(CommandLineOptions.EXAMPLES.getOption());
        options.addOption(CommandLineOptions.TEST.getOption());
        options.addOption(CommandLineOptions.YAML.getOption());
        options.addOption(CommandLineOptions.OUTPUT_DIRECTORY.getOption());
    }

    @Override
    public CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        try {
            super.parseOptions(commandLine);
            LearningFromBatchCLI cli = readYamlFile(commandLine, this.getClass(), DEFAULT_YAML_CONFIGURATION_FILE);
            cli.knowledgeBaseFilePaths = getFilesFromOption(commandLine,
                                                            CommandLineOptions.KNOWLEDGE_BASE.getOptionName(),
                                                            cli.knowledgeBaseFilePaths);
            cli.theoryFilePaths = getFilesFromOption(commandLine, CommandLineOptions.THEORY.getOptionName(),
                                                     cli.theoryFilePaths);
            cli.exampleFilePaths = getFilesFromOption(commandLine, CommandLineOptions.EXAMPLES.getOptionName(),
                                                      cli.exampleFilePaths);
            cli.testFilePaths = getFilesFromOption(commandLine, CommandLineOptions.TEST.getOptionName(),
                                                   cli.testFilePaths);
            cli.outputDirectoryPath = commandLine.getOptionValue(CommandLineOptions.OUTPUT_DIRECTORY.getOptionName(),
                                                                 cli.outputDirectoryPath);
            return cli;
        } catch (FileNotFoundException | YamlException e) {
            throw new CommandLineInterrogationException(e);
        }
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
                Arrays.deepToString(theoryFilePaths != null ? theoryFilePaths : STRINGS) +
                "\n" +
                "\t" +
                "Example files:\t\t\t" +
                Arrays.deepToString(exampleFilePaths) +
                "\n" +
                "\t" +
                "Test files:\t\t\t\t" +
                Arrays.deepToString(testFilePaths);

        return description.trim();
    }

}