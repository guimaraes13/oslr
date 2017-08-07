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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory for {@link IterationTimeStamp}s.
 * <p>
 * Created on 06/08/17.
 *
 * @author Victor Guimarães
 */
public class IterationTimeStampFactory {

    protected final String iterationPrefix;

    protected Map<Integer, Map<IterationTimeMessage, IterationTimeStamp>> messages;

    /**
     * Default constructor with the iteration prefix.
     *
     * @param iterationPrefix the iteration prefix
     */
    public IterationTimeStampFactory(String iterationPrefix) {
        this.iterationPrefix = iterationPrefix;
        this.messages = new HashMap<>();
    }

    /**
     * Gets the time stamp of the iteration with the message.
     *
     * @param iterationIndex the iteration index
     * @param message        the message
     * @return the time stamp
     */
    public IterationTimeStamp getTimeStamp(int iterationIndex, IterationTimeMessage message) {
        final Map<IterationTimeMessage, IterationTimeStamp>
                iterationMessages = messages.computeIfAbsent(
                iterationIndex, i -> new EnumMap<>(IterationTimeMessage.class));

        return iterationMessages.computeIfAbsent(message, m -> new IterationTimeStamp(message, iterationPrefix,
                                                                                      iterationIndex));
    }

}
