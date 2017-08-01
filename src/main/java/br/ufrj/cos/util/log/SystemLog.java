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
public enum SystemLog {

    EMPTY(),

    BUILDING_LEARNING_SYSTEM("Build the learning system:\t{}"),
    BUILDING_ENGINE_SYSTEM_TRANSLATOR("Build the engine system translator:\t{}"),

    CREATING_KNOWLEDGE_BASE_WITH_PREDICATE("Creating knowledge base with predicate:\t{}"),
    KNOWLEDGE_BASE_SIZE("Knowledge base size:\t{}"),

    CREATING_THEORY_WITH_PREDICATE("Creating theory with predicate:\t{}"),
    THEORY_SIZE("Theory size:\t{}"),
    READ_CLAUSE_SIZE("Number of read clauses:\t{}"),

    EXAMPLES_SIZE("Number of read iterator lines:\t{}"),

    THEORY_FILE("Theory File:\t{}\n--------------- THEORY FILE " +
                        "---------------\n{}\n--------------- THEORY FILE ---------------"),
    THEORY_CONTENT("\n------------------ THEORY -----------------\n{}\n------------------ THEORY -----------------"),

    ERROR_UPDATING_KNOWLEDGE_BASE_GRAPH("Error updating the knowledge base graph cache for atom {}, reason:");

    protected final String message;

    SystemLog() {
        this.message = "";
    }

    SystemLog(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
