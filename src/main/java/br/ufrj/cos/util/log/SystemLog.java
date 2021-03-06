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
public enum SystemLog {

    BUILDING_LEARNING_SYSTEM("Build the learning system:\t{}"),
    BUILDING_ENGINE_SYSTEM_TRANSLATOR("Build the engine system translator:\t{}"),
    BUILDING_KNOWLEDGE_ITERATIONS("Build the knowledge iterations"),
    BUILDING_EXAMPLES_ITERATIONS("Build the examples iterations"),


    CREATING_KNOWLEDGE_BASE_WITH_PREDICATE("Creating knowledge base with predicate:\t{}"),
    KNOWLEDGE_BASE_SIZE("Knowledge base size:\t{}"),

    CREATING_THEORY_WITH_PREDICATE("Creating theory with predicate:\t{}"),
    THEORY_SIZE("Theory size:\t{}"),
    READ_CLAUSE_SIZE("Number of read clauses:\t{}"),

    EXAMPLES_SIZE("Number of read iterator lines:\t{}"),

    THEORY_FILE("Theory File:\t{}\n--------------- THEORY FILE " +
                        "---------------\n{}\n--------------- THEORY FILE ---------------"),
    THEORY_CONTENT("\n------------------ THEORY -----------------\n{}\n------------------ THEORY -----------------"),

    ERROR_UPDATING_KNOWLEDGE_BASE_GRAPH("Error updating the knowledge base graph cache for atom, reason: {}");

    protected final String message;

    SystemLog(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
