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

    EMPTY(),

    FIND_MINIMAL_SAFE_CLAUSES("Finding the minimal safe clauses from the bottom clause."),
    SKIPPING_COVERED_EXAMPLE("Skipping covered example:\t{}"),

    BUILDING_CLAUSE_FROM_EXAMPLE("Building clause from the example:\t{}"),
    BUILDING_THE_BOTTOM_CLAUSE("Building the bottom clause from the example:\t{}"),
    REFINING_RULE_FROM_EXAMPLE("Refining rule from the example:\t{}"),
    REFINING_RULE("Refining rule :\t{}"),

    PROPOSED_REFINED_RULE("Proposed refined rule:\t{}"),

    ERROR_EVALUATING_MINIMAL_CLAUSES("No minimal safe clause could be evaluated. There are two possible reasons: " +
                                             "the timeout is too low; or the metric returns the default value for " +
                                             "all" + " evaluations"),

    ERROR_REVISING_THEORY("Error when revising the theory, reason:");

    protected final String message;

    RevisionLog() {
        this.message = "";
    }

    RevisionLog(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
