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
    BEGIN_EVALUATION("Begin evaluation."),
    END_EVALUATION("End evaluation"),
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
