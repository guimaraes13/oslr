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

package br.ufrj.cos.cli.nell;

import br.ufrj.cos.cli.CommandLineInterface;
import br.ufrj.cos.cli.CommandLineInterrogationException;
import br.ufrj.cos.logic.parser.knowledge.ParseException;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.time.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

import static br.ufrj.cos.cli.CommandLineOptions.*;
import static br.ufrj.cos.cli.CommandLineOptions.ITERATION_PREFIX;
import static br.ufrj.cos.cli.CommandLineOptions.NEGATIVE_EXTENSION;
import static br.ufrj.cos.cli.CommandLineOptions.POSITIVE_EXTENSION;
import static br.ufrj.cos.cli.LearningFromIterationsCLI.getIterationDirectory;
import static br.ufrj.cos.util.log.GeneralLog.TOTAL_PROGRAM_TIME;
import static br.ufrj.cos.util.log.NellConverterLog.*;

/**
 * Class to convert examples from the logic representation to the ProPPR's representation.
 * <p>
 * Created on 04/08/17.
 *
 * @author Victor Guimarães
 */
public class IterationLogicToProPprConverter extends LogicToProPprConverter {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    /**
     * The iteration prefix.
     */
    public String iterationPrefix = NellBaseConverterCLI.DEFAULT_ITERATION_PREFIX;
    /**
     * The target relation to learn the theory.
     */
    public String[] targetRelations;

    /**
     * Filters to remove examples that only has the negative part. If {@code true}, examples with only negative part
     * will be removed from the output.
     */
    public boolean filterOnlyNegativeExamples = false;

    protected File[] iterationDirectories;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new IterationLogicToProPprConverter();
        mainProgram(instance, logger, args);
    }

    @Override
    public void initialize() throws InitializationException {
        numberFormat = NumberFormat.getIntegerInstance();
        iterationDirectories = getIterationDirectory(dataDirectoryPath, iterationPrefix);
        buildOutputDirectory("");
    }

    @Override
    public void run() {
        try {
            long begin = TimeUtils.getNanoTime();
            for (File iteration : iterationDirectories) {
                logger.info(PROCESSING_ITERATION.toString(), iteration.getName());
                for (String targetRelation : targetRelations) {
                    logger.info(PROCESSING_RELATION.toString(), targetRelation);
                    outputDirectory = iteration;
                    convertExamplesFromLogic(iteration, targetRelation);
                    logger.info(EMPTY);
                }
            }
            long end = TimeUtils.getNanoTime();
            logger.warn(TOTAL_PROGRAM_TIME.toString(), TimeUtils.formatNanoDifference(begin, end));
        } catch (IOException | ParseException e) {
            logger.error(ExceptionMessages.GENERAL_ERROR.toString(), e);
        }
    }

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        if (options == null) { options = new Options(); }

        options.addOption(ITERATION_PREFIX.getOption());
        options.addOption(TARGET_RELATIONS.getOption());
    }

    @Override
    public CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        super.parseOptions(commandLine);
        dataDirectoryPath = commandLine.getOptionValue(DATA_DIRECTORY.getOptionName());
        outputDirectoryPath = commandLine.getOptionValue(OUTPUT_DIRECTORY.getOptionName(), dataDirectoryPath);
        iterationPrefix = commandLine.getOptionValue(ITERATION_PREFIX.getOptionName(), iterationPrefix);

        positiveExtension = commandLine.getOptionValue(POSITIVE_EXTENSION.getOptionName(), positiveExtension);
        negativeExtension = commandLine.getOptionValue(NEGATIVE_EXTENSION.getOptionName(), negativeExtension);
        examplesFileExtension = commandLine.getOptionValue(EXAMPLES_EXTENSION.getOptionName(), examplesFileExtension);

        targetRelations = commandLine.getOptionValues(TARGET_RELATIONS.getOptionName());

        if (commandLine.hasOption(FORCE_INDEX.getOptionName())) {
            forceIndex = Integer.parseInt(commandLine.getOptionValue(FORCE_INDEX.getOptionName()));
        }

        filterOnlyNegativeExamples = commandLine.hasOption(FILTER_ONLY_NEGATIVES.getOptionName());

        return this;
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
        if (forceIndex > 0) {
            description.append("\t").append("Force Index:\t\t").append(forceIndex).append("\n");
        }
        description.append("\t").append("Filter Negatives:\t").append(filterOnlyNegativeExamples).append("\n");
        description.append("\t").append("Target Relations:\t").append("\n");
        for (String targetRelation : targetRelations) {
            description.append("\t\t - ").append(targetRelation).append("\n");
        }
        description.append("\t").append("Iteration Directories:\n");
        for (File iterationDirectory : iterationDirectories) {
            description.append("\t\t - ").append(iterationDirectory.getName()).append("\n");
        }
        return description.toString();
    }

}
