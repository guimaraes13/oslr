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

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
public class LearningFromFilesCLI extends CommandLineInterface {

    public static final Logger logger = LogManager.getLogger();

    public static final String FILE_NOT_FOUND_MESSAGE = "File %s for %s does not exists.";

    protected File[] knowledgeBaseFiles;
    protected File[] theoryFiles;
    protected File[] exampleFiles;

    protected Set<Option> optionSet;

    public static void main(String[] args) {
        logger.info("Program begin!");
        LearningFromFilesCLI main = new LearningFromFilesCLI();
        String[] arguments = {
                "-h",
                "-k",
                "/Users/Victor/Desktop/ProPPR_Smokers/smokers_test.data"
        };

        main.parseOptions(args);
        logger.fatal("Program end!");
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

        return null;
    }

    protected File[] readPathsToFiles(String paths[], String inputName) throws FileNotFoundException {
        File[] files = new File[paths.length];
        File file;
        for (int i = 0; i < paths.length; i++) {
            file = new File(paths[i]);
            if (file.exists()) {
                files[i] = file;
            } else {
                throw new FileNotFoundException(String.format(FILE_NOT_FOUND_MESSAGE, file.getAbsoluteFile(), inputName));
            }
        }

        return files;
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

}
