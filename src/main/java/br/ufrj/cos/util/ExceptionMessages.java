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
 * Centralizes all the exception messages from the system.
 * <p>
 * Created on 29/04/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("JavaDoc")
public enum ExceptionMessages {
    //No additional parameters to format
    GENERATED_RULE_NOT_SAVE("Error when generating a new rule, the generate rule can not be made safe."),
    ERROR_DURING_THEORY_COPY("Error when copying the theory."),

    //One additional parameters to format
    ERROR_ANSWERING_QUERY("Error when answering the query {}."),

    //Two additional parameters to format
    FILE_NOT_EXISTS("File {} for {} does not exists.");

    protected final String message;

    ExceptionMessages(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
