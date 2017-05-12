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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * Centralizes all the command line options from the system.
 * <p>
 * Created on 30/04/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings({"deprecation", "AccessStaticViaInstance", "JavaDoc"})
public enum CommandLineOptions {
    YAML(OptionBuilder.withArgName("yaml")
                 .withLongOpt("yaml")
                 .hasArg()
                 .withDescription("the yaml configuration file. The yaml configuration file is used to set all " +
                                          "the options of the system. If it is not provided, the default one will" +
                                          " be used. The command line options override the yaml file.")
                 .create("y")),
    HELP(new Option("h", "help", false, "print this message.")),
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
                     .create("e"));
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
