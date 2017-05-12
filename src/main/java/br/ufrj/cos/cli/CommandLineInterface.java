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

import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.LogMessages;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents classes that implements the main method and so have input options
 * <p>
 * Created on 24/04/17.
 *
 * @author Victor Guimarães
 */
public abstract class CommandLineInterface implements Runnable, Initializable {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * The configuration file path.
     */
    public String configurationFilePath;

    protected Options options;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            logger.info(LogMessages.PROGRAM_BEGIN);
            CommandLineInterface main = new LearningFromFilesCLI();
            main = main.parseOptions(args);
            main.initialize();
            logger.info(main.toString());
            main.run();
        } catch (Exception e) {
            logger.error(LogMessages.ERROR_MAIN_PROGRAM, e);
        } finally {
            logger.fatal(LogMessages.PROGRAM_END);
        }
    }

    /**
     * Parses the command line arguments based on the available {@link Option}s
     *
     * @param arguments the command line arguments
     * @return a new configured {@link CommandLineInterface}
     */
    public CommandLineInterface parseOptions(String[] arguments) {
        CommandLineParser parser = new DefaultParser();
        if (options == null) {
            initializeOptions();
        }
        try {
            logger.trace(LogMessages.PARSING_INPUT_ARGUMENTS);
            CommandLine commandLine = parser.parse(options, arguments);
            return parseOptions(commandLine);
        } catch (ParseException | CommandLineInterrogationException e) {
            logger.error(LogMessages.ERROR_PARSING_FAILED, e);
        }
        return null;
    }

    /**
     * Initializes the {@link Option}s that might be parsed from the input arguments
     */
    protected abstract void initializeOptions();

    /**
     * Method to set the proper fields based on the {@link Option}s parsed from the command line arguments
     *
     * @param commandLine the parsed command line arguments
     * @return a new configured {@link CommandLineInterface}
     * @throws CommandLineInterrogationException semantic error on the command line arguments
     */
    protected abstract CommandLineInterface parseOptions(
            CommandLine commandLine) throws CommandLineInterrogationException;

}
