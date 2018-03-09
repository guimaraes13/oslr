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
@SuppressWarnings({"JavaDoc"})
public enum IterationLog {

    EXAMPLE_READ_FROM_ITERATION("Examples read from iteration {}:\t{}"),
    KNOWLEDGE_READ_FROM_ITERATION("Knowledge read from iteration {}:\t{}"),
    NUMBER_OF_RELEVANT_TERM("Number of relevants term for filtering:\t{}"),
    BEGIN_REVISION_ITERATIONS("Begin the revision of {} iteration(s)"),
    REVISING_ITERATION("Revising iteration:\t{}"),
    ADDED_ITERATION_KNOWLEDGE("Added iteration's knowledge:\t{}"),
    BEGIN_REVISION_EXAMPLE("Begin the revision of\t{} example(s)"),
    END_REVISION_EXAMPLE("Ended the revision of the example(s)"),
    BEGIN_TRAINING_REMAINING_EXAMPLES("Begin the training of the \t{} remaining example(s)"),
    END_TRAINING_REMAINING_EXAMPLES("Ended the training of the remaining example(s)"),
    BEGIN_EVALUATION("Begin the evaluation on iteration:\t{}"),
    END_TRAIN_EVALUATION("Ended the train evaluation on iteration:\t{}"),
    END_TEST_EVALUATION("Ended the test evaluation on iteration:\t{}"),
    END_REVISION_ITERATION("Ended the revision of the {} iteration(s)"),
    END_EVALUATION("Ended the evaluation on iteration:\t{}"),
    ITERATION_TRAINING_TIME("Training time of the iteration:\t{}"),
    END_REVISION_ITERATIONS("Ended the revision of the iteration(s)"),
    ITERATION_DATA_SAVED("Iteration theory saved in directory:\t{}"),
    ERROR_WRITING_ITERATION_THEORY_FILE("Error when writing the iteration's theory, reason:\t{}"),
    ERROR_WRITING_ITERATION_INFERENCE_FILE("Error when writing the iteration's inferences, reason:\t{}"),
    ERROR_WRITING_STATISTICS_FILE("Error when writing the statistics to file, reason:\t{}");

    protected final String message;

    IterationLog(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
