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
    ADDING_NON_GROUND_FACT("Adding non ground fact:\t{}.");

    protected final String message;

    EngineSystemLog(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
