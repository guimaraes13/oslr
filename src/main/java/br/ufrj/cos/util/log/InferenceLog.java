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
@SuppressWarnings("JavaDoc")
public enum InferenceLog {

    ANSWERING_QUERY("Answering query:\t{}"),
    NUMBER_OF_QUERY_ANSWERS("Number of answers:\t{}"),
    ANSWER_RESULT_WITH_VALUE("Result:\tP[{}]\t=\t{}\t[not normalized]"),
    ANSWER_STATE_WITH_VALUE("State:\t{},\t{}"),

    EVALUATED_TIMEOUT_PROPORTION("{}% out of {} rules has finished the evaluation within the timeout."),
    EVALUATION_UNDER_METRIC("Evaluation of the theory under the metric:\t{}\t=\t{}"),

    BEGIN_ASYNC_EVALUATION("[ BEGIN ]\tAsynchronous evaluation of {} candidates."),
    SUBMITTING_CANDIDATE("Submitting candidate:\t{}"),
    EVALUATION_FOR_RULE("Evaluation: {}\twith time: {}s\tfor rule:\t{}"),
    END_ASYNC_EVALUATION("[  END  ]\tAsynchronous evaluation."),

    GROUNDING_EXAMPLE("Grounding iterator:\t{}"),
    @SuppressWarnings("unused") GROUNDING_EXAMPLE_TIMEOUT("Grounding iterator {} timed out."),

    EVALUATION_INITIAL_THEORIES("Evaluating the initial {} theory(es)."),
    EVALUATION_THEORIES_OF_SIZE("Evaluating {} theory(es) of size:\t{}"),
    EVALUATION_THEORY_TIMEOUT("Evaluation of the theory timed out after {} seconds."),

    BIGGEST_GAP_THRESHOLD("The biggest gap threshold was:\t{}"),

    ERROR_GROUNDING_EXAMPLE("Error when grounding the example, reason:"),
    @SuppressWarnings("unused") ERROR_BUILDING_ATOM("Error when building an atom, reason:"),
    ERROR_PROVING_GOAL("Could not prove the goal:\t{}"),
    ERROR_EVALUATING_CLAUSE("Error when evaluating the clause, reason:"),
    ERROR_EVALUATING_CANDIDATE_THEORY("Error when evaluating a candidate theory, reason:"),
    ERROR_EVALUATING_REVISION_OPERATOR("Error when evaluating the revision operator, reason:");

    protected final String message;

    InferenceLog(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
