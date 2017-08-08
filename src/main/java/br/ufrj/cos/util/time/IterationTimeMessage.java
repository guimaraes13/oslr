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

package br.ufrj.cos.util.time;

/**
 * Enum to help measuring time of iteration process.
 * <p>
 * Created on 06/08/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("JavaDoc")
public enum IterationTimeMessage {

    BEGIN("Begin of %s%d"),
    LOAD_KNOWLEDGE_DONE("Load of knowledge from %s%d done"),
    REVISION_DONE("Revision on %s%d done"),
    TRAIN_EVALUATION_DONE("Train evaluation on %s%d done"),
    TEST_EVALUATION_DONE("Test evaluation on %s%d done"),
    EVALUATION_DONE("End of evaluation on %s%d"),
    SAVING_EVALUATION_DONE("End of saving evaluation on %s%d\t to files"),
    END("End of %s%d");

    final String message;

    IterationTimeMessage(String message) {
        this.message = message;
    }

    protected String getMessage(String iterationPrefix, int iterationIndex) {
        return String.format(message, iterationPrefix, iterationIndex);
    }

}
