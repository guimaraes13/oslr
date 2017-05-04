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
import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.ExampleSet;
import br.ufrj.cos.knowledge.example.ProPprExampleSet;
import br.ufrj.cos.knowledge.filter.ClausePredicate;
import br.ufrj.cos.knowledge.filter.GroundedFactPredicate;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.parser.example.ExampleParser;
import br.ufrj.cos.logic.parser.knowledge.KnowledgeParser;
import br.ufrj.cos.logic.parser.knowledge.ParseException;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.LanguageUtils;
import br.ufrj.cos.util.LogMessages;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("CanBeFinal")
public class LearningFromFilesCLI extends CommandLineInterface implements Runnable {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * The knowledge base collection class.
     */
    public Class<? extends Collection> KNOWLEDGE_BASE_COLLECTION_CLASS = ArrayList.class;
    /**
     * The knowledge base predicate.
     */
    public Class<? extends ClausePredicate> KNOWLEDGE_BASE_PREDICATE = GroundedFactPredicate.class;

    /**
     * The most generic {@link Atom} subclass allowed in the knowledge base.
     */
    public Class<? extends Atom> KNOWLEDGE_BASE_ANCESTRAL_CLASS = Atom.class;

    /**
     * The theory collection class.
     */
    public Class<? extends Collection> THEORY_COLLECTION_CLASS = ArrayList.class;

    /**
     * The theory predicate.
     */
    public Class<? extends ClausePredicate> THEORY_PREDICATE = null;

    /**
     * The most generic {@link HornClause} subclass allowed in the theory.
     */
    public Class<? extends HornClause> THEORY_BASE_ANCESTRAL_CLASS = null;

    /**
     * Argument options.
     */
    protected Set<Option> optionSet;

    //Input fields
    /**
     * Input knowledge base files.
     */
    protected File[] knowledgeBaseFiles;

    /**
     * Input theory files.
     */
    protected File[] theoryFiles;

    /**
     * Input example files.
     */
    protected File[] exampleFiles;

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
    protected ExampleSet examples;

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
     * Parses the {@link File}'s examples and appends they to the correspondent {@link List}.
     *
     * @param file           the {@link File} to parse
     * @param atomExamples   the {@link List} to the ProbLog like examples
     * @param proPprExamples the {@link List} to the ProPPR like examples
     */
    protected static void readExamplesToLists(File file,
                                              List<AtomExample> atomExamples,
                                              List<ProPprExampleSet> proPprExamples) {
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

    @Override
    protected void initializeOptions() {
        buildOptionSet();
        this.options = new Options();

        for (Option option : optionSet) {
            options.addOption(option);
        }
    }

    /**
     * Builds the {@link Set} of {@link Option}s to be parsed from the command line.
     */
    protected void buildOptionSet() {
        optionSet = new HashSet<>();
        optionSet.add(CommandLineOptions.HELP.getOption());
        optionSet.add(CommandLineOptions.KNOWLEDGE_BASE.getOption());
        optionSet.add(CommandLineOptions.THEORY.getOption());
        optionSet.add(CommandLineOptions.EXAMPLES.getOption());
    }

    @Override
    public void parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        try {
            if (commandLine.hasOption(CommandLineOptions.HELP.getOptionName())) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(this.getClass().getSimpleName(), options, true);
                return;
            }

            knowledgeBaseFiles = getFilesFromOption(commandLine, CommandLineOptions.KNOWLEDGE_BASE.getOptionName());
            theoryFiles = getFilesFromOption(commandLine, CommandLineOptions.THEORY.getOptionName());
            exampleFiles = getFilesFromOption(commandLine, CommandLineOptions.EXAMPLES.getOptionName());
        } catch (Exception e) {
            throw new CommandLineInterrogationException(e);
        }
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
            buildKnowledgeBase();
            buildTheory();
            buildExampleSet();
            buildLearningSystem();
            //TODO: call the learning methods
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException
                e) {
            logger.error(LogMessages.ERROR_READING_INPUT_FILES, e);
        }
    }

    /**
     * Builds the {@link KnowledgeBase} from the input files.
     *
     * @throws IllegalAccessException if an error occurs when instantiating a new object by reflection
     * @throws InstantiationException if an error occurs when instantiating a new object by reflection
     */
    protected void buildKnowledgeBase() throws IllegalAccessException, InstantiationException {
        List<Clause> clauses = readInputKnowledge(knowledgeBaseFiles);
        ClausePredicate predicate = KNOWLEDGE_BASE_PREDICATE.newInstance();
        logger.debug(LogMessages.CREATING_KNOWLEDGE_BASE_WITH_PREDICATE.toString(), predicate);

        knowledgeBase = new KnowledgeBase(KNOWLEDGE_BASE_COLLECTION_CLASS.newInstance(), predicate);
        knowledgeBase.addAll(clauses, KNOWLEDGE_BASE_ANCESTRAL_CLASS);
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
        if (THEORY_PREDICATE != null && THEORY_BASE_ANCESTRAL_CLASS != null) {
            predicate = THEORY_PREDICATE.getConstructor(THEORY_BASE_ANCESTRAL_CLASS.getClass())
                    .newInstance(THEORY_BASE_ANCESTRAL_CLASS);
            logger.debug(LogMessages.CREATING_THEORY_WITH_PREDICATE.toString(), predicate);
        }

        theory = new Theory(THEORY_COLLECTION_CLASS.newInstance(), predicate);
        theory.addAll(clauses, THEORY_BASE_ANCESTRAL_CLASS);
        logger.info(LogMessages.THEORY_SIZE.toString(), theory.size());
    }

    /**
     * Builds the {@link ExampleSet} from the input files.
     */
    protected void buildExampleSet() {
        List<InputStream> inputStreams = new ArrayList<>();
        List<AtomExample> atomExamples = new ArrayList<>();
        List<ProPprExampleSet> proPprExamples = new ArrayList<>();
        logger.trace(LogMessages.READING_INPUT_FILES);
        for (File file : exampleFiles) {
            readExamplesToLists(file, atomExamples, proPprExamples);
        }
        logger.info(LogMessages.EXAMPLES_SIZE.toString(), atomExamples.size() + proPprExamples.size());
        examples = new ExampleSet(atomExamples, proPprExamples);
    }

    /**
     * Builds the {@link LearningSystem}.
     */
    protected void buildLearningSystem() {
        logger.info(LogMessages.BUILDING_LEARNING_SYSTEM.toString(), LearningSystem.class.getSimpleName());
        learningSystem = new LearningSystem(knowledgeBase, theory, examples);
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
     * Gets the {@link KnowledgeBase}'s files
     *
     * @return the {@link KnowledgeBase}'s files
     */
    public File[] getKnowledgeBaseFiles() {
        return knowledgeBaseFiles;
    }

    /**
     * Gets the {@link Theory}'s files
     *
     * @return the {@link Theory}'s files
     */
    public File[] getTheoryFiles() {
        return theoryFiles;
    }

    /**
     * Gets the {@link ExampleSet}'s files
     *
     * @return the {@link ExampleSet}'s files
     */
    public File[] getExampleFiles() {
        return exampleFiles;
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
