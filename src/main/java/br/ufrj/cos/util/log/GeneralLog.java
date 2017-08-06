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
    ERROR_MAIN_PROGRAM("Main program error, reason:"),
    COMMAND_LINE_ARGUMENTS("Command line arguments:\t{}"),
    SAVE_STANDARD_OUTPUT("Saving standard output to {}"),

    ERROR_INITIALIZING_COMPONENTS("Error when initializing the components, reason:"),
    ERROR_READING_BUILD_PROPERTIES("Error reading build properties, reason:\t{}"),
    TOTAL_DISK_IO_TIME("Total Disk IO time:\t\t{}"),
    TOTAL_TRAINING_TIME("Total training time:\t{}"),
    TOTAL_PROGRAM_TIME("Total elapsed time:\t\t{}"),

    CONFIGURATION_FILE("Configuration File:\t{}\n--------------- CONFIGURATION FILE " +
                               "---------------\n{}\n--------------- CONFIGURATION FILE ---------------"),

    ERROR_READING_CONFIGURATION_FILE("Error when reading the configuration file, reason:");

    protected final String message;

    GeneralLog(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
