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

package br.ufrj.cos.util;

/**
 * Centralizes all the log messages from the system.
 * <p>
 * Created on 29/04/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("JavaDoc")
public enum LogMessages {
    //No additional parameters to format
    PROGRAM_BEGIN("Program begin!"),
    PROGRAM_END("Program end!"),
    PARSING_INPUT_ARGUMENTS("Parsing input arguments."),
    READING_INPUT_FILES("Reading input file(s)."),

    FIND_MINIMAL_SAFE_CLAUSES("Finding the minimal safe clauses from the bottom clause."),

    ERROR_EVALUATING_MINIMAL_CLAUSES("No minimal safe clause could be evaluated. There are two possible reasons: " +
                                             "the timeout is too low; or the metric returns the default value for " +
                                             "all" + " evaluations"),

    //One additional parameters to format
    BUILDING_LEARNING_SYSTEM("Build the learning system:\t{}"),
    CREATING_KNOWLEDGE_BASE_WITH_PREDICATE("Creating knowledge base with predicate:\t{}"),
    KNOWLEDGE_BASE_SIZE("Knowledge base size:\t{}"),
    THEORY_SIZE("Theory size:\t{}"),
    CREATING_THEORY_WITH_PREDICATE("Creating theory with predicate:\t{}"),
    READ_CLAUSE_SIZE("Number of read clauses:\t{}"),
    EXAMPLES_SIZE("Number of read examples lines:\t{}"),

    BUILDING_CLAUSE_FROM_EXAMPLES("Building a clause from the example:\t{}"), BUILDING_THE_BOTTOM_CLAUSE("Building the bottom clause from the example:\t{}"), REFINING_RULE_FROM_EXAMPLE("Refining rule from the example:\t{}"), EVALUATION_INITIAL_THEORIES("Evaluating the initial {} theory(es)."), EVALUATION_THEORY_TIMEOUT("Evaluation of the theory timed out after {}seconds."), ERROR_EVALUATING_CLAUSE("Error when evaluating the clause, reason:\t{}"),

    ERROR_MAIN_PROGRAM("Main program error, reason:\t{}"),
    ERROR_PARSING_FAILED("Parsing failed, reason:\t{}"),
    ERROR_READING_INPUT_FILES("Error during reading the input files, reason:\t{}"),
    ERROR_REVISING_THEORY("Error when revising the theory, reason:\t{}"),
    ERROR_EVALUATING_CANDIDATE_THEORY("Error when evaluating a candidate theory, reason:\t{}"),

    //Two additional parameters to format
    ERROR_READING_FILE("Error when reading file {}, reason:\t{}"),
    ERROR_UPDATING_KNOWLEDGE_BASE_GRAPH("Error updating the knowledge base graph cache for atom {}, reason:\t{}");

    protected final String message;

    LogMessages(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
