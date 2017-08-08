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

import br.ufrj.cos.cli.nell.NellBaseConverterCLI;
import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.example.ProPprExample;
import br.ufrj.cos.knowledge.filter.ClausePredicate;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.parser.example.ExampleParser;
import br.ufrj.cos.logic.parser.knowledge.ParseException;
import br.ufrj.cos.util.*;
import br.ufrj.cos.util.time.*;
import com.esotericsoftware.yamlbeans.YamlException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;

import static br.ufrj.cos.cli.CommandLineOptions.*;
import static br.ufrj.cos.util.FileIOUtils.DEFAULT_INPUT_ENCODE;
import static br.ufrj.cos.util.log.GeneralLog.*;
import static br.ufrj.cos.util.log.IterationLog.*;
import static br.ufrj.cos.util.log.PreRevisionLog.PASSING_EXAMPLE_REVISION;
import static br.ufrj.cos.util.log.SystemLog.*;
import static br.ufrj.cos.util.time.RunTimeStamp.BEGIN_INITIALIZE;
import static br.ufrj.cos.util.time.RunTimeStamp.END_INITIALIZE;
import static br.ufrj.cos.util.time.TimeUtils.formatNanoDifference;

/**
 * A Command Line Interface which allows experiments of learning from files separated by iterations.
 * <p>
 * Created on 02/08/17.
 *
 * @author Victor Guimarães
 */
public class LearningFromIterationsCLI extends LearningFromFilesCLI {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * The default yaml configuration file.
     */
    public static final String DEFAULT_YAML_CONFIGURATION_FILE = "default_it.yml";
    /**
     * The directory to save the run output data in.
     */
    public static final String OUTPUT_RUN_DIRECTORY = "RUN_%s";
    /**
     * The iteration suffix pattern.
     */
    public static final String ITERATION_SUFFIX_PATTERN = "[0-9]+";
    /**
     * The start string regex.
     */
    public static final String START_STRING = "^";
    /**
     * The default example file extension.
     */
    public static final String DEFAULT_EXAMPLES_FILE_EXTENSION = ".data";
    /**
     * The statistic output file name.
     */
    public static final String STATISTICS_FILE_NAME = "statistics.yaml";
    /**
     * The name of the file that logs the output.
     */
    public static final String STDOUT_LOG_FILE_NAME = "output.txt";
    /**
     * The name of the file to save the train inference.
     */
    public static final String TRAIN_INFERENCE_FILE_NAME = "inference.train.tsv";
    /**
     * The name of the file to save the test inference.
     */
    public static final String TEST_INFERENCE_FILE_NAME = "inference.test.tsv";

    private static final File[] FILES = new File[0];
    /**
     * The example file extension.
     */
    public String examplesFileExtension = DEFAULT_EXAMPLES_FILE_EXTENSION;
    /**
     * The data directory path. The directory to find the iterations in.
     */
    public String dataDirectoryPath;
    /**
     * The iteration prefix.
     */
    public String iterationPrefix = NellBaseConverterCLI.DEFAULT_ITERATION_PREFIX;
    /**
     * The positive output extension.
     */
    public String positiveExtension = NellBaseConverterCLI.DEFAULT_POSITIVE_EXTENSION;
    /**
     * The negative output extension.
     */
    public String negativeExtension = NellBaseConverterCLI.DEFAULT_NEGATIVE_EXTENSION;
    /**
     * The target relation to learn the theory.
     */
    public String targetRelation;

    /**
     * The set of selected relations to consider. If it is null (or empty), all the relations will be considered,
     * otherwise, only the relations in the set will be.
     */
    @SuppressWarnings("CanBeFinal")
    public Set<String> selectedRelations = null;

    protected File[] iterationDirectories;
    protected List<Collection<? extends Atom>> iterationKnowledge;
    protected List<Examples> iterationExamples;
    protected AtomFactory atomFactory;

    protected IterationStatistics<TimeStampTag> iterationStatistics;
    protected TimeMeasure<TimeStampTag> timeMeasure;
    protected IterationTimeStampFactory timeStampFactory;
    protected Map<Example, Map<Atom, Double>> trainInferredExamples;
    protected Map<Example, Map<Atom, Double>> testInferredExamples;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Locale.setDefault(new Locale(DEFAULT_LANGUAGE, DEFAULT_COUNTRY));
        try {
            CommandLineInterface main = new LearningFromIterationsCLI();
            main = main.parseOptions(args);
            run(main, args);
        } catch (Exception e) {
            logger.error(ERROR_MAIN_PROGRAM, e);
        } finally {
            logger.fatal(PROGRAM_END);
        }
    }

    @Override
    public void run() {
        try {
            timeMeasure.measure(RunTimeStamp.BEGIN_TRAIN);
            reviseExamples();
            timeMeasure.measure(RunTimeStamp.END_TRAIN);
            timeMeasure.measure(RunTimeStamp.BEGIN_DISK_OUTPUT);
            saveParameters();
            timeMeasure.measure(RunTimeStamp.END_DISK_OUTPUT);
            timeMeasure.endMeasure(RunTimeStamp.END);
            logger.warn(iterationStatistics);
            saveStatistics();
            logElapsedTimes();
        } catch (IOException e) {
            logger.error(ERROR_WRITING_OUTPUT_FILE, e);
        }
    }

    /**
     * Saves the statistics of the run to a yaml file.
     */
    protected void saveStatistics() {
        try {
            IterationStatistics<String> statistics = new IterationStatistics<>();
            statistics.setNumberOfIterations(iterationStatistics.getNumberOfIterations());
            statistics.setIterationPrefix(iterationStatistics.getIterationPrefix());
            statistics.setTargetRelation(iterationStatistics.getTargetRelation());

            statistics.setIterationKnowledgeSizes(iterationStatistics.getIterationKnowledgeSizes());
            statistics.setIterationExamplesSizes(iterationStatistics.getIterationExamplesSizes());

            statistics.setIterationTrainEvaluation(iterationStatistics.getIterationTrainEvaluation());
            statistics.setIterationTestEvaluation(iterationStatistics.getIterationTestEvaluation());

            statistics.setTimeMeasure(timeMeasure.convertTimeMeasure(TimeStampTag::getMessage));

            FileIOUtils.writeObjectToYamlFile(statistics, getStatisticsFile());
        } catch (IOException e) {
            logger.error(ERROR_WRITING_STATISTICS_FILE, e);
        }
    }

    /**
     * Logs the elapsed times of the run.
     */
    protected void logElapsedTimes() {
        long initializeTime = timeMeasure.timeBetweenStamps(RunTimeStamp.BEGIN_INITIALIZE, RunTimeStamp.END_INITIALIZE);
        long allTrainingTime = timeMeasure.timeBetweenStamps(RunTimeStamp.BEGIN_TRAIN, RunTimeStamp.END_TRAIN);
        long outputTime = timeMeasure.timeBetweenStamps(RunTimeStamp.BEGIN_DISK_OUTPUT, RunTimeStamp.END_DISK_OUTPUT);
        long totalProgramTime = timeMeasure.timeBetweenStamps(RunTimeStamp.BEGIN, RunTimeStamp.END);

        logger.warn(TOTAL_INITIALIZATION_TIME.toString(), formatNanoDifference(initializeTime));
        logger.warn(TOTAL_TRAINING_TIME.toString(), formatNanoDifference(allTrainingTime));

        detailedTimeBySteps();

        logger.warn(TOTAL_OUTPUT_TIME.toString(), formatNanoDifference(outputTime));
        logger.warn(TOTAL_PROGRAM_TIME.toString(), formatNanoDifference(totalProgramTime));
    }

    /**
     * A detailed log which logs the total time of the iterations by each step. The step are: loading knowledge,
     * revision, inference, output files.
     */
    protected void detailedTimeBySteps() {
        long iterationLoadTime = 0;
        long iterationRevisionTime = 0;
        long iterationInferenceTime = 0;
        long iterationOutputTime = 0;
        for (int i = 0; i < iterationKnowledge.size(); i++) {
            iterationLoadTime += timeMeasure.timeBetweenStamps(
                    timeStampFactory.getTimeStamp(i, IterationTimeMessage.BEGIN),
                    timeStampFactory.getTimeStamp(i, IterationTimeMessage.LOAD_KNOWLEDGE_DONE));
            iterationRevisionTime += timeMeasure.timeBetweenStamps(
                    timeStampFactory.getTimeStamp(i, IterationTimeMessage.LOAD_KNOWLEDGE_DONE),
                    timeStampFactory.getTimeStamp(i, IterationTimeMessage.REVISION_DONE));
            iterationInferenceTime += timeMeasure.timeBetweenStamps(
                    timeStampFactory.getTimeStamp(i, IterationTimeMessage.REVISION_DONE),
                    timeStampFactory.getTimeStamp(i, IterationTimeMessage.EVALUATION_DONE));
            iterationOutputTime += timeMeasure.timeBetweenStamps(
                    timeStampFactory.getTimeStamp(i, IterationTimeMessage.EVALUATION_DONE),
                    timeStampFactory.getTimeStamp(i, IterationTimeMessage.END));
        }

        logger.warn(ITERATIONS_KNOWLEDGE_LOAD_TIME.toString(), formatNanoDifference(iterationLoadTime));
        logger.warn(ITERATIONS_REVISION_TIME.toString(), formatNanoDifference(iterationRevisionTime));
        logger.warn(ITERATIONS_EVALUATION_TIME.toString(), formatNanoDifference(iterationOutputTime));
        logger.warn(ITERATIONS_SAVING_FILES_TIME.toString(), formatNanoDifference(iterationInferenceTime));
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
        timeMeasure.measure(BEGIN_INITIALIZE);
        super.initialize();
        try {
            timeStampFactory = new IterationTimeStampFactory(iterationPrefix);
            iterationDirectories = getIterationDirectory(dataDirectoryPath, iterationPrefix);
            build();
            atomFactory = new AtomFactory();
            iterationStatistics = new IterationStatistics();
            buildIterationKnowledge();
            buildIterationExamples();
            iterationStatistics.setNumberOfIterations(iterationKnowledge.size());
            iterationStatistics.setIterationPrefix(iterationPrefix);
            iterationStatistics.setTargetRelation(targetRelation);
            iterationStatistics.setTimeMeasure(timeMeasure);
            timeMeasure.measure(END_INITIALIZE);
        } catch (IOException | ParseException |
                br.ufrj.cos.logic.parser.example.ParseException | ReflectiveOperationException e) {
            throw new InitializationException(e);
        }
    }

    /**
     * Builds the examples of each iteration.
     *
     * @throws IOException                                     if an I/O error has occurred
     * @throws br.ufrj.cos.logic.parser.example.ParseException if a error occurs during the parsing
     * @throws ReflectiveOperationException                    if an error occurs when instantiating a new set of
     *                                                         examples
     */
    protected void buildIterationExamples() throws IOException, br.ufrj.cos.logic.parser.example
            .ParseException, ReflectiveOperationException {
        logger.info(BUILDING_EXAMPLES_ITERATIONS);
        File iterationExample;
        iterationExamples = new ArrayList<>(iterationDirectories.length);
        Examples examples;
        for (File iteration : iterationDirectories) {
            iterationExample = new File(iteration, targetRelation + examplesFileExtension);
            examples = readExamplesFromFile(iterationExample);
            iterationExamples.add(examples);
            iterationStatistics.addIterationExamplesSizes(examples.size());
        }
    }

    /**
     * Call the method to revise the examples
     */
    @Override
    protected void reviseExamples() {
        final int numberOfIterations = iterationKnowledge.size();
        logger.info(BEGIN_REVISION_ITERATIONS.toString(), numberOfIterations);
        for (int i = 0; i < numberOfIterations; i++) {
            reviseIteration(i);
        }
        logger.info(END_REVISION_ITERATIONS.toString(), numberOfIterations);
    }

    /**
     * Revises the iteration.
     *
     * @param index the index of the current iteration
     */
    protected void reviseIteration(int index) {
        IterationTimeStamp beginStamp;
        IterationTimeStamp endStamp;// begin iteration
        logger.debug(REVISING_ITERATION.toString(), index);
        beginStamp = timeStampFactory.getTimeStamp(index, IterationTimeMessage.BEGIN);
        timeMeasure.measure(beginStamp);
        addIterationKnowledge(index);
        // measure the time to add knowledge to the learning system
        passExamplesToRevise(index);
        // measure the time to train in the iteration
        timeMeasure.measure(timeStampFactory.getTimeStamp(index, IterationTimeMessage.REVISION_DONE));
        logger.trace(END_REVISION_EXAMPLE.toString());
        evaluateIteration(index);
        saveIterationFiles(index);
        endStamp = timeStampFactory.getTimeStamp(index, IterationTimeMessage.END);
        timeMeasure.measure(endStamp);
        logger.debug(END_REVISION_ITERATION.toString(), index);
        logger.debug(ITERATION_TRAINING_TIME.toString(), timeMeasure.textTimeBetweenStamps(beginStamp, endStamp));
    }

    /**
     * Adds the iteration knowledge to the learning system
     *
     * @param index the index of the iteration
     */
    protected void addIterationKnowledge(int index) {
        learningSystem.addAtomsToKnowledgeBase(iterationKnowledge.get(index));
        logger.trace(ADDED_ITERATION_KNOWLEDGE.toString(), integerFormat.format(iterationKnowledge.get(index).size()));
    }

    /**
     * Passes the examples to revise.
     *
     * @param index the index of the iteration
     */
    protected void passExamplesToRevise(int index) {
        final Examples currentExamples = iterationExamples.get(index);
        final int size = currentExamples.size();
        logger.trace(BEGIN_REVISION_EXAMPLE.toString(), integerFormat.format(size));
        timeMeasure.measure(timeStampFactory.getTimeStamp(index, IterationTimeMessage.LOAD_KNOWLEDGE_DONE));
        int count = 1;
        for (Example example : currentExamples) {
            logger.trace(PASSING_EXAMPLE_REVISION.toString(), integerFormat.format(count), integerFormat.format(size));
            learningSystem.incomingExampleManager.incomingExamples(example);
            count++;
        }
    }

    /**
     * Evaluates the trained iteration
     *
     * @param index the index of the iteration
     */
    protected void evaluateIteration(int index) {
        logger.trace(BEGIN_EVALUATION.toString(), index);
        Examples examples = iterationExamples.get(index);
        trainInferredExamples = learningSystem.inferExamples(examples);
        iterationStatistics.addIterationTrainEvaluation(learningSystem.evaluate(examples, trainInferredExamples));
        // measure the to evaluate the iteration in the train set
        timeMeasure.measure(timeStampFactory.getTimeStamp(index, IterationTimeMessage.TRAIN_EVALUATION_DONE));
        logger.trace(END_TRAIN_EVALUATION.toString(), index);
        if (index < iterationKnowledge.size() - 1) {
            examples = iterationExamples.get(index + 1);
            testInferredExamples = learningSystem.inferExamples(examples);
            iterationStatistics.addIterationTestEvaluation(learningSystem.evaluate(examples, testInferredExamples));
            // measure the to evaluate the iteration in the test set
            timeMeasure.measure(timeStampFactory.getTimeStamp(index, IterationTimeMessage.TEST_EVALUATION_DONE));
            logger.trace(END_TEST_EVALUATION.toString(), index);
        } else {
            testInferredExamples = null;
        }
        timeMeasure.measure(timeStampFactory.getTimeStamp(index, IterationTimeMessage.EVALUATION_DONE));
        logger.trace(END_EVALUATION.toString(), index);
    }

    /**
     * Gets the iteration directories.
     *
     * @param dataDirectoryPath the data directory path
     * @param iterationPrefix   the iteration prefix
     * @return the iteration directories
     */
    public static File[] getIterationDirectory(String dataDirectoryPath, String iterationPrefix) {
        Pattern pattern = Pattern.compile(iterationPrefix + ITERATION_SUFFIX_PATTERN);
        File[] iterations = new File(dataDirectoryPath).listFiles((dir, name) -> pattern.matcher(name).matches());
        if (iterations == null) { return FILES; }
        Arrays.sort(iterations,
                    Comparator.comparingInt(i -> LearningFromIterationsCLI.getIterationNumber(iterationPrefix, i)));
        return iterations;
    }

    /**
     * Reads the clauses from each iteration and loads into the {@link #iterationKnowledge}.
     *
     * @throws FileNotFoundException        if a file is not found.
     * @throws UnsupportedEncodingException if the encoding is not supported
     * @throws ParseException               if a parser error occurs
     */
    protected void buildIterationKnowledge() throws IOException,
            ParseException {
        logger.info(BUILDING_KNOWLEDGE_ITERATIONS);
        iterationKnowledge = new ArrayList<>(iterationDirectories.length);
        loadKnowledge();
    }

    /**
     * Saves the iteration files, all the needed files to evaluate the iteration or restore the system to the state
     * it was at the end of the iteration.
     *
     * @param index the index of the iteration
     */
    protected void saveIterationFiles(int index) {
        File iterationDirectory = new File(outputDirectory, iterationPrefix + index);
        if (!iterationDirectory.exists()) {
            //noinspection ResultOfMethodCallIgnored
            iterationDirectory.mkdir();
        }

        saveIterationTheory(iterationDirectory);
        FileIOUtils.saveInferencesToTsvFile(trainInferredExamples, iterationExamples.get(index),
                                            new File(iterationDirectory, TRAIN_INFERENCE_FILE_NAME));
        if (index < iterationKnowledge.size() - 1) {
            FileIOUtils.saveInferencesToTsvFile(testInferredExamples, iterationExamples.get(index + 1),
                                                new File(iterationDirectory, TEST_INFERENCE_FILE_NAME));
        }
        learningSystem.saveParameters(iterationDirectory);
        saveStatistics();
        timeMeasure.measure(timeStampFactory.getTimeStamp(index, IterationTimeMessage.SAVING_EVALUATION_DONE));
        logger.trace(ITERATION_DATA_SAVED.toString(), iterationDirectory.getAbsolutePath());
    }

    /**
     * Gets the number of the iteration.
     *
     * @param iterationPrefix the iteration prefix
     * @param file            the iteration file
     * @return the number of the iteration
     */
    protected static int getIterationNumber(String iterationPrefix, File file) {
        return Integer.parseInt(file.getName().replaceAll(START_STRING + iterationPrefix, ""));
    }

    /**
     * Saves the theory of the iteration
     *
     * @param iterationDirectory the iteration directory
     */
    protected void saveIterationTheory(File iterationDirectory) {
        File theoryFile = new File(iterationDirectory, THEORY_FILE_NAME);
        try {
            String theoryContent = LanguageUtils.theoryToString(learningSystem.getTheory());
            FileIOUtils.writeStringToFile(theoryContent, theoryFile);
        } catch (IOException e) {
            logger.error(ERROR_WRITING_ITERATION_THEORY_FILE, e);
            //noinspection ResultOfMethodCallIgnored
            theoryFile.delete();
        }
    }

    /**
     * Reads the examples from the file
     *
     * @param file the file
     * @return the examples
     * @throws IOException                                     if an I/O error has occurred
     * @throws br.ufrj.cos.logic.parser.example.ParseException if a error occurs during the parsing
     * @throws ReflectiveOperationException                    if an error occurs when instantiating a new set of
     *                                                         examples
     */
    protected static Examples readExamplesFromFile(File file)
            throws IOException, br.ufrj.cos.logic.parser.example.ParseException, ReflectiveOperationException {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), DEFAULT_INPUT_ENCODE));
        ExampleParser parser = new ExampleParser(reader);
        List<AtomExample> atomExamples = new ArrayList<>();
        List<ProPprExample> proPprExamples = new ArrayList<>();
        parser.parseExamplesAppend(atomExamples, proPprExamples);
        reader.close();
        return new Examples(proPprExamples, atomExamples);
    }

    @Override
    protected void buildKnowledgeBase() throws IllegalAccessException, InstantiationException, FileNotFoundException {
        ClausePredicate predicate = knowledgeBasePredicateClass.newInstance();
        logger.debug(CREATING_KNOWLEDGE_BASE_WITH_PREDICATE.toString(), predicate);
        knowledgeBase = new KnowledgeBase(knowledgeBaseCollectionClass.newInstance(), predicate);
    }

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        if (options == null) { options = new Options(); }

        options.addOption(DATA_DIRECTORY.getOption());
        options.addOption(ITERATION_PREFIX.getOption());

        options.addOption(POSITIVE_EXTENSION.getOption());
        options.addOption(NEGATIVE_EXTENSION.getOption());
        options.addOption(EXAMPLES_EXTENSION.getOption());

        options.addOption(TARGET_RELATION.getOption());

        options.addOption(YAML.getOption());
        options.addOption(OUTPUT_DIRECTORY.getOption());
    }

    @Override
    public CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        try {
            super.parseOptions(commandLine);
            LearningFromIterationsCLI cli = readYamlFile(commandLine, this.getClass(), DEFAULT_YAML_CONFIGURATION_FILE);
            cli.dataDirectoryPath = commandLine.getOptionValue(DATA_DIRECTORY.getOptionName(), cli.dataDirectoryPath);
            cli.iterationPrefix = commandLine.getOptionValue(ITERATION_PREFIX.getOptionName(), cli.iterationPrefix);

            cli.positiveExtension = commandLine.getOptionValue(POSITIVE_EXTENSION.getOptionName(),
                                                               cli.positiveExtension);
            cli.negativeExtension = commandLine.getOptionValue(NEGATIVE_EXTENSION.getOptionName(),
                                                               cli.negativeExtension);
            cli.examplesFileExtension = commandLine.getOptionValue(EXAMPLES_EXTENSION.getOptionName(),
                                                                   cli.examplesFileExtension);

            cli.targetRelation = commandLine.getOptionValue(TARGET_RELATION.getOptionName(), cli.targetRelation);

            cli.outputDirectoryPath = commandLine.getOptionValue(OUTPUT_DIRECTORY.getOptionName());
            return cli;
        } catch (FileNotFoundException | YamlException e) {
            throw new CommandLineInterrogationException(e);
        }
    }

    @Override
    protected void buildExamples() {
        examples = new Examples();
    }

    @Override
    protected void buildTheory() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException, FileNotFoundException {
        theory = new Theory(new HashSet<>());
    }

    /**
     * Loads the knowledge from each iteration, skipping the target relation.
     *
     * @throws ParseException if a error occurs during the parsing
     * @throws IOException    if an I/O error has occurred
     */
    protected void loadKnowledge() throws IOException, ParseException {
        final String targetFileName = targetRelation + positiveExtension;
        final FilenameFilter relationFilenameFilter;
        if (selectedRelations == null || selectedRelations.isEmpty()) {
            relationFilenameFilter = (dir, name) -> name.endsWith(positiveExtension);
        } else {
            relationFilenameFilter = (dir, name) -> name.endsWith(positiveExtension) &&
                    selectedRelations.contains(name.substring(0, name.length() - positiveExtension.length()));
        }
        Set<Atom> clauses;
        File[] relations;

        for (File iteration : iterationDirectories) {
            relations = iteration.listFiles(relationFilenameFilter);
            if (relations == null) {
                iterationKnowledge.add(new HashSet<>());
                continue;
            }
            clauses = new HashSet<>();
            appendFactsFromRelations(relations, targetFileName, clauses);
            iterationKnowledge.add(clauses);
            iterationStatistics.addIterationKnowledgeSizes(clauses.size());
        }
    }

    /**
     * Appends the facts from the relation files into the clauses.
     *
     * @param relations      the relation files
     * @param targetFileName the target file name, to skip the target relation
     * @param clauses        the clauses
     * @throws ParseException if a parser error occurs
     * @throws IOException    if an I/O error has occurred
     */
    protected void appendFactsFromRelations(File[] relations, String targetFileName, Set<Atom> clauses)
            throws IOException,
            ParseException {
        for (File relation : relations) {
            if (relation.getName().equals(targetFileName)) { continue; }
            FileIOUtils.readAtomKnowledgeFromFile(atomFactory, clauses, relation);
        }
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append("\t").append("Settings:").append("\n");
        description.append("\t").append("Data Directory:\t\t").append(dataDirectoryPath).append("\n");
        description.append("\t").append("Output Directory:\t").append(outputDirectoryPath).append("\n");
        description.append("\t").append("Iteration Prefix:\t").append(iterationPrefix).append("\n");
        description.append("\t").append("Positive Extension:\t").append(positiveExtension).append("\n");
        description.append("\t").append("Negative Extension:\t").append(negativeExtension).append("\n");
        description.append("\t").append("Examples Extension:\t").append(examplesFileExtension).append("\n");
        description.append("\t").append("Target Relation:\t").append(targetRelation).append("\n");
        description.append("\t").append("Iteration Directories:\n");
        appendSelectedRelations(description);
        for (int i = 0; i < iterationDirectories.length; i++) {
            description.append("\t\t - ").append(iterationDirectories[i].getName());
            description.append("\tSize:\t").append(integerFormat.format(iterationKnowledge.get(i).size()));
            description.append("\tExamples (ProPPR):\t").append(integerFormat.format(iterationExamples.get(i).size()))
                    .append("\tExamples (All):\t")
                    .append(integerFormat.format(flatProPprExamples(iterationExamples.get(i))));
            description.append("\n");
        }

        return description.toString();
    }

    /**
     * Appends the selected relations to the description.
     *
     * @param description the description
     */
    @SuppressWarnings("HardCodedStringLiteral")
    protected void appendSelectedRelations(StringBuilder description) {
        if (selectedRelations != null && !selectedRelations.isEmpty()) {
            description.append("\t").append("Selected Relations:\n");
            selectedRelations.stream().sorted().forEach(r -> description.append("\t\t - ").append(r).append("\n"));
        }
    }

    /**
     * Calculates the number of atom examples inside the collection of ProPprExamples, since a ProPprExample may
     * contain several atom examples.
     *
     * @param proPprExamples the collection of ProPprExamples
     * @return the number of atom examples
     */
    protected static int flatProPprExamples(Collection<? extends ProPprExample> proPprExamples) {
        return proPprExamples.stream().mapToInt(p -> p.getGroundedQuery().size()).sum();
    }

    @Override
    protected File getStandardOutputFile() {
        return new File(outputDirectory, STDOUT_LOG_FILE_NAME);
    }

    @Override
    protected void buildOutputDirectory(String configFileContent) {
        outputDirectory = new File(outputDirectoryPath,
                                   String.format(OUTPUT_RUN_DIRECTORY, TimeUtils.getCurrentTime()));
    }

}
