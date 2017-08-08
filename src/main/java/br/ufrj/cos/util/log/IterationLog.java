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
 * Created on 01/08/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings({"JavaDoc"})
public enum IterationLog {

    BEGIN_REVISION_ITERATIONS("Begin the revision of {} iteration(s)"),
    REVISING_ITERATION("Revising iteration:\t{}"),
    ADDED_ITERATION_KNOWLEDGE("Added iteration's knowledge:\t{}"),
    BEGIN_REVISION_EXAMPLE("Begin the revision of\t{} example(s)"),
    END_REVISION_EXAMPLE("Ended the revision of the example(s)"),
    BEGIN_EVALUATION("Begin the evaluation on iteration:\t{}"),
    END_TRAIN_EVALUATION("Ended the train evaluation on iteration:\t{}"),
    END_TEST_EVALUATION("Ended the test evaluation on iteration:\t{}"),
    END_REVISION_ITERATION("Ended the revision of the {} iteration(s)"),
    END_EVALUATION("Ended the evaluation on iteration:\t{}"),
    ITERATION_TRAINING_TIME("Training time of the iteration:\t{}"),
    END_REVISION_ITERATIONS("Ended the revision of the iteration(s)"),
    ITERATION_DATA_SAVED("Iteration theory saved in directory:\t{}"),
    ERROR_WRITING_ITERATION_THEORY_FILE("Error when writing the iteration's theory, reason:\t{}"),
    ERROR_WRITING_ITERATION_INFERENCE_FILE("Error when writing the iteration's inferences, reason:\t{}");

    protected final String message;

    IterationLog(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
