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
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

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
    OUTPUT_DIRECTORY(OptionBuilder.withArgName("output")
                             .withLongOpt("outputDirectory")
                             .hasArgs(Option.UNLIMITED_VALUES)
                             .withDescription("the directory to save the files in. If not specified, a new directory," +
                                                      " in the current directory will be created.")
                             .create("o")),
    INPUT_DIRECTORY(OptionBuilder.withArgName("input")
                            .withLongOpt("inputDirectory")
                            .hasArgs(Option.UNLIMITED_VALUES)
                            .withDescription("the directory to read the files from. If not specified, the program " +
                                                     "will try to read it from a yaml configuration file.")
                            .create("i")),
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
    ITERATION_PREFIX(OptionBuilder.withArgName("iterationPrefix")
                             .withLongOpt("iterationPrefix")
                             .hasArg()
                             .withDescription("the iteration prefix, default is (" +
                                                      NellBaseConverterCLI.DEFAULT_ITERATION_PREFIX + ").")
                             .create("p")),
    POSITIVE_EXTENSION(OptionBuilder.withArgName("positiveExtension")
                               .withLongOpt("positiveExtension")
                               .hasArg()
                               .withDescription("the positive relation extension, default is (" +
                                                        NellBaseConverterCLI.DEFAULT_POSITIVE_EXTENSION + ").")
                               .create("pos")),
    NEGATIVE_EXTENSION(OptionBuilder.withArgName("negativeExtension")
                               .withLongOpt("negativeExtension")
                               .hasArg()
                               .withDescription("the negative relation extension, default is (" +
                                                        NellBaseConverterCLI.DEFAULT_NEGATIVE_EXTENSION + ").")
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
                             .create("tr"));
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
