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
import br.ufrj.cos.knowledge.filter.ClausePredicate;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.parser.knowledge.KnowledgeParser;
import br.ufrj.cos.logic.parser.knowledge.ParseException;
import br.ufrj.cos.util.AtomFactory;
import br.ufrj.cos.util.InitializationException;
import com.esotericsoftware.yamlbeans.YamlException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Pattern;

import static br.ufrj.cos.cli.CommandLineOptions.*;
import static br.ufrj.cos.util.LanguageUtils.DEFAULT_INPUT_ENCODE;
import static br.ufrj.cos.util.log.GeneralLog.ERROR_MAIN_PROGRAM;
import static br.ufrj.cos.util.log.GeneralLog.PROGRAM_END;
import static br.ufrj.cos.util.log.SystemLog.CREATING_KNOWLEDGE_BASE_WITH_PREDICATE;

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
    protected List<Collection<? extends Clause>> iterationKnowledge;

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

    }

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        try {
            buildIterationKnowledge();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the clauses from each iteration and loads into the {@link #iterationKnowledge}.
     *
     * @throws FileNotFoundException        if a file is not found.
     * @throws UnsupportedEncodingException if the enconding is not supported
     * @throws ParseException               if a parser error occurs
     */
    protected void buildIterationKnowledge() throws FileNotFoundException, UnsupportedEncodingException,
            ParseException {
        iterationDirectories = getIterationDirectory();
        final String targetFileName = targetRelation + positiveExtension;
        final FilenameFilter relationFilenameFilter = (dir, name) -> name.endsWith(positiveExtension);
        iterationKnowledge = new ArrayList<>(iterationDirectories.length);
        AtomFactory atomFactory = new AtomFactory();
        KnowledgeParser parser;
        BufferedReader reader;
        Set<Clause> clauses;
        File[] relations;
        for (File iteration : iterationDirectories) {
            clauses = new HashSet<>();
            relations = iteration.listFiles(relationFilenameFilter);
            if (relations == null) {
                iterationKnowledge.add(clauses);
                continue;
            }
            for (File relation : relations) {
                if (relation.getName().equals(targetFileName)) { continue; }
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(relation), DEFAULT_INPUT_ENCODE));
                parser = new KnowledgeParser(reader);
                parser.factory = atomFactory;
                parser.parseKnowledgeAppend(clauses);
            }
            iterationKnowledge.add(clauses);
        }
    }

    /**
     * Gets the iteration directories.
     *
     * @return the iteration directories
     */
    protected File[] getIterationDirectory() {
        Pattern pattern = Pattern.compile(iterationPrefix + ITERATION_SUFFIX_PATTERN);
        return new File(dataDirectoryPath).listFiles((dir, name) -> pattern.matcher(name).matches());
    }

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        if (options == null) { options = new Options(); }

        options.addOption(DATA_DIRECTORY.getOption());
        options.addOption(ITERATION_PREFIX.getOption());

        options.addOption(POSITIVE_EXTENSION.getOption());
        options.addOption(NEGATIVE_EXTENSION.getOption());

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

            cli.targetRelation = commandLine.getOptionValue(TARGET_RELATION.getOptionName(), cli.targetRelation);

            cli.outputDirectoryPath = commandLine.getOptionValue(OUTPUT_DIRECTORY.getOptionName());
            return cli;
        } catch (FileNotFoundException | YamlException e) {
            throw new CommandLineInterrogationException(e);
        }
    }

    @Override
    protected void buildKnowledgeBase() throws IllegalAccessException, InstantiationException, FileNotFoundException {
        ClausePredicate predicate = knowledgeBasePredicateClass.newInstance();
        logger.debug(CREATING_KNOWLEDGE_BASE_WITH_PREDICATE.toString(), predicate);
        knowledgeBase = new KnowledgeBase(knowledgeBaseCollectionClass.newInstance(), predicate);
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append("\t").append("Settings:").append("\n");
        description.append("\t").append("Data Directory:\t\t").append(dataDirectoryPath).append("\n");
        description.append("\t").append("Iteration Prefix:\t").append(iterationPrefix).append("\n");
        description.append("\t").append("Positive Extension:\t").append(positiveExtension).append("\n");
        description.append("\t").append("Negative Extension:\t").append(negativeExtension).append("\n");
        description.append("\t").append("Target Relation:\t").append(targetRelation).append("\n");
        description.append("\t").append("Iteration Directories:\n");
        NumberFormat numberFormat = NumberFormat.getIntegerInstance();
        for (int i = 0; i < iterationDirectories.length; i++) {
            description.append("\t\t - ").append(iterationDirectories[i].getName()).append("\t");
            description.append("Size:\t").append(numberFormat.format(iterationKnowledge.get(i).size())).append("\n");
        }
        return description.toString();
    }

}
