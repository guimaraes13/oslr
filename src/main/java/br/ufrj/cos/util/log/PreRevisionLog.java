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
public enum PreRevisionLog {

    CALLING_REVISION_ON_EXAMPLES("Calling the revision on {} examples."),
    TRY_REFINE_RULE("Trying to refine rule:\t{}"),
    SELECTED_OPERATOR("Operator selected for revision:\t{}"),

    RULE_PROPOSED_TO_THEORY("Rule proposed to be add to the theory:\t{}"),
    RULE_APPENDED_TO_THEORY("Rule appended to the theory:\t{}"),

    ERROR_INITIALIZING_REVISION_EXAMPLES("Error initializing revision examples, using {}.");

    protected final String message;

    PreRevisionLog(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
