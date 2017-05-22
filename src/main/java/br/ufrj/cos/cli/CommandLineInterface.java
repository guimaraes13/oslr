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
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;
import br.ufrj.cos.util.LogMessages;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

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
     * Argument short option prefix.
     */
    public static final String ARGUMENTS_SHORT_OPTION_PREFIX = "-";
    /**
     * Argument long option prefix.
     */
    public static final String ARGUMENTS_LONG_OPTION_PREFIX = "--";
    /**
     * If is to append the log file.
     */
    public static final String APPEND_LOG_FILE = "false";
    /**
     * If is to lock the log file.
     */
    public static final String LOCKING_LOG_FILE = "false";
    /**
     * The log file appender's name.
     */
    public static final String FILE_APPENDER_NAME = "FileStd";
    /**
     * If is to immediate flush the log file.
     */
    public static final String IMMEDIATE_FLUSH_LOG_FILE = "true";
    /**
     * If is to ignore exception in the log file.
     */
    public static final String IGNORE_EXCEPTIONS_LOG_FILE = "false";
    /**
     * If is to buffer the log file.
     */
    public static final String BUFFERED_IO_LOG_FILE = "false";
    /**
     * Size of the log file buffer.
     */
    public static final String BUFFER_SIZE_LOG_FILE = "0";
    /**
     * If is to advertise the log file.
     */
    public static final String ADVERTISE_LOG_FILE = "false";
    /**
     * The log file pattern layout
     */
    public static final String PATTERN_LAYOUT = "[ %d{yyy-MM-dd HH:mm:ss.SSS} ] [ %-5level ] [ %logger{1} " +
            "]\t-\t%msg%n%throwable{full}";
    /**
     * The default language for the formatted outputs.
     */
    public static final String DEFAULT_LANGUAGE = "en";
    /**
     * The default country for the formatted outputs.
     */
    public static final String DEFAULT_COUNTRY = "us";
    /**
     * The configuration file path.
     */
    public String configurationFilePath;

    protected Options options;
    /**
     * The command line arguments.
     */
    protected String[] cliArguments;

    /**
     * Adds a file appender to the output file name.
     *
     * @param outputFileName the file name.
     */
    @SuppressWarnings({"unchecked", "deprecation"})
    protected static void addAppender(final String outputFileName) {
        final LoggerContext context = LoggerContext.getContext(false);
        final Configuration config = context.getConfiguration();
        final Layout layout = PatternLayout.createLayout(PATTERN_LAYOUT, null, config, null,
                                                         null, true, true,
                                                         null, null);
        final Appender appender = FileAppender.createAppender(outputFileName, APPEND_LOG_FILE, LOCKING_LOG_FILE,
                                                              FILE_APPENDER_NAME, IMMEDIATE_FLUSH_LOG_FILE,
                                                              IGNORE_EXCEPTIONS_LOG_FILE, BUFFERED_IO_LOG_FILE,
                                                              BUFFER_SIZE_LOG_FILE, layout, null,
                                                              ADVERTISE_LOG_FILE, null, config);
        appender.start();
        config.addAppender(appender);
        final Level level = null;
        final Filter filter = null;
        config.getRootLogger().addAppender(appender, level, filter);
        config.getRootLogger().addAppender(appender, level, filter);
    }

    /**
     * Runs the main program
     *
     * @param main the main program
     * @param args the arguments
     * @throws InitializationException if an error occurs during the initialization of an {@link Initializable}.
     */
    protected static void run(CommandLineInterface main, String[] args) throws InitializationException {
        if (main != null) {
            main.cliArguments = args;
            main.initialize();
            logger.info(LogMessages.PROGRAM_BEGIN);
            logger.info(main.toString());
            main.run();
        }
    }

    /**
     * Reads the yaml configuration file and returns a version of the class from it.
     *
     * @param commandLine              the {@link CommandLine}
     * @param clazz                    the {@link CommandLineInterface} class
     * @param defaultConfigurationFile the default configuration file path
     * @return the read {@link LearningFromFilesCLI}
     * @throws FileNotFoundException if the file does not exists
     * @throws YamlException         if an error occurs when reading the yaml
     */
    protected static <C extends CommandLineInterface> C readYamlFile(CommandLine commandLine,
                                                                     Class<C> clazz,
                                                                     String defaultConfigurationFile)
            throws FileNotFoundException, YamlException {
        File yamlFile;
        if (commandLine.hasOption(CommandLineOptions.YAML.getOptionName())) {
            yamlFile = new File(commandLine.getOptionValue(CommandLineOptions.YAML.getOptionName()));
        } else {
            yamlFile = new File(defaultConfigurationFile);
        }
        return readYamlFile(clazz, yamlFile);
    }

    /**
     * Read the yaml configuration and returns a version of the class from it.
     *
     * @param clazz    the class
     * @param yamlFile the yaml configuration file
     * @return the read {@link LearningFromFilesCLI}
     * @throws FileNotFoundException if the file does not exists
     * @throws YamlException         if an error occurs when reading the yaml
     */
    protected static <C extends CommandLineInterface> C readYamlFile(Class<C> clazz,
                                                                     File yamlFile) throws FileNotFoundException,
            YamlException {
        YamlReader reader = new YamlReader(new FileReader(yamlFile));
        C cli = reader.read(clazz);
        cli.configurationFilePath = yamlFile.getAbsolutePath();
        return cli;
    }

    /**
     * Formats an array of command line arguments appending a configuration file at the end.
     *
     * @param arguments    the input arguments
     * @param option       the option to add
     * @param optionValues the value of the option
     * @return the formatted string
     */
    public static String formatArgumentsWithOption(String[] arguments, Option option, String... optionValues) {
        StringBuilder stringBuilder = new StringBuilder();

        consumeArguments(arguments, option, stringBuilder);

        stringBuilder.append(ARGUMENTS_SHORT_OPTION_PREFIX);
        stringBuilder.append(option.getOpt());
        stringBuilder.append(LanguageUtils.ARGUMENTS_SEPARATOR);
        for (String optionValue : optionValues) { stringBuilder.append(optionValue); }
        return stringBuilder.toString().trim();
    }

    /**
     * Consumes the input arguments skipping the given option.
     *
     * @param arguments     the arguments
     * @param option        the option
     * @param stringBuilder the string builder to append
     */
    protected static void consumeArguments(String[] arguments, Option option, StringBuilder stringBuilder) {
        for (int i = 0; i < arguments.length; ) {
            if (arguments[i].equals(ARGUMENTS_LONG_OPTION_PREFIX + option.getLongOpt())
                    || arguments[i].equals(ARGUMENTS_SHORT_OPTION_PREFIX + option.getOpt())) {
                i++;
                while (i < arguments.length && !arguments[i].startsWith(ARGUMENTS_SHORT_OPTION_PREFIX)) { i++; }
                continue;
            }
            stringBuilder.append(arguments[i]);
            stringBuilder.append(LanguageUtils.ARGUMENTS_SEPARATOR);
            i++;
            while (i < arguments.length && !arguments[i].startsWith(ARGUMENTS_SHORT_OPTION_PREFIX)) {
                stringBuilder.append(arguments[i]);
                stringBuilder.append(LanguageUtils.ARGUMENTS_SEPARATOR);
                i++;
            }
        }
    }

    /**
     * Gets the correspondent {@link File}s from the parsed {@link CommandLine}.
     *
     * @param commandLine the parsed command line
     * @param optionName  the {@link Option} to get the parsed {@link String}s.
     * @return the {@link File}s
     */
    protected static String[] getFilesFromOption(CommandLine commandLine, String optionName) {
        if (commandLine.hasOption(optionName)) {
            return commandLine.getOptionValues(optionName);
        }

        return new String[0];
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
    protected void initializeOptions() {
        options = new Options();
        options.addOption(CommandLineOptions.HELP.getOption());
    }

    /**
     * Method to set the proper fields based on the {@link Option}s parsed from the command line arguments
     *
     * @param commandLine the parsed command line arguments
     * @return a new configured {@link CommandLineInterface}
     * @throws CommandLineInterrogationException semantic error on the command line arguments
     */
    protected CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        if (commandLine.hasOption(CommandLineOptions.HELP.getOptionName())) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(this.getClass().getSimpleName(), options, true);
            System.exit(0);
        }
        return this;
    }

}
