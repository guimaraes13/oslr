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

package br.ufrj.cos.util.log;

/**
 * Centralizes log messages from the system.
 * <p>
 * Created on 01/08/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("JavaDoc")
public enum NellConverterLog {

    EMPTY(),

    PROCESSING_FILE("Processing file:\t{}"),
    ITERATION_PREFIX("Iteration prefix:\t{}"),
    PROCESSING_ITERATION("Processing iteration:\t{}"),
    PROCESSING_RELATION("Processing relation:\t{}"),
    PROCESSING_RELATION_ITERATION("Processing relation:\t{} at iteration:\t{}"),

    PROCESSING_FILE_HEADER("\nProcessing file:\t{}\nHeader:\t{}"),
    FOUND_INDEX("Found index for {}:{}{}"),

    FILTERING_ITERATION("Removing examples of iteration {}\tfound in iteration {}."),
    TOTAL_NUMBER_POSITIVES_RATE("Total number of positives:\t{}{}%"),
    TOTAL_NUMBER_NEGATIVES_RATE("Total number of negatives:\t{}{}%"),
    TOTAL_NUMBER_POSITIVES("Total number of positives:\t{}"),
    TOTAL_NUMBER_NEGATIVES("Total number of negatives:\t{}"),
    PROCESSED_NUMBER_EXAMPLES("Processed\t{} of {} \texamples"),
    FILTERING_EXAMPLES("Filtering examples to {} percent"),
    FILE_CONTAINS_LINES("File:\t{}\tContains {} lines"),

    DONE_ITERATION("Done iteration:\t\t\t{}"),
    DONE_RELATION("Done relation:\t\t\t{}"),
    DIRECTORY_1("Directory 1:\t{}"),
    DIRECTORY_2("Directory 2:\t{}"),

    POSITIVE_EXTENSION("Positive extension:\t{}"),
    NEGATIVE_EXTENSION("Negative extension:\t{}"),

    TOTAL_NUMBER_PREDICATES("Total number of predicates:\t{}"),
    TOTAL_NUMBER_EXAMPLES("Total added examples:\t\t{}"),
    TOTAL_NUMBER_EXAMPLES_PROPPR("ProPPR format examples:\t\t{}"),
    TOTAL_SKIPPED_EXAMPLES("Total skipped examples:\t\t{}"),
    TOTAL_REMOVED_EXAMPLES("Total removed examples:\t\t{}. This examples were removed because contradict future ones."),

    ITERATION_SAVING("Saving iteration {} to directory:\t{}"),
    EXAMPLES_SAVING("Saving examples to file:\t{}"),
    ITERATION_SAVED("Iteration {} successfully saved to directory:\t{}"),
    FILE_SAVE("File\t{} saved at\t{}"),

    FILE_HASH_HEADER("-------------------- FILE HASH --------------------"),
    FILE_HASH_FOOTER("-------------------- FILE HASH --------------------"),
    FILE_HASH_AND_SIZE("Hash of file\t{}{} is {}\tNumber of facts:\t{}"),
    FILE_NORMAL_HASH("{} Hash (Normal):\t{}"),
    FILE_ZIPPED_HASH("{} Hash (Zipped):\t{}"),
    FILE_HASH("{}  {}"),
    ITERATION_TOTAL_HASH("The iteration total hash:\t{}"),
    CONFIGURATION_TOTAL_HASH("The total hash of the configuration:\t\t{}"),
    OUTPUT_TOTAL_HASH("The total of the output:\t\t\t\t\t{}"),
    TOTAL_HASH("The total of the output and configuration:\t{}"),

    HASH_DISCLAIMER("\n\nThe logic data set must be the same for two runs, except by the case of hash collision,\n" +
                            "if the all of the following conditions are true:\n\n" +
                            "\t1) The commit hash of both runs are the same;\n" +
                            "\t2) There is no uncommitted/untracked files in both runs;\n" +
                            "\t3) The output and configuration hash of both runs are the same.\n\n" +
                            "It is possible that the output data set is equal for two runs, even if the input are " +
                            "not,\nbecause repeated atoms are ignored in the input.\n");

    protected final String message;

    NellConverterLog() {
        this.message = "";
    }

    NellConverterLog(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
