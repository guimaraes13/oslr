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

import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.ExampleSet;
import br.ufrj.cos.knowledge.example.ProPprExampleSet;
import br.ufrj.cos.knowledge.filter.BaseAncestralPredicate;
import br.ufrj.cos.knowledge.filter.ClausePredicate;
import br.ufrj.cos.knowledge.filter.GroundedFactPredicate;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.parser.example.ExampleParser;
import br.ufrj.cos.logic.parser.knowledge.KnowledgeParser;
import br.ufrj.cos.logic.parser.knowledge.ParseException;
import br.ufrj.cos.util.LanguageUtils;
import org.apache.commons.cli.*;
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
public class LearningFromFilesCLI extends CommandLineInterface implements Runnable {

    public static final Logger logger = LogManager.getLogger();

    public static final Class<? extends Collection> KNOWLEDGE_BASE_COLLECTION_CLASS = ArrayList.class;
    public static final Class<? extends ClausePredicate> KNOWLEDGE_BASE_PREDICATE = GroundedFactPredicate.class;

    public static final Class<? extends Collection> THEORY_COLLECTION_CLASS = ArrayList.class;
    public static final Class<? extends ClausePredicate> THEORY_PREDICATE = BaseAncestralPredicate.class;
    public static final Class<? extends HornClause> THEORY_BASE_ANCESTRAL_CLASS = HornClause.class;

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
    KnowledgeBase knowledgeBase;

    /**
     * Theory representation.
     */
    Theory theory;

    /**
     * Examples representation.
     */
    ExampleSet examples;

    protected static void readClausesToList(File file, List<Clause> clauses) {
        try {
            BufferedReader reader;
            KnowledgeParser parser;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), LanguageUtils.DEFAULT_INPUT_ENCODE));
            parser = new KnowledgeParser(reader);
            parser.parseKnowledgeAppend(clauses);
        } catch (UnsupportedEncodingException | FileNotFoundException | ParseException e) {
            logger.error("Error when reading file {}:\t{}", file.getAbsoluteFile(), e);
        }
    }

    protected static void readExamplesToLists(File file,
                                              List<Example> probLogExamples,
                                              List<ProPprExampleSet> proPprExamples) {
        try {
            BufferedReader reader;
            ExampleParser parser;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), LanguageUtils.DEFAULT_INPUT_ENCODE));
            parser = new ExampleParser(reader);
            parser.parseExamplesAppend(probLogExamples, proPprExamples);
        } catch (UnsupportedEncodingException | FileNotFoundException | br.ufrj.cos.logic.parser.example.ParseException e) {
            logger.error("Error when reading file {}:\t{}", file.getAbsoluteFile(), e);
        }
    }

    public static void main(String[] args) {
        try {
            logger.info("Program begin!");
            LearningFromFilesCLI main = new LearningFromFilesCLI();
            main.parseOptions(args);

            logger.info(main.toString());
            main.run();
        } catch (Exception e) {
            logger.error("Main program error:\t", e);
        } finally {
            logger.fatal("Program end!");
        }
    }

    @SuppressWarnings({"deprecation", "AccessStaticViaInstance"})
    protected void buildOptionSet() {
        optionSet = new HashSet<>();
        optionSet.add(new Option("h", "help", false, "print this message"));
        optionSet.add(OptionBuilder.withArgName("base")
                              .withLongOpt("knowledgeBase")
                              .hasArgs(Option.UNLIMITED_VALUES)
                              .withDescription("the input knowledge base file(s)")
                              .create("k"));

        optionSet.add(OptionBuilder.withArgName("theory")
                              .withLongOpt("theory")
                              .hasArgs(Option.UNLIMITED_VALUES)
                              .withDescription("the input theory file(s)")
                              .create("t"));

        optionSet.add(OptionBuilder.withArgName("examples")
                              .withLongOpt("example")
                              .hasArgs(Option.UNLIMITED_VALUES)
                              .withDescription("the input example file(s)")
                              .create("e"));
    }

    @Override
    protected void initializeOptions() {
        buildOptionSet();
        this.options = new Options();

        for (Option option : optionSet) {
            options.addOption(option);
        }
    }

    @Override
    public void parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        try {
            if (commandLine.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(this.getClass().getSimpleName(), options, true);
                return;
            }

            knowledgeBaseFiles = getFilesFromOption(commandLine, "k");
            theoryFiles = getFilesFromOption(commandLine, "t");
            exampleFiles = getFilesFromOption(commandLine, "e");
        } catch (Exception e) {
            throw new CommandLineInterrogationException(e);
        }
    }

    protected File[] getFilesFromOption(CommandLine commandLine, String optionName) throws FileNotFoundException {
        if (commandLine.hasOption(optionName)) {
            return readPathsToFiles(commandLine.getOptionValues(optionName), options.getOption(optionName).getLongOpt());
        }

        return new File[0];
    }

    protected File[] readPathsToFiles(String paths[], String inputName) throws FileNotFoundException {
        File[] files = new File[paths.length];
        File file;
        for (int i = 0; i < paths.length; i++) {
            file = new File(paths[i]);
            if (file.exists()) {
                files[i] = file;
            } else {
                throw new FileNotFoundException("File " + file.getAbsoluteFile() + " for " + inputName +
                                                        " does not exists.");
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
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            logger.error("Error during reading the input files:\t", e);
        }
    }

    protected void buildKnowledgeBase() throws IllegalAccessException, InstantiationException {
        List<Clause> clauses = readInputKnowledge(knowledgeBaseFiles);
        ClausePredicate predicate = KNOWLEDGE_BASE_PREDICATE.newInstance();
        logger.debug("Creating knowledge base with predicate:\t{}", predicate);

        knowledgeBase = new KnowledgeBase(KNOWLEDGE_BASE_COLLECTION_CLASS.newInstance(), predicate);
        knowledgeBase.addAll(clauses);
        logger.info("Knowledge base size:\t{}", knowledgeBase.size());
    }

    protected void buildTheory() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        List<Clause> clauses = readInputKnowledge(theoryFiles);
        ClausePredicate predicate = THEORY_PREDICATE.getConstructor(THEORY_BASE_ANCESTRAL_CLASS.getClass())
                .newInstance(THEORY_BASE_ANCESTRAL_CLASS);
        logger.debug("Creating theory with predicate:\t{}", predicate);

        theory = new Theory(THEORY_COLLECTION_CLASS.newInstance(), predicate);
        theory.addAll(clauses, THEORY_BASE_ANCESTRAL_CLASS);
        logger.info("Theory size:\t{}", theory.size());
    }

    protected void buildExampleSet() {
        List<InputStream> inputStreams = new ArrayList<>();
        List<Example> probLogExamples = new ArrayList<>();
        List<ProPprExampleSet> proPprExamples = new ArrayList<>();
        logger.trace("Reading input file(s).");
        for (File file : exampleFiles) {
            readExamplesToLists(file, probLogExamples, proPprExamples);
        }
        logger.info("Number of read examples lines:\t{}", probLogExamples.size() + proPprExamples.size());
        examples = new ExampleSet(probLogExamples, proPprExamples);
    }

    protected List<Clause> readInputKnowledge(File[] inputFiles) {
        List<InputStream> inputStreams = new ArrayList<>();
        List<Clause> clauses = new ArrayList<>();
        logger.trace("Reading input file(s).");
        for (File file : inputFiles) {
            readClausesToList(file, clauses);
        }
        logger.debug("Number of read clauses:\t{}", clauses.size());
        return clauses;
    }

    public File[] getKnowledgeBaseFiles() {
        return knowledgeBaseFiles;
    }

    public void setKnowledgeBaseFiles(File[] knowledgeBaseFiles) {
        this.knowledgeBaseFiles = knowledgeBaseFiles;
    }

    public File[] getTheoryFiles() {
        return theoryFiles;
    }

    public void setTheoryFiles(File[] theoryFiles) {
        this.theoryFiles = theoryFiles;
    }

    public File[] getExampleFiles() {
        return exampleFiles;
    }

    public void setExampleFiles(File[] exampleFiles) {
        this.exampleFiles = exampleFiles;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\t");
        stringBuilder.append("Settings:");
        stringBuilder.append("\n");

        stringBuilder.append("\t");
        stringBuilder.append("Knowledge base files:\t");
        stringBuilder.append(Arrays.deepToString(knowledgeBaseFiles));
        stringBuilder.append("\n");

        stringBuilder.append("\t");
        stringBuilder.append("Theory files:\t\t\t");
        stringBuilder.append(Arrays.deepToString(theoryFiles));
        stringBuilder.append("\n");

        stringBuilder.append("\t");
        stringBuilder.append("Example files:\t\t\t");
        stringBuilder.append(Arrays.deepToString(exampleFiles));

        return stringBuilder.toString().trim();
    }
}
