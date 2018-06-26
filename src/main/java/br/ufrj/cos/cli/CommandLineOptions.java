/*
 * Online Structure Learner by Revision (OSLR) is an online relational
 * learning algorithm that can handle continuous, open-ended
 * streams of relational examples as they arrive. We employ
 * techniques from theory revision to take advantage of the already
 * acquired knowledge as a starting point, find where it should be
 * modified to cope with the new examples, and automatically update it.
 * We rely on the Hoeffding's bound statistical theory to decide if the
 * model must in fact be updated accordingly to the new examples.
 * The system is built upon ProPPR statistical relational language to
 * describe the induced models, aiming at contemplating the uncertainty
 * inherent to real data.
 *
 * Copyright (C) 2017-2018 Victor Guimarães
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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import static br.ufrj.cos.cli.nell.NellBaseConverterCLI.*;
import static br.ufrj.cos.cli.util.EvaluateCrossValidationCLI.DEFAULT_FOLD_PREFIX;

/**
 * Centralizes all the command line options from the system.
 * <p>
 * Created on 30/04/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings({"deprecation", "AccessStaticViaInstance", "JavaDoc", "HardCodedStringLiteral"})
public enum CommandLineOptions {
    HELP(new Option("h", "help", false, "print this message.")),
    YAML(OptionBuilder.withArgName("yaml")
                 .withLongOpt("yaml")
                 .hasArg()
                 .withDescription("the yaml configuration file. The yaml configuration file is used to set all " +
                                          "the options of the system. If it is not provided, the default one will" +
                                          " be used. The command line options override the yaml file.")
                 .create("y")),
    KNOWLEDGE_BASE(OptionBuilder.withArgName("base")
                           .withLongOpt("knowledgeBase")
                           .hasArgs(Option.UNLIMITED_VALUES)
                           .withDescription("the input knowledge base file(s).")
                           .create("k")),
    THEORY(OptionBuilder.withArgName("theory")
                   .withLongOpt("theory")
                   .hasArgs(Option.UNLIMITED_VALUES)
                   .withDescription("the input theory file(s).")
                   .create("t")),
    EXAMPLES(OptionBuilder.withArgName("examples")
                     .withLongOpt("example")
                     .hasArgs(Option.UNLIMITED_VALUES)
                     .withDescription("the input example file(s).")
                     .create("e")),
    QUERIES(OptionBuilder.withArgName("queries")
                    .withLongOpt("queries")
                    .hasArgs(Option.UNLIMITED_VALUES)
                    .withDescription("the input query file(s).")
                    .create("q")),
    WORKING_DIRECTORY(OptionBuilder.withArgName("workingDirectory")
                              .withLongOpt("workingDirectory")
                              .hasArg()
                              .withDescription("the working directory with the logic engine parameter file(s).")
                              .create("w")),
    PROPPR_ALPHA_PARAMETER(OptionBuilder.withArgName("alpha")
                                   .withLongOpt("alpha")
                                   .hasArg()
                                   .withDescription("sets the alpha parameter of the ProPPR (default: 0.1).")
                                   .create("a")),
    PROPPR_EPSILON_PARAMETER(OptionBuilder.withArgName("epsilon")
                                     .withLongOpt("epsilon")
                                     .hasArg()
                                     .withDescription("sets the epsilon parameter of the ProPPR (default: 1e-4).")
                                     .create("e")),
    PROPPR_MAX_DEPTH_PARAMETER(OptionBuilder.withArgName("maxDepth")
                                       .withLongOpt("maxDepth")
                                       .hasArg()
                                       .withDescription("sets the max depth parameter of the ProPPR (default: 20).")
                                       .create("d")),
    INDIVIDUALLY_INFERENCE(OptionBuilder.withLongOpt("individuallyInference")
                                   .withDescription("makes the inference one query at a time.")
                                   .create("ind")),
    TEST(OptionBuilder.withArgName("test")
                 .withLongOpt("test")
                 .hasArgs(Option.UNLIMITED_VALUES)
                 .withDescription("the test example file(s).")
                 .create("test")),
    OUTPUT_DIRECTORY(OptionBuilder.withArgName("output")
                             .withLongOpt("outputDirectory")
                             .hasArg()
                             .withDescription("the directory to save the files in. If not specified, a new directory," +
                                                      " in the current directory will be created." +
                                                      " This option creates a folder inside the output directory with" +
                                                      " the name of the relation and the starting time of the run.")
                             .create("o")),
    STRICT_OUTPUT_DIRECTORY(OptionBuilder.withArgName("strictOutput")
                                    .withLongOpt("strictOutput")
                                    .withDescription("if set, the output will be saved strict to the output " +
                                                             "directory, " +
                                                             "without creating any folder. Careful, this option might" +
                                                             " " +
                                                             "override files in a consecutive run.")
                                    .create("so")),
    INPUT_DIRECTORY(OptionBuilder.withArgName("input")
                            .withLongOpt("inputDirectory")
                            .hasArgs(Option.UNLIMITED_VALUES)
                            .withDescription("the directory to read the files from. If not specified, the program " +
                                                     "will try to read it from a yaml configuration file.")
                            .create("i")),
    INPUT_FILES(OptionBuilder.withArgName("input")
                        .withLongOpt("inputFiles")
                        .hasArgs(Option.UNLIMITED_VALUES)
                        .withDescription("the input files.")
                        .create("i")),
    OUTPUT_FILE(OptionBuilder.withArgName("output")
                        .withLongOpt("outputFile")
                        .hasArg()
                        .withDescription("the output file.")
                        .create("o")),
    OUTPUT_FILES(OptionBuilder.withArgName("output")
                         .withLongOpt("outputFiles")
                         .hasArg()
                         .withDescription("the output file.")
                         .create("o")),
    INPUT_DIRECTORIES(OptionBuilder.withArgName("input")
                              .withLongOpt("inputDirectories")
                              .hasArgs(2)
                              .withDescription("the directories to read from.")
                              .create("i")),
    DATA_DIRECTORY(OptionBuilder.withArgName("dataDirectory")
                           .withLongOpt("dataDirectory")
                           .hasArg()
                           .withDescription("the directory of the iterations data. If not specified, the program " +
                                                    "will try to read it from a yaml configuration file.")
                           .create("d")),
    PERCENTAGE_FILTER(OptionBuilder.withArgName("percentageFilter")
                              .withLongOpt("percentageFilter")
                              .hasArg()
                              .withDescription("Filters at random, the elements of the initial collection of " +
                                                       "examples, " +
                                                       "returning only the percentage defined by this option.")
                              .create("f")),
    DEPTH(OptionBuilder.withArgName("relevantsDepth")
                  .withLongOpt("relevantsDepth")
                  .hasArg()
                  .withDescription("The relevantsDepth of the knowledge base.")
                  .create("d")),
    ITERATION_PREFIX(OptionBuilder.withArgName("iterationPrefix")
                             .withLongOpt("iterationPrefix")
                             .hasArg()
                             .withDescription("the iteration prefix, default is (" + DEFAULT_ITERATION_PREFIX + ").")
                             .create("p")),
    FOLD_PREFIX(OptionBuilder.withArgName("foldPrefix")
                        .withLongOpt("foldPrefix")
                        .hasArg()
                        .withDescription("the fold prefix, default is (" + DEFAULT_FOLD_PREFIX + ").")
                        .create("p")),
    POSITIVE_EXTENSION(OptionBuilder.withArgName("positiveExtension")
                               .withLongOpt("positiveExtension")
                               .hasArg()
                               .withDescription("the positive relation extension, default is (" +
                                                        DEFAULT_POSITIVE_EXTENSION + ").")
                               .create("pos")),
    NEGATIVE_EXTENSION(OptionBuilder.withArgName("negativeExtension")
                               .withLongOpt("negativeExtension")
                               .hasArg()
                               .withDescription("the negative relation extension, default is (" +
                                                        DEFAULT_NEGATIVE_EXTENSION + ").")
                               .create("neg")),
    EXAMPLES_EXTENSION(OptionBuilder.withArgName("examplesExtension")
                               .withLongOpt("examplesExtension")
                               .hasArg()
                               .withDescription("the examples file extension, default is (" +
                                                        LearningFromIterationsCLI.DEFAULT_EXAMPLES_FILE_EXTENSION +
                                                        ").")
                               .create("ext")),
    TARGET_RELATION(OptionBuilder.withArgName("targetRelation")
                            .withLongOpt("targetRelation")
                            .hasArg()
                            .withDescription("the target relation to learn the theory. If not specified, the program " +
                                                     "will try to read it from a yaml configuration file.")
                            .create("tr")),
    TARGET_RELATIONS(OptionBuilder.withArgName("targetRelations")
                             .withLongOpt("targetRelations")
                             .hasArgs(Option.UNLIMITED_VALUES)
                             .withDescription("the target relation to learn the theory. If not specified, the program" +
                                                      " " +
                                                      "will try to read it from a yaml configuration file.")
                             .create("tr")),
    FORCE_INDEX(OptionBuilder.withArgName("forceIndex")
                        .withLongOpt("forceIndex")
                        .hasArg()
                        .withDescription("the index of the term to become a variable.")
                        .create("ind")),
    FILTER_ONLY_NEGATIVES(OptionBuilder.withArgName("filterOnlyNegatives")
                                  .withLongOpt("filterOnlyNegatives")
                                  .withDescription("If set, removes the examples that has only the negative part.")
                                  .create("fon")),
    NEGATIVES_PORTION_FILTER(OptionBuilder.withArgName("negativePortionFilter")
                                     .withLongOpt("negativePortionFilter")
                                     .hasArg()
                                     .withDescription("If there are more negatives examples than positive ones, " +
                                                              "filter " +
                                                              "the negatives such that the number of negatives become" +
                                                              " " +
                                                              "<negativePortionFilter> times the number of positives," +
                                                              " " +
                                                              "approximately.\nSetting this options automatically " +
                                                              "sets " +
                                                              "the " + FILTER_ONLY_NEGATIVES.getOptionName() + " " +
                                                              "option.")
                                     .create("npf")),
    NO_RANDOM_SEED(OptionBuilder.withArgName("noRandomSeed")
                           .withLongOpt("noRandomSeed")
                           .withDescription("If set, no random seed will be used and the random generator will use " +
                                                    "its own initialization. " +
                                                    "In general it is preferred to get better random result but " +
                                                    "without reproducibility.")
                           .create("nrs")),
    SHUFFLE(OptionBuilder.withArgName("shuffle")
                    .withLongOpt("shuffle")
                    .withDescription("If set, the output data.")
                    .create("shf"));
    protected final Option option;

    CommandLineOptions(Option option) {
        this.option = option;
    }

    public Option getOption() {
        return option;
    }

    public String getOptionName() {
        return option.getOpt();
    }

}
