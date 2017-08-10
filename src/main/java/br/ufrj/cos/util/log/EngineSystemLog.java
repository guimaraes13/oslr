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
 * Centralizes log messages from the engine system translator.
 * <p>
 * Created on 01/08/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings({"JavaDoc"})
public enum EngineSystemLog {

    INITIALIZING_ENGINE_SYSTEM_TRANSLATOR("Initializing EngineSystemTranslator:\t{}"),
    TRAINING_PARAMETERS("Training the parameters of the engine system."),
    SAVING_TRAINED_PARAMETERS_AS_CURRENT("Saving the trained parameters as current."),
    SAVING_PARAMETERS_TO_FILE("Saving the trained parameters to file:\t{}"),
    LOADING_PARAMETERS_FROM_FILE("Loading the parameters from file:\t{}"),

    CALLING_REVISION_OF_EXAMPLE("Calling the revision for\t{} examples."),

    EXAMPLES_PLACED_AT_LEAVES("New examples placed at the leaves of the tree, total:\t{}"),
    CALLING_REVISION_OF_LEAVES("Calling the revision for\t{} modified leaves of predicate:\t{}."),;

    protected final String message;

    EngineSystemLog(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
