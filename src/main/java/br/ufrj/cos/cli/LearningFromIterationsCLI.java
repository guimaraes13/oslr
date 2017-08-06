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
import com.esotericsoftware.yamlbeans.YamlException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Pattern;

import static br.ufrj.cos.cli.CommandLineOptions.*;
import static br.ufrj.cos.util.LanguageUtils.DEFAULT_INPUT_ENCODE;
import static br.ufrj.cos.util.log.GeneralLog.*;
import static br.ufrj.cos.util.log.SystemLog.*;

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

    protected File[] iterationDirectories;
    protected List<Collection<? extends Atom>> iterationKnowledge;
    protected List<Examples> iterationExamples;
    protected AtomFactory atomFactory;

    protected long begin;
    protected long beginTraining; // begin training
    protected long end;
    protected IterationStatistics iterationStatistics;

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
        beginTraining = TimeUtils.getNanoTime();
        //TODO: revise examples
//        reviseExamples();
        //TODO: save parameters (measuring the time)
        end = TimeUtils.getNanoTime();
        //TODO: load the last data to iterationStatistics
        //TODO: print all the statistics
        //TODO: serialize the statistics to a yaml file
        logger.warn(TOTAL_DISK_IO_TIME.toString(), TimeUtils.formatNanoDifference(begin, beginTraining));
        logger.warn(TOTAL_TRAINING_TIME.toString(), TimeUtils.formatNanoDifference(beginTraining, end));
        logger.warn(TOTAL_PROGRAM_TIME.toString(), TimeUtils.formatNanoDifference(begin, end));
    }

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        try {
            begin = TimeUtils.getNanoTime();
            iterationDirectories = getIterationDirectory(dataDirectoryPath, iterationPrefix);
            build();
            atomFactory = new AtomFactory();
            buildIterationKnowledge();
            buildIterationExamples();
        } catch (IOException | ParseException |
                br.ufrj.cos.logic.parser.example.ParseException | ReflectiveOperationException e) {
            throw new InitializationException(e);
        }
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
        for (File iteration : iterationDirectories) {
            iterationExample = new File(iteration, targetRelation + examplesFileExtension);
            iterationExamples.add(readExamplesFromFile(iterationExample));
        }
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
     * Call the method to revise the examples
     */
    @Override
    protected void reviseExamples() {
        final int numberOfIterations = iterationKnowledge.size();
        iterationStatistics = new IterationStatistics(numberOfIterations);
        for (int i = 0; i < numberOfIterations; i++) {
            iterationStatistics.addIterationKnowledgeSizes(iterationKnowledge.get(i).size());
            iterationStatistics.addIterationExamplesSizes(iterationExamples.get(i).size());
            //TODO: measure the time to add knowledge to the learning system
            learningSystem.addAtomsToKnowledgeBase(iterationKnowledge.get(i));
            //TODO: measure the time to train in the iteration
            for (Example example : iterationExamples.get(i)) {
                learningSystem.incomingExampleManager.incomingExamples(example);
            }
            //TODO: measure the to evaluate the iteration in the train set
            iterationStatistics.addIterationTrainEvaluation(learningSystem.evaluate(iterationExamples.get(i)));
            //TODO: measure the to evaluate the iteration in the test set
            if (i < numberOfIterations - 1) {
                iterationStatistics.addIterationTestEvaluation(learningSystem.evaluate(iterationExamples.get(i + 1)));
            }
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
        NumberFormat numberFormat = NumberFormat.getIntegerInstance();
        for (int i = 0; i < iterationDirectories.length; i++) {
            description.append("\t\t - ").append(iterationDirectories[i].getName());
            description.append("\tSize:\t").append(numberFormat.format(iterationKnowledge.get(i).size()));
            description.append("\tExamples:\t").append(numberFormat.format(iterationExamples.get(i).size()));
            description.append("\n");
        }
        return description.toString();
    }

    /**
     * Loads the knowledge from each iteration, skipping the target relation.
     *
     * @throws ParseException if a error occurs during the parsing
     * @throws IOException    if an I/O error has occurred
     */
    protected void loadKnowledge() throws IOException, ParseException {
        final String targetFileName = targetRelation + positiveExtension;
        final FilenameFilter relationFilenameFilter = (dir, name) -> name.endsWith(positiveExtension);
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
        }
    }

    @Override
    protected void buildTheory() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException, FileNotFoundException {
        theory = new Theory(new HashSet<>());
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
            LanguageUtils.readAtomKnowledgeFromFile(atomFactory, clauses, relation);
        }
    }

}
