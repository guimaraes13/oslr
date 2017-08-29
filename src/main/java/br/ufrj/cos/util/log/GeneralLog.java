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

package br.ufrj.cos.util.log;

import br.ufrj.cos.util.time.TimeUtils;

import java.util.Arrays;

/**
 * Centralizes log messages from the system.
 * <p>
 * Created on 29/04/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("JavaDoc")
public enum GeneralLog {

    PROGRAM_BEGIN("Program begin!"),
    PROGRAM_END("Program end!"),
    COMMAND_LINE_ARGUMENTS("Command line arguments:\t{}"),
    SAVE_STANDARD_OUTPUT("Saving standard output to {}"),
    ERROR_MAIN_PROGRAM("Main program error, reason:"),

    INITIALIZING_COMMAND_LINE_INTERFACE("Initializing CommandLineInterface:\t{}"),
    INITIALIZING_LEARNING_SYSTEM("Initializing LearningSystem:\t{}"),
    @SuppressWarnings("unused") ERROR_INITIALIZING_COMPONENTS("Error when initializing the components, reason:"),
    ERROR_READING_BUILD_PROPERTIES("Error reading build properties, reason:\t{}"),

    TOTAL_INITIALIZATION_TIME("Total initialization time:\t{}"),
    TOTAL_TRAINING_TIME("Total training time:\t\t{}"),
    TOTAL_EVALUATION_TIME("Total evaluation time:\t\t{}"),
    ITERATIONS_KNOWLEDGE_LOAD_TIME("\t- Iteration loading knowledge time:\t{}"),
    ITERATIONS_REVISION_TIME("\t- Iterations revision time:\t\t\t{}"),
    ITERATIONS_EVALUATION_TIME("\t- Iterations evaluation time:\t\t{}"),
    ITERATIONS_SAVING_FILES_TIME("\t- Iteration saving files time:\t\t{}"),

    TOTAL_OUTPUT_TIME("Total output time:\t\t\t{}"),
    TOTAL_PROGRAM_TIME("Total elapsed time:\t\t\t{}"),

    CONFIGURATION_FILE("Configuration File:\t{}\n--------------- CONFIGURATION FILE " +
                               "---------------\n{}\n--------------- CONFIGURATION FILE ---------------"),

    ERROR_READING_CONFIGURATION_FILE("Error when reading the configuration file, reason:"),
    ERROR_WRITING_OUTPUT_FILE("Error when writing the output files, reason:\t{}");

    public static final String USER_DIRECTORY = "user.dir";
    protected final String message;

    GeneralLog(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

    @SuppressWarnings({"HardCodedStringLiteral", "MethodWithTooManyParameters"})
    public static String programEndWithDescription(long begin, long end,
                                                   String startingTime, String endingTime,
                                                   boolean success, String... args) {
        final StringBuilder message = new StringBuilder();
        message.append(PROGRAM_END).append("\n");
        message.append("Current Directory:\t").append(System.getProperty(USER_DIRECTORY)).append("\n");
        if (args != null && args.length > 0) {
            message.append("Arguments:\n");
            Arrays.stream(args).forEach(a -> message.append("\t").append(a).append("\n"));
        }

        message.append("Starting Time:\t").append(startingTime).append("\n");
        message.append("Ending Time:\t").append(endingTime).append("\n");
        message.append("Total time:\t\t").append(TimeUtils.formatNanoDifference(begin, end)).append("\n");
        message.append("Success:\t\t\t").append(success);

        return message.toString().trim();
    }

}
