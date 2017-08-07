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
 * Enum to help measuring time of a run.
 * <p>
 * Created on 06/08/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("JavaDoc")
public enum RunTimeStamp implements TimeStampTag {

    BEGIN("Begin."),
    BEGIN_INITIALIZE("Begin initialize."),
    END_INITIALIZE("End initialize."),
    BEGIN_TRAIN("Begin training."),
    END_TRAIN("End training."),
    BEGIN_DISK_OUTPUT("Begin disk output."),
    END_DISK_OUTPUT("End disk output."),
    END("End.");

    final String message;

    RunTimeStamp(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return toString();
    }

    @Override
    public String toString() {
        return message;
    }

}
