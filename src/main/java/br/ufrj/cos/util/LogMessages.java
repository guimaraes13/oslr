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

    ERROR_READING_CONFIGURATION_FILE("Error when reading the configuration file, reason:"),
    ERROR_INITIALIZING_COMPONENTS("Error when initializing the components, reason:"),
    ERROR_GROUNDING_EXAMPLE("Error when grounding the example, reason:"),
    //    ERROR_BUILDING_ATOM("Error when building an atom, reason:"),
    ERROR_EVALUATING_CLAUSE("Error when evaluating the clause, reason:"),
    ERROR_EVALUATING_REVISION_OPERATOR("Error when evaluating the revision operator, reason:"),

    ERROR_MAIN_PROGRAM("Main program error, reason:"),
    ERROR_PARSING_FAILED("Parsing failed, reason:"),
    ERROR_READING_INPUT_FILES("Error during reading the input files, reason:"),
    ERROR_REVISING_THEORY("Error when revising the theory, reason:"),
    ERROR_EVALUATING_CANDIDATE_THEORY("Error when evaluating a candidate theory, reason:"),

    BEGIN_ASYNC_EVALUATION("[ BEGIN ]\tAsynchronous evaluation of {} candidates."),
    END_ASYNC_EVALUATION("[  END  ]\tAsynchronous evaluation."),

    //One additional parameters to format
    CALLING_REVISION_ON_EXAMPLES("Calling the revision on {} examples."),
    RULE_APPENDED_TO_THEORY("Rule appended to the theory:\t{}"),

    ERROR_PROVING_GOAL("Could not prove the goal:\t{}"),
    COMMAND_LINE_ARGUMENTS("Command line arguments:\t{}"),

    BUILDING_LEARNING_SYSTEM("Build the learning system:\t{}"),
    BUILDING_ENGINE_SYSTEM_TRANSLATOR("Build the engine system translator:\t{}"),
    CREATING_KNOWLEDGE_BASE_WITH_PREDICATE("Creating knowledge base with predicate:\t{}"),
    KNOWLEDGE_BASE_SIZE("Knowledge base size:\t{}"),
    THEORY_SIZE("Theory size:\t{}"),
    CREATING_THEORY_WITH_PREDICATE("Creating theory with predicate:\t{}"),
    READ_CLAUSE_SIZE("Number of read clauses:\t{}"),
    EXAMPLES_SIZE("Number of read iterator lines:\t{}"),

    GROUNDING_EXAMPLE("Grounding iterator:\t{}"),
    ERROR_READING_BUILD_PROPERTIES("Error reading build properties, reason:\t{}"),
    //    GROUNDING_EXAMPLE_TIMEOUT("Grounding iterator {} timed out."),

    SKIPPING_COVERED_EXAMPLE("Skipping covered example:\t{}"),
    BUILDING_CLAUSE_FROM_EXAMPLE("Building clause from the example:\t{}"),
    BUILDING_THE_BOTTOM_CLAUSE("Building the bottom clause from the example:\t{}"),
    REFINING_RULE_FROM_EXAMPLE("Refining rule from the example:\t{}"),
    EVALUATION_INITIAL_THEORIES("Evaluating the initial {} theory(es)."),
    EVALUATION_THEORY_TIMEOUT("Evaluation of the theory timed out after {} seconds."),
    TOTAL_PROGRAM_TIME("Total elapsed time:\t{}"),
    ERROR_INITIALIZING_REVISION_EXAMPLES("Error initializing revision examples, using {}."),
    //    BUILDING_CLAUSE_FROM_EXAMPLE("Building a clause from the example:\t{}"),

    CONFIGURATION_FILE("Configuration File:\t{}\n--------------- CONFIGURATION FILE " +
                               "---------------\n{}\n--------------- CONFIGURATION FILE ---------------"),
    THEORY_FILE("Theory File:\t{}\n--------------- THEORY FILE " +
                        "---------------\n{}\n--------------- THEORY FILE ---------------"),
    THEORY_CONTENT("\n------------------ THEORY -----------------\n{}\n------------------ THEORY -----------------"),

    ANSWERING_QUERY("Answering query:\t{}"),
    NUMBER_OF_QUERY_ANSWERS("Number of answers:\t{}"),

    ERROR_READING_FILE("Error when reading file {}, reason:"),
    ERROR_UPDATING_KNOWLEDGE_BASE_GRAPH("Error updating the knowledge base graph cache for atom {}, reason:"),

    //Two additional parameters to format
    COMMITTED_VERSION("This run is based on the commit of hash {}."),
    COMMITTED_VERSION_WITH_TAG("This run is based on the commit of tag {} and hash {}."),
    ALL_FILES_COMMITTED("There is no file uncommitted or untracked during this running."),
    UNCOMMITTED_FILE("The following file(s) was(were) not committed during this running:\t{}"),
    UNTRACKED_FILE("The following file(s) was(were) not tracked during this running:\t\t{}"),
    EVALUATED_TIMEOUT_PROPORTION("{}% out of {} rules has finished the evaluation within the timeout."),
    EVALUATION_UNDER_METRIC("Evaluation of the theory under the metric:\t{}\t=\t{}"),
    ANSWER_RESULT_WITH_VALUE("Result:\tP[{}]\t=\t{}\t[not normalized]"),
    ANSWER_STATE_WITH_VALUE("State:\t{},\t{}"),
    //    SAMPLING_FROM_TARGETS("Sampled {} examples out of {} targets."),
    THEORY_MODIFICATION_SKIPPED("Theory modification skipped due no significant improvement. Improvement of {}, " +
                                        "threshold of {}."),
    THEORY_MODIFICATION_ACCEPTED("Theory modification accepted. Improvement of {}, threshold of {}.");

    protected final String message;

    LogMessages(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
