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
public enum RevisionLog {

    INITIALIZING_THEORY_REVISION_MANAGER("Initializing TheoryRevisionManager:\t{}"),
    INITIALIZING_REVISION_MANAGER("Initializing RevisionManager:\t{}"),
    INITIALIZING_THEORY_EVALUATOR("Initializing TheoryEvaluator:\t{}"),
    INITIALIZING_REVISION_OPERATOR_SELECTOR("Initializing RevisionOperatorSelector:\t{}"),
    INITIALIZING_REVISION_OPERATOR_EVALUATOR("Initializing RevisionOperatorEvaluator:\t{}"),
    INITIALIZING_REVISION_OPERATOR("Initializing RevisionOperator:\t{}"),
    INITIALIZING_FEATURE_GENERATOR("Initializing FeatureGenerator:\t{}"),

    PERFORMING_OPERATION_ON_EXAMPLES("Performing operation on\t{} examples."),

    FIND_MINIMAL_SAFE_CLAUSES("Finding the minimal safe clauses from the bottom clause."),
    FIND_CLAUSES_OF_SIZE("Finding the clauses, from the bottom clause, of size:\t{}"),
    SKIPPING_COVERED_EXAMPLE("Skipping covered example:\t{}"),

    FOUND_PREDICATES("Number of predicates found among the examples:\t{}"),
    BUILDING_CLAUSE_FROM_PREDICATE_EXAMPLES("Building rule for predicate\t{} and\t{} examples."),
    BUILDING_CLAUSE_FROM_EXAMPLE("Building clause from the example:\t{}"),
    BOTTOM_CLAUSE_SIZE("Bottom clause body size:\t{}"),
    REFINING_RULE("Refining rule:\t{}"),

    PROPOSED_REFINED_RULE("Proposed refined rule:\t{}"),
    ACCEPTING_NEW_BEST_REFINED_CANDIDATE("Accepting new best refined candidate:\t{}"),
    CANDIDATE_CLAUSES("Candidate clauses:\t{}"),
    CANDIDATE_EVALUATION("Candidates evaluation:\t{}"),
    MAKING_SIDE_MOVEMENT_FOR_CANDIDATE("Making side movement for candidate:\t{}"),

    PROPOSED_ADD_RULE("Propose to add the rule:\t{}"),
    PROPOSED_ADD_LITERAL("Propose to add the literal(s):\t{}"),

    PROPOSED_REMOVE_RULE("Propose to remove the rule:\t{}"),
    PROPOSED_REMOVE_LITERAL("Propose to remove the literal:\t{}"),

    ERROR_REVISING_EXAMPLE("Error when revising the example, reason:\t{}"),
    ERROR_EVALUATING_MINIMAL_CLAUSES("No minimal safe clause could be evaluated. There are two possible reasons: " +
                                             "the timeout is too low; or the metric returns the default value for " +
                                             "all" + " evaluations"),

    ERROR_REVISING_THEORY("Error when revising the theory, reason:");

    protected final String message;

    RevisionLog(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
