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

import br.ufrj.cos.util.*;
import br.ufrj.cos.util.time.TimeUtils;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.apache.commons.cli.*;
import org.apache.commons.codec.digest.DigestUtils;
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

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

import static br.ufrj.cos.util.log.GeneralLog.*;
import static br.ufrj.cos.util.log.ParsingLog.ERROR_PARSING_FAILED;
import static br.ufrj.cos.util.log.ParsingLog.PARSING_INPUT_ARGUMENTS;
import static br.ufrj.cos.util.log.RepositoryInfoLog.*;

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
     * Argument short option PREFIX.
     */
    public static final String ARGUMENTS_SHORT_OPTION_PREFIX = "-";
    /**
     * Argument long option PREFIX.
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
     * The output arguments file name.
     */
    public static final String ARGUMENTS_FILE_NAME = "arguments_%s.txt";
    /**
     * The output configuration file name.
     */
    public static final String CONFIG_FILE_NAME = "configuration_%s.yaml";
    /**
     * The name of the file that logs the output.
     */
    public static final String STDOUT_LOG_FILE_NAME = "output_%s.txt";
    /**
     * The build properties file
     */
    public static final String BUILD_PROPERTIES_FILE = "git.properties";
    /**
     * The default value for boolean properties
     */
    public static final String DEFAULT_BOOLEAN_VALUE = "false";
    /**
     * The has tag property name
     */
    public static final String HAS_TAG_PROPERTY = "hasTag";
    /**
     * The tag property name
     */
    public static final String TAG_PROPERTY = "tag";
    /**
     * The files property name
     */
    public static final String FILES_PROPERTY = "files";
    /**
     * The change files property name
     */
    public static final String CHANGED_FILES_PROPERTY = "changedFiles";
    /**
     * The has untracked files property name
     */
    public static final String HAS_UNTRACKED_FILES_PROPERTY = "hasUntrackedFiles";
    /**
     * The untracked files property name
     */
    public static final String UNTRACKED_FILES_PROPERTY = "untrackedFiles";
    /**
     * The default value for the untracked files property
     */
    public static final String UNTRACKED_FILES_DEFAULT_VALUE = "untracked files";
    /**
     * The revision property name
     */
    public static final String COMMIT_PROPERTY = "commit";
    /**
     * The directory to save the standard output logs in.
     */
    public static final String OUTPUT_STANDARD_DIRECTORY = "STD_OUT";
    /**
     * The suffix of the class name, to suppress.
     */
    public static final String CLASS_NAME_SUPPRESS_SUFFIX = "CLI";
    /**
     * The separator between the class name and the file name.
     */
    public static final String CLASS_NAME_SEPARATOR = "_";
    /**
     * The output run time to be used in the name of some output files.
     */
    public String outputRunTime;
    /**
     * The configuration file path.
     */
    public String configurationFilePath;
    /**
     * The output directory to save the files in.
     */
    public String outputDirectoryPath;
    protected Options options;
    /**
     * The command line arguments.
     */
    protected String[] cliArguments;
    /**
     * The output directory to save the files in.
     */
    protected File outputDirectory;

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
            logger.info(PROGRAM_BEGIN);
            logger.info(main.toString());
            main.run();
        }
    }

    /**
     * The main method. It runs a instance of this class, measuring the running total time and logging it along with
     * the command line arguments and if the program ends normally.
     *
     * @param instance the instance of this class
     * @param logger   the logger of the instance
     * @param args     the command line arguments
     */
    protected static void mainProgram(CommandLineInterface instance, Logger logger, String[] args) {
        Locale.setDefault(new Locale(DEFAULT_LANGUAGE, DEFAULT_COUNTRY));
        final String startingTime = TimeUtils.getLocalizedCurrentTime();
        final long begin = TimeUtils.getNanoTime();
        boolean success = true;
        try {
            CommandLineInterface main = instance.parseOptions(args);
            run(main, args);
        } catch (Exception e) {
            logger.error(ERROR_MAIN_PROGRAM, e);
            success = false;
        } finally {
            long end = TimeUtils.getNanoTime();
            final String endingTime = TimeUtils.getLocalizedCurrentTime();
            logger.fatal(programEndWithDescription(begin, end, startingTime, endingTime, success, args));
        }
        if (!success) { System.exit(1); }
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
    protected <C extends CommandLineInterface> C readYamlFile(CommandLine commandLine,
                                                              Class<C> clazz,
                                                              String defaultConfigurationFile)
            throws FileNotFoundException, YamlException {
        File yamlFile;
        if (commandLine.hasOption(CommandLineOptions.YAML.getOptionName())) {
            yamlFile = new File(commandLine.getOptionValue(CommandLineOptions.YAML.getOptionName()));
        } else if (defaultConfigurationFile == null) {
            throw new FileNotFoundException(ExceptionMessages.ERROR_NO_YAML_FILE.toString());
        } else {
            final URL resource = getClass().getClassLoader().getResource(defaultConfigurationFile);
            if (resource == null) {
                throw new FileNotFoundException(ExceptionMessages.ERROR_FILE_NOT_IN_CLASS_PATH.toString());
            }
            yamlFile = new File(resource.getFile());
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
    @SuppressWarnings("ImplicitDefaultCharsetUsage")
    protected static <C extends CommandLineInterface> C readYamlFile(Class<C> clazz,
                                                                     File yamlFile) throws FileNotFoundException,
            YamlException {
        YamlReader reader = new YamlReader(new FileReader(yamlFile));
        C cli = reader.read(clazz);
        cli.configurationFilePath = yamlFile.getAbsolutePath();
        return cli;
    }

    /**
     * Gets the correspondent {@link File}s from the parsed {@link CommandLine}.
     *
     * @param commandLine  the parsed command line
     * @param optionName   the {@link Option} to get the parsed {@link String}s.
     * @param defaultValue the default value
     * @return the {@link File}s
     */
    protected static String[] getFilesFromOption(CommandLine commandLine, String optionName, String[] defaultValue) {
        if (commandLine.hasOption(optionName)) {
            return commandLine.getOptionValues(optionName);
        }

        return defaultValue;
    }

    @Override
    public void initialize() throws InitializationException {
        logger.debug(INITIALIZING_COMMAND_LINE_INTERFACE.toString(), this.getClass().getName());
    }

    /**
     * Logs the configurations from the configuration file.
     *
     * @throws InitializationException if an error occurs with the file
     */
    protected void saveConfigurations() throws InitializationException {
        try {
            String configFileContent = FileIOUtils.readFileToString(configurationFilePath);
            buildOutputDirectory(configFileContent);
            final File standardOutputFile = getStandardOutputFile();
            File outputStdDirectory = standardOutputFile.getParentFile();
            if (!outputStdDirectory.exists()) {
                //noinspection ResultOfMethodCallIgnored
                outputStdDirectory.mkdirs();
            }
            addAppender(standardOutputFile.getAbsolutePath());
            logCommittedVersion();
            File configurationFile = new File(outputDirectory, getConfigurationFileName());
            String commandLineArguments = formatArgumentsWithOption(cliArguments, CommandLineOptions.YAML.getOption(),
                                                                    configurationFile.getCanonicalPath());
            FileIOUtils.writeStringToFile(commandLineArguments, new File(outputDirectory, getArgumentFileName()));
            FileIOUtils.writeStringToFile(configFileContent, configurationFile);

            logger.info(COMMAND_LINE_ARGUMENTS.toString(),
                        Arrays.stream(cliArguments).collect(Collectors.joining(LanguageUtils.ARGUMENTS_SEPARATOR)));
            logger.info(CONFIGURATION_FILE.toString(), configurationFilePath, configFileContent);
        } catch (IOException e) {
            throw new InitializationException(e);
        }
    }

    /**
     * Gets the standard output file.
     *
     * @return the standard output file
     */
    protected File getStandardOutputFile() {
        return new File(new File(outputDirectory, OUTPUT_STANDARD_DIRECTORY), getOutputLogFileName());
    }

    /**
     * Gets the configuration file name.
     *
     * @return the configuration file name
     */
    public String getConfigurationFileName() {
        return String.format(CONFIG_FILE_NAME, LanguageUtils.formatClassName(this, CLASS_NAME_SUPPRESS_SUFFIX));
    }

    /**
     * Formats an array of command line arguments appending a configuration file at the end.
     *
     * @param arguments    the input arguments
     * @param option       the option to add
     * @param optionValues the value of the option
     * @return the formatted string
     */
    public String formatArgumentsWithOption(String[] arguments, Option option, String... optionValues) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.getClass().getSimpleName());
        stringBuilder.append(LanguageUtils.ARGUMENTS_SEPARATOR);

        consumeArguments(arguments, option, stringBuilder);

        stringBuilder.append(ARGUMENTS_SHORT_OPTION_PREFIX);
        stringBuilder.append(option.getOpt());
        stringBuilder.append(LanguageUtils.ARGUMENTS_SEPARATOR);
        for (String optionValue : optionValues) { stringBuilder.append(optionValue); }
        return stringBuilder.toString().trim();
    }

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
        logger.info(SAVE_STANDARD_OUTPUT.toString(), outputFileName);
    }

    /**
     * Builds the output directory.
     *
     * @param configFileContent the configuration file content
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void buildOutputDirectory(String configFileContent) {
        if (this.outputDirectoryPath != null) {
            outputDirectory = new File(this.outputDirectoryPath);
        } else {
            String suffix = DigestUtils.shaHex(Arrays.deepToString(cliArguments) + configFileContent);
            outputDirectory = new File(LanguageUtils.formatDirectoryName(this, suffix));
        }
        outputDirectory.mkdirs();
    }

    /**
     * Gets the argument file name.
     *
     * @return the argument file name
     */
    public String getArgumentFileName() {
        return String.format(ARGUMENTS_FILE_NAME, LanguageUtils.formatClassName(this, CLASS_NAME_SUPPRESS_SUFFIX));
    }

    /**
     * Logs the committed version.
     */
    protected void logCommittedVersion() {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = getClass().getClassLoader().getResourceAsStream(BUILD_PROPERTIES_FILE);
            prop.load(input);
            logCommit(prop);
            boolean changed;
            changed = isLoggedChangedFiles(prop.getProperty(CHANGED_FILES_PROPERTY, DEFAULT_BOOLEAN_VALUE),
                                           prop.getProperty(FILES_PROPERTY, FILES_PROPERTY),
                                           UNCOMMITTED_FILE.toString());
            changed |= isLoggedChangedFiles(prop.getProperty(HAS_UNTRACKED_FILES_PROPERTY, DEFAULT_BOOLEAN_VALUE),
                                            prop.getProperty(UNTRACKED_FILES_PROPERTY, UNTRACKED_FILES_DEFAULT_VALUE),
                                            UNTRACKED_FILE.toString());
            if (!changed) {
                logger.info(ALL_FILES_COMMITTED.toString());
            }
        } catch (IOException e) {
            logger.error(ERROR_READING_BUILD_PROPERTIES.toString(), e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error(ERROR_READING_BUILD_PROPERTIES.toString(), e);
                }
            }
        }
    }

    /**
     * Formats the name of the output std out file based on the current date and time.
     *
     * @return the name of the output std out file
     */
    protected String getOutputLogFileName() {
        outputRunTime = TimeUtils.getCurrentTime();
        String className = LanguageUtils.formatClassName(this, CLASS_NAME_SUPPRESS_SUFFIX);
        return className + CLASS_NAME_SEPARATOR + String.format(STDOUT_LOG_FILE_NAME, outputRunTime);
    }

    /**
     * Logs the commit
     *
     * @param prop the properties
     */
    protected static void logCommit(Properties prop) {
        String commit = prop.getProperty(COMMIT_PROPERTY);
        boolean hasTag = Boolean.parseBoolean(prop.getProperty(HAS_TAG_PROPERTY, DEFAULT_BOOLEAN_VALUE));
        if (hasTag) {
            logger.info(COMMITTED_VERSION_WITH_TAG.toString(),
                        prop.getProperty(TAG_PROPERTY, TAG_PROPERTY), commit);
        } else {
            logger.info(COMMITTED_VERSION.toString(), commit);
        }
    }

    /**
     * Logs the changed files
     *
     * @param hasProperty the has property value
     * @param property    the property value
     * @param message     the message
     * @return {@code true} if there was(were) changed files.
     */
    protected static boolean isLoggedChangedFiles(String hasProperty, String property, String message) {
        boolean changedFiles = Boolean.parseBoolean(hasProperty);
        if (changedFiles) {
            logger.warn(message, property);
        }

        return changedFiles;
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
            logger.trace(PARSING_INPUT_ARGUMENTS);
            CommandLine commandLine = parser.parse(options, arguments);
            return parseOptions(commandLine);
        } catch (ParseException | CommandLineInterrogationException e) {
            logger.error(ERROR_PARSING_FAILED, e);
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
