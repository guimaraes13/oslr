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
 * Class to help measuring time of iteration process.
 * <p>
 * Created on 06/08/17.
 *
 * @author Victor Guimarães
 */
public class IterationTimeStamp implements TimeStampTag {

    protected final IterationTimeMessage message;
    protected final String iterationPrefix;
    protected final int iterationIndex;

    /**
     * Default constructor.
     *
     * @param message         the message relative to the iteration prefix and index
     * @param iterationPrefix the iteration prefix
     * @param iterationIndex  the index
     */
    public IterationTimeStamp(IterationTimeMessage message, String iterationPrefix, int iterationIndex) {
        this.message = message;
        this.iterationPrefix = iterationPrefix;
        this.iterationIndex = iterationIndex;
    }

    @Override
    public String getMessage() {
        return message.getMessage(iterationPrefix, iterationIndex);
    }

    @Override
    public int hashCode() {
        int result = message.hashCode();
        result = 31 * result + iterationPrefix.hashCode();
        result = 31 * result + iterationIndex;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof IterationTimeStamp)) { return false; }

        IterationTimeStamp that = (IterationTimeStamp) o;

        if (iterationIndex != that.iterationIndex) { return false; }
        if (message != that.message) { return false; }
        return iterationPrefix.equals(that.iterationPrefix);
    }

}
